package com.bink.wallet.utils.local_point_scraping

import android.content.Context
import android.os.CountDownTimer
import androidx.lifecycle.MutableLiveData
import com.bink.wallet.model.LocalPointsAgent
import com.bink.wallet.model.currentAgent
import com.bink.wallet.model.getId
import com.bink.wallet.model.isEnabled
import com.bink.wallet.model.request.membership_card.MembershipCardRequest
import com.bink.wallet.model.response.membership_card.CardBalance
import com.bink.wallet.model.response.membership_card.CardStatus
import com.bink.wallet.model.response.membership_card.MembershipCard
import com.bink.wallet.utils.*
import com.bink.wallet.utils.enums.CardCodes
import com.bink.wallet.utils.enums.MembershipCardStatus
import java.util.*
import kotlin.collections.ArrayList

object WebScrapableManager {

    val newlyAddedCard = MutableLiveData<MembershipCard>()
    val updatedCards = MutableLiveData<List<MembershipCard>?>()

    private val localPointsCollection = RemoteConfigUtil().localPointsCollection
    private val scrapableAgents = localPointsCollection?.agents ?: ArrayList()

    private val deletedCards = ArrayList<String>()

    var currentAgent: LocalPointsAgent? = null

    private var userName: String? = null
    private var password: String? = null

    private var timer: CountDownTimer? = null

    private const val BASE_ENCRYPTED_KEY_SHARED_PREFERENCES =
        "com.bink.wallet.utils.LocalPointScraping.credentials.cardId_%s.%s"

    enum class CredentialType {
        USERNAME,
        PASSWORD
    }

    private var membershipCards: List<MembershipCard>? = null

    fun setUsernameAndPassword(request: MembershipCardRequest): MembershipCardRequest {
        for (scrapableAgent in scrapableAgents) {
            request.membership_plan?.toIntOrNull()?.let { membershipPlanId ->
                if (scrapableAgent.membership_plan_id.getId() == membershipPlanId) {
                    request.account?.authorise_fields?.let { authoriseFields ->

                        userName = authoriseFields.firstOrNull {
                            (it.column
                                                        ?: "").lowercase(Locale.getDefault()) == scrapableAgent.fields.username_field_common_name
                        }?.value
                        password = authoriseFields.firstOrNull {
                            (it.column ?: "").lowercase(Locale.getDefault()) == "password"
                        }?.value

                        request.account.authorise_fields!!.removeAll { it.value == userName }
                        request.account.authorise_fields!!.removeAll { it.value == password }
                        return request
                    }

                }
            }

        }

        return request
    }

    fun storeCredentialsFromRequest(cardId: String) {

        if (userName == null || password == null) return

        val webScrapeCredentials = WebScrapeCredentials(userName, password, cardId)
        storeCredentials(webScrapeCredentials)

    }

    /**
     * When starting tryScrapeCards we pass an index as a parameter to make sure
     * that we loop through all available cards, specifically for a refresh in the
     * loyalty wallet.
     */

    fun tryScrapeCards(
        index: Int,
        cards: List<MembershipCard>,
        context: Context?,
        isAddCard: Boolean,
        currentStatus: (Boolean, String, Boolean, String?) -> Unit,
        callback: (List<MembershipCard>?) -> Unit
    ) {
        if (context == null) return
        if (index == 0) membershipCards = cards

        logDebug("LocalPointScrape", "tryScrapeCards index: $index")
        timer?.cancel()
        timer = null
        currentAgent = null

        /**
         * We start a 60 second timer to ensure that there is a timeout period.
         */

        if (timer == null) {
            timer = object : CountDownTimer(60000, 1000) {
                override fun onFinish() {
                    logDebug("LocalPointScrape", "Countdown Timer Finished")
                    if (membershipCards != null) {
                        try {
                            membershipCards!![index].status = CardStatus(
                                listOf(CardCodes.X101.code),
                                MembershipCardStatus.FAILED.status
                            )
                            membershipCards!![index].isScraped = true
                        } catch (e: IndexOutOfBoundsException) {
                        }
                    }

                    SentryUtils.logError(
                        SentryErrorType.LOCAL_POINTS_SCRAPE_SITE,
                        LocalPointScrapingError.UNHANDLED_IDLING.issue,
                        currentAgent?.merchant,
                        isAddCard
                    )

                    tryScrapeCards(index + 1, cards, context, isAddCard, currentStatus, callback)
                }

                override fun onTick(millisUntilFinished: Long) {
                }
            }
        }

        /**
         * After the null checks we:
         * > start the 60 seccond timer
         * > initialize the current card we need
         * > ensure the user has credentials for said card
         * > ensure there is an available agent stored in remote config for the merchant
         * > perform a scrape
         */

        try {
            timer?.start()

            val card = cards[index]

            retrieveCredentials(card.id).let { credentials ->

                val agent = localPointsCollection?.currentAgent(card.membership_plan?.toIntOrNull())

                currentAgent = agent

                if (!isAddCard) {
                    card.isScraped?.let { isScraped ->
                        if (!isScraped) {
                            tryScrapeCards(
                                index + 1,
                                cards,
                                context,
                                isAddCard,
                                currentStatus,
                                callback
                            )
                            return
                        }
                    }
                }

                if (credentials == null || agent == null) {
                    //Try next card
                    logDebug("LocalPointScrape", "Credentials null, agent null")
                    tryScrapeCards(index + 1, cards, context, isAddCard, currentStatus, callback)
                } else {

                    /**
                     * We then assign the current status for analytic purposes
                     * and proceed to scrape
                     */

                    currentStatus(true, "", false, "")

                    PointScrapingUtil.performNewScrape(
                        context,
                        isAddCard,
                        agent,
                        credentials.email,
                        credentials.password,
                        {
                            currentStatus(
                                false,
                                agent.merchant,
                                false,
                                it
                            )
                        }
                    ) { pointScrapeResponse ->

                        if (agent.isEnabled()) {

                            logDebug("LocalPointScrape", "Scrape returned $pointScrapeResponse")

                            /**
                             * Once we have the response there are two outcomes.
                             * If we have the points string, we then return the value
                             * If we have the error message, we return a failed status
                             * We then proceed to try scrape the following card in the list
                             */

                            pointScrapeResponse.pointsString?.let { points ->
                                if (membershipCards != null) {
                                    val balance = CardBalance(
                                        points,
                                        agent.loyalty_scheme.balance_currency,
                                        agent.loyalty_scheme.balance_prefix,
                                        agent.loyalty_scheme.balance_suffix,
                                        (System.currentTimeMillis() / 1000)

                                    )
                                    membershipCards!![index].balances = arrayListOf(balance)
                                    membershipCards!![index].status =
                                        CardStatus(null, MembershipCardStatus.AUTHORISED.status)
                                    membershipCards!![index].isScraped = true

                                    currentStatus(
                                        false,
                                        agent.merchant,
                                        true,
                                        null
                                    )

                                    tryScrapeCards(
                                        index + 1,
                                        cards,
                                        context,
                                        isAddCard,
                                        currentStatus,
                                        callback
                                    )
                                }
                            }

                            pointScrapeResponse.errorMessage?.let {
                                if (membershipCards != null) {
                                    try {
                                        membershipCards!![index].status = CardStatus(
                                            listOf(CardCodes.X101.code),
                                            MembershipCardStatus.FAILED.status
                                        )
                                        membershipCards!![index].isScraped = true
                                    } catch (e: IndexOutOfBoundsException) {
                                    }
                                }

                                tryScrapeCards(
                                    index + 1,
                                    cards,
                                    context,
                                    isAddCard,
                                    currentStatus,
                                    callback
                                )
                            }

                        } else {
                            tryScrapeCards(
                                index + 1,
                                cards,
                                context,
                                isAddCard,
                                currentStatus,
                                callback
                            )
                        }

                    }
                }
            }

        } catch (e: IndexOutOfBoundsException) {

            /**
             * We determine when we're done by causing an out of bounds exception.
             */

            logDebug("LocalPointScrape", "Index out of bounds")
            timer?.cancel()
            timer = null

            var nonDeletedCards = ArrayList<MembershipCard>()

            if (membershipCards != null) {
                membershipCards?.forEach { membershipCard ->
                    if (!deletedCards.contains(membershipCard.id)) {
                        nonDeletedCards.add(membershipCard)
                    }
                }
            }

            /**
             * Check to see if we're using the add card journey
             * If we are we return the first card in the list to add in to the loyalty wallet
             * If we aren't then we return the whole list
             **/

            if (isAddCard) {
                if (nonDeletedCards.isNotEmpty()) {
                    nonDeletedCards[0].let { newCard ->
                        newlyAddedCard.value = newCard
                    }
                }
            } else {
                updatedCards.value = nonDeletedCards
            }

            deletedCards.clear()
            callback(nonDeletedCards)
        }

    }

    private fun storeCredentials(webScrapeCredentials: WebScrapeCredentials) {
        if (webScrapeCredentials.cardId == null) return

        //Storing the username/email here
        webScrapeCredentials.email?.let {
            LocalStoreUtils.setAppSharedPref(
                encryptedKeyForCardId(webScrapeCredentials.cardId, CredentialType.USERNAME),
                it
            )
        }

        //Storing password here
        webScrapeCredentials.password?.let {
            LocalStoreUtils.setAppSharedPref(
                encryptedKeyForCardId(webScrapeCredentials.cardId, CredentialType.PASSWORD),
                it
            )
        }

        userName = null
        password = null
    }

    private fun retrieveCredentials(cardId: String?): WebScrapeCredentials? {
        if (cardId == null) return null
        val username =
            LocalStoreUtils.getAppSharedPref(encryptedKeyForCardId(cardId, CredentialType.USERNAME))
                ?: return null
        val password =
            LocalStoreUtils.getAppSharedPref(encryptedKeyForCardId(cardId, CredentialType.PASSWORD))
                ?: return null

        return WebScrapeCredentials(username, password, cardId)
    }

    fun removeCredentials(cardId: String) {
        LocalStoreUtils.removeKey(encryptedKeyForCardId(cardId, CredentialType.USERNAME))
        LocalStoreUtils.removeKey(encryptedKeyForCardId(cardId, CredentialType.PASSWORD))

        deletedCards.add(cardId)
    }

    private fun encryptedKeyForCardId(cardId: String, credentialType: CredentialType): String {
        return String.format(BASE_ENCRYPTED_KEY_SHARED_PREFERENCES, cardId, credentialType.name)
    }

    fun mapOldToNewCards(
        oldCards: List<MembershipCard>,
        newCards: List<MembershipCard>?
    ): List<MembershipCard> {
        if (newCards == null) return emptyList()
        val storedScrapedCards = oldCards.filter { it.isScraped == true }

        for (scrapedCard in storedScrapedCards) {
            val index = newCards.indexOfFirst { it.id.equals(scrapedCard.id) }
            if (index != -1) {
                newCards[index].balances = scrapedCard.balances
                newCards[index].status = scrapedCard.status
                newCards[index].isScraped = true
            }
        }

        return newCards
    }

    fun isCardScrapable(planId: String?): Boolean {
        localPointsCollection?.currentAgent(planId?.toIntOrNull())?.let {
            return it.isEnabled()
        }

        return false
    }

}
