package com.bink.wallet.utils.LocalPointScraping

import android.content.Context
import android.os.CountDownTimer
import android.util.Log
import androidx.constraintlayout.widget.ConstraintLayout
import com.bink.wallet.model.request.membership_card.MembershipCardRequest
import com.bink.wallet.model.response.membership_card.CardBalance
import com.bink.wallet.model.response.membership_card.CardStatus
import com.bink.wallet.model.response.membership_card.MembershipCard
import com.bink.wallet.utils.LocalStoreUtils
import com.bink.wallet.utils.enums.MembershipCardStatus

object WebScrapableManager {

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
                    membershipCardRequest.account?.authorise_fields?.let { authorizeFields ->

                        val username = authorizeFields.firstOrNull {
                            (it.column ?: "").equals(scrapableAgent.usernameFieldTitle)
                        }?.value
                        val password = authorizeFields.firstOrNull {
                            (it.column ?: "").equals(scrapableAgent.passwordFieldTitle)
                        }?.value

                        val webScrapeCredentials = WebScrapeCredentials(membershipCardRequest.membership_plan, username, password)
                        storeCredentials(webScrapeCredentials)

                    }
                }
            }

        }

    }

    fun tryScrapeCards(index: Int, cards: List<MembershipCard>, context: Context?, callback: (List<MembershipCard>?) -> Unit) {
        if (context == null) return
        if (index == 0) membershipCards = cards

        Log.d("LocalPointScrape", "tryScrapeCards index: $index")

        //We need a timer in the case that an uncaught error occurs, it will automatically carry on after 60 seconds
        val timer = object : CountDownTimer(60000, 10000) {
            override fun onFinish() {
                Log.d("LocalPointScrape", "Countdown Timer Finished")
                if (membershipCards != null) {
                    membershipCards!![index].status = CardStatus(null, MembershipCardStatus.FAILED.status)
                }
                tryScrapeCards(index + 1, cards, context, callback)
            }

            override fun onTick(millisUntilFinished: Long) {
            }
        }


        try {
            timer.start()

            val card = cards[index]

            retrieveCredentials(card.membership_plan).let { credentials ->

                val agent = scrapableAgents.firstOrNull { it.membershipPlanId.toString().equals(card.membership_plan) }

                if (credentials == null || agent == null) {
                    //Try next card
                    Log.d("LocalPointScrape", "Credentials null, agent null")
                    timer.cancel()
                    tryScrapeCards(index + 1, cards, context, callback)
                } else {
                    PointScrapingUtil.performScrape(context, agent.merchant, credentials.email, credentials.password) { pointScrapeResponse ->

                        Log.d("LocalPointScrape", "Scrape returned $pointScrapeResponse")

                        pointScrapeResponse.points?.let { points ->
                            if (membershipCards != null) {
                                val balance = CardBalance(points, null, null, agent.cardBalanceSuffix, System.currentTimeMillis())
                                membershipCards!![index].balances = arrayListOf(balance)
                                membershipCards!![index].status = CardStatus(null, MembershipCardStatus.AUTHORISED.status)
                                membershipCards!![index].isScraped = true

                                timer.cancel()
                                tryScrapeCards(index + 1, cards, context, callback)
                            }
                        }

                    }
                }
            }

        } catch (e: IndexOutOfBoundsException) {
            //Ran through all cards, return updated values
            Log.d("LocalPointScrape", "Index out of bounds")
            timer.cancel()
            PointScrapingUtil.lastSeenURL = null
            callback(membershipCards)
        }


    }

    private fun storeCredentials(webScrapeCredentials: WebScrapeCredentials) {

        if (webScrapeCredentials.id == null) return

        //Storing the username/email here
        webScrapeCredentials.email?.let {
            LocalStoreUtils.setAppSharedPref(
                encryptedKeyForCardId(webScrapeCredentials.id, CredentialType.USERNAME),
                it
            )
        }

        //Storing password here
        webScrapeCredentials.password?.let {
            LocalStoreUtils.setAppSharedPref(
                encryptedKeyForCardId(webScrapeCredentials.id, CredentialType.PASSWORD),
                it
            )
        }
    }

    private fun retrieveCredentials(cardId: String?): WebScrapeCredentials? {
        if (cardId == null) return null
        val username =
            LocalStoreUtils.getAppSharedPref(encryptedKeyForCardId(cardId, CredentialType.USERNAME))
        val password =
            LocalStoreUtils.getAppSharedPref(encryptedKeyForCardId(cardId, CredentialType.PASSWORD))

        return WebScrapeCredentials(cardId, username, password)
    }

    fun removeCredentials(cardId: String) {
        LocalStoreUtils.removeKey(encryptedKeyForCardId(cardId, CredentialType.USERNAME))
        LocalStoreUtils.removeKey(encryptedKeyForCardId(cardId, CredentialType.PASSWORD))
    }

    private fun encryptedKeyForCardId(cardId: String, credentialType: CredentialType): String {

        return String.format(BASE_ENCRYPTED_KEY_SHARED_PREFERENCES, cardId, credentialType.name)
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
