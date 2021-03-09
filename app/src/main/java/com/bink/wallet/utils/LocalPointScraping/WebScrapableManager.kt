package com.bink.wallet.utils.LocalPointScraping

import android.content.Context
import android.os.CountDownTimer
import android.util.Log
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.MutableLiveData
import com.bink.wallet.model.request.membership_card.MembershipCardRequest
import com.bink.wallet.model.response.membership_card.CardBalance
import com.bink.wallet.model.response.membership_card.CardStatus
import com.bink.wallet.model.response.membership_card.MembershipCard
import com.bink.wallet.utils.LocalStoreUtils
import com.bink.wallet.utils.enums.CardCodes
import com.bink.wallet.utils.enums.MembershipCardStatus
import com.bink.wallet.utils.logDebug

object WebScrapableManager {

    val newlyAddedCard = MutableLiveData<MembershipCard>()
    val updatedCards = MutableLiveData<List<MembershipCard>?>()

    private var timer: CountDownTimer? = null

    private val scrapableAgents = arrayListOf(TescoScrapableAgent())

    private const val BASE_ENCRYPTED_KEY_SHARED_PREFERENCES =
        "com.bink.wallet.utils.LocalPointScraping.credentials.cardId_%s.%s"

    enum class CredentialType {
        USERNAME,
        PASSWORD
    }

    private var membershipCards: List<MembershipCard>? = null

    fun storeCredentialsFromRequest(membershipCardRequest: MembershipCardRequest) {

        for (scrapableAgent in scrapableAgents) {
            membershipCardRequest.membership_plan?.toIntOrNull()?.let { membershipPlanId ->
                if (scrapableAgent.membershipPlanId == membershipPlanId) {
                    membershipCardRequest.account?.add_fields?.let { addFields ->
                        membershipCardRequest.account?.authorise_fields?.let { authorizeFields ->

                            val username = authorizeFields.firstOrNull {
                                (it.column ?: "").equals(scrapableAgent.usernameFieldTitle)
                            }?.value
                            val password = authorizeFields.firstOrNull {
                                (it.column ?: "").equals(scrapableAgent.passwordFieldTitle)
                            }?.value

                            val uniqueString = addFields.firstOrNull {
                                (it.column ?: "").equals(scrapableAgent.uniqueFieldTitle)
                            }?.value

                            val webScrapeCredentials = WebScrapeCredentials(username, password, uniqueString)
                            storeCredentials(webScrapeCredentials)

                        }
                    }
                }
            }

        }

    }

    fun tryScrapeCards(index: Int, cards: List<MembershipCard>, context: Context?, isAddCard: Boolean, callback: (List<MembershipCard>?) -> Unit) {
        if (context == null) return
        if (index == 0) membershipCards = cards

        logDebug("LocalPointScrape", "tryScrapeCards index: $index")
        timer?.cancel()
        timer = null

        if (timer == null) {
            timer = object : CountDownTimer(60000, 1000) {
                override fun onFinish() {
                    logDebug("LocalPointScrape", "Countdown Timer Finished")
                    if (membershipCards != null) {
                        try {
                            membershipCards!![index].status = CardStatus(listOf(CardCodes.X101.code), MembershipCardStatus.FAILED.status)
                            membershipCards!![index].isScraped = true
                        } catch (e: IndexOutOfBoundsException) {
                        }
                    }
                    tryScrapeCards(index + 1, cards, context, isAddCard, callback)
                }

                override fun onTick(millisUntilFinished: Long) {
                }
            }
        }

        try {
            timer?.start()

            val card = cards[index]

            retrieveCredentials(card.card?.membership_id).let { credentials ->

                val agent = scrapableAgents.firstOrNull { it.membershipPlanId.toString().equals(card.membership_plan) }

                if (!isAddCard) {
                    card.isScraped?.let { isScraped ->
                        if (!isScraped) {
                            tryScrapeCards(index + 1, cards, context, isAddCard, callback)
                            return
                        }
                    }

                }

                if (credentials == null || agent == null) {
                    //Try next card
                    logDebug("LocalPointScrape", "Credentials null, agent null")
                    tryScrapeCards(index + 1, cards, context, isAddCard, callback)
                } else {
                    PointScrapingUtil.performScrape(context, agent.merchant, credentials.email, credentials.password) { pointScrapeResponse ->

                        logDebug("LocalPointScrape", "Scrape returned $pointScrapeResponse")

                        pointScrapeResponse.points?.let { points ->
                            if (membershipCards != null) {
                                val balance = CardBalance(points, null, null, agent.cardBalanceSuffix, System.currentTimeMillis())
                                membershipCards!![index].balances = arrayListOf(balance)
                                membershipCards!![index].status = CardStatus(null, MembershipCardStatus.AUTHORISED.status)
                                membershipCards!![index].isScraped = true

                                tryScrapeCards(index + 1, cards, context, isAddCard, callback)
                            }
                        }

                    }
                }
            }

        } catch (e: IndexOutOfBoundsException) {
            //Ran through all cards, return updated values
            logDebug("LocalPointScrape", "Index out of bounds")
            timer?.cancel()
            timer = null
            PointScrapingUtil.lastSeenURL = null

            if (isAddCard) {
                membershipCards?.get(0)?.let{ newCard ->
                    newlyAddedCard.value = newCard
                }
            } else {
                updatedCards.value = membershipCards
            }

            callback(membershipCards)
        }


    }

    private fun storeCredentials(webScrapeCredentials: WebScrapeCredentials) {
        if (webScrapeCredentials.uniqueString == null) return

        //Storing the username/email here
        webScrapeCredentials.email?.let {
            LocalStoreUtils.setAppSharedPref(
                encryptedKeyForCardId(webScrapeCredentials.uniqueString, CredentialType.USERNAME),
                it
            )
        }

        //Storing password here
        webScrapeCredentials.password?.let {
            LocalStoreUtils.setAppSharedPref(
                encryptedKeyForCardId(webScrapeCredentials.uniqueString, CredentialType.PASSWORD),
                it
            )
        }
    }

    private fun retrieveCredentials(uniqueString: String?): WebScrapeCredentials? {
        if (uniqueString == null) return null
        val username =
            LocalStoreUtils.getAppSharedPref(encryptedKeyForCardId(uniqueString, CredentialType.USERNAME))
        val password =
            LocalStoreUtils.getAppSharedPref(encryptedKeyForCardId(uniqueString, CredentialType.PASSWORD))

        return WebScrapeCredentials(username, password, uniqueString)
    }

    fun removeCredentials(uniqueString: String) {
        LocalStoreUtils.removeKey(encryptedKeyForCardId(uniqueString, CredentialType.USERNAME))
        LocalStoreUtils.removeKey(encryptedKeyForCardId(uniqueString, CredentialType.PASSWORD))
    }

    private fun encryptedKeyForCardId(uniqueString: String, credentialType: CredentialType): String {

        return String.format(BASE_ENCRYPTED_KEY_SHARED_PREFERENCES, uniqueString, credentialType.name)
    }

    fun mapOldToNewCards(oldCards: List<MembershipCard>, newCards: List<MembershipCard>?): List<MembershipCard> {
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

}
