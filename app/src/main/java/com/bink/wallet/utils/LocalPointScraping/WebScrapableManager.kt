package com.bink.wallet.utils.LocalPointScraping

import android.content.Context
import android.os.CountDownTimer
import androidx.constraintlayout.widget.ConstraintLayout
import com.bink.wallet.data.SharedPreferenceManager
import com.bink.wallet.model.request.membership_card.MembershipCardRequest
import com.bink.wallet.model.response.membership_card.CardBalance
import com.bink.wallet.model.response.membership_card.CardStatus
import com.bink.wallet.model.response.membership_card.MembershipCard
import com.bink.wallet.utils.enums.MembershipCardStatus
import org.koin.core.KoinComponent
import org.koin.core.inject

object WebScrapableManager : KoinComponent {

    private val webScrapeViewModel: WebScrapeViewModel by inject()

    private val scrapableAgents = arrayListOf(TescoScrapableAgent())

    private var membershipCards: List<MembershipCard>? = null

    fun storeCredentialsFromRequest(membershipCardRequest: MembershipCardRequest) {

        for (scrapableAgent in scrapableAgents) {
            membershipCardRequest.membership_plan?.toIntOrNull()?.let { membershipPlanId ->
                if (scrapableAgent.membershipPlanId == membershipPlanId) {
                    membershipCardRequest.account?.authorise_fields?.let { authorizeFields ->

                        val username = authorizeFields.firstOrNull { (it.column ?: "").equals(scrapableAgent.usernameFieldTitle) }?.value
                        val password = authorizeFields.firstOrNull { (it.column ?: "").equals(scrapableAgent.passwordFieldTitle) }?.value

                        val webScrapeCredentials = WebScrapeCredentials(membershipPlanId, username, password)
                        webScrapeViewModel.storeWebScrapeCredentials(webScrapeCredentials)

                    }
                }
            }

        }

    }

    fun tryScrapeCards(index: Int, cards: List<MembershipCard>, context: Context?, parentView: ConstraintLayout, callback: (List<MembershipCard>?) -> Unit) {
        if (context == null) return
        if (index == 0) membershipCards = cards
        SharedPreferenceManager.membershipCardsLastScraped = System.currentTimeMillis()

        //We need a timer in the case that an uncaught error occurs, it will automatically carry on after 60 seconds
        val timer = object : CountDownTimer(60000, 10000) {
            override fun onFinish() {
                tryScrapeCards(index + 1, cards, context, parentView, callback)
            }

            override fun onTick(millisUntilFinished: Long) {
            }
        }

        webScrapeViewModel.getWebScrapeCredentials {
            it?.let { storedCredentials ->
                try {
                    timer.start()

                    val card = cards[index]

                    val credentials = storedCredentials?.firstOrNull { it.id.toString().equals(card.membership_plan) }
                    val agent = scrapableAgents.firstOrNull { it.membershipPlanId.toString().equals(card.membership_plan) }

                    if (credentials == null || agent == null) {
                        //Try next card
                        tryScrapeCards(index + 1, cards, context, parentView, callback)
                    } else {
                        PointScrapingUtil.performScrape(context, agent.merchant, parentView, credentials.email, credentials.password) { pointScrapeResponse ->

                            pointScrapeResponse.points?.let { points ->
                                if (membershipCards != null) {
                                    val balance = CardBalance(points, null, null, agent.cardBalanceSuffix, System.currentTimeMillis())
                                    membershipCards!![index].balances = arrayListOf(balance)
                                    membershipCards!![index].status = CardStatus(null, MembershipCardStatus.AUTHORISED.status)
                                    membershipCards!![index].isScraped = true

                                    tryScrapeCards(index + 1, cards, context, parentView, callback)
                                }
                            }

                        }
                    }


                } catch (e: IndexOutOfBoundsException) {
                    //Ran through all cards, return updated values
                    timer.cancel()
                    callback(membershipCards)
                }

            }
        }
    }

}
