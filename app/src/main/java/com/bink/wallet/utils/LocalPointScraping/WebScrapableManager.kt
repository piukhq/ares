package com.bink.wallet.utils.LocalPointScraping

import android.content.Context
import android.util.Log
import androidx.constraintlayout.widget.ConstraintLayout
import com.bink.wallet.model.request.membership_card.MembershipCardRequest
import com.bink.wallet.model.response.membership_card.CardBalance
import com.bink.wallet.model.response.membership_card.CardStatus
import com.bink.wallet.model.response.membership_card.MembershipCard
import com.bink.wallet.utils.enums.MembershipCardStatus
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import org.koin.core.KoinComponent
import org.koin.core.inject
import java.lang.IndexOutOfBoundsException

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

                        Log.d("TescoLPS", "$username, $password")

                    }
                }
            }

        }

    }

    fun tryScrapeCards(index: Int, cards: List<MembershipCard>, context: Context?, parentView: ConstraintLayout, callback: (List<MembershipCard>?) -> Unit) {
        if (context == null) return
        if (index == 0) membershipCards = cards

        webScrapeViewModel.getWebScrapeCredentials {
            it?.let { storedCredentials ->
                try {

                    //TODO: ADD 60 SECOND TIMER TO ITERATE

                    val card = cards[index]

                    Log.d("LocalPointScrape", "Attempt on ${card.id} - ${card.plan?.account?.company_name}")

                    val credentials = storedCredentials?.firstOrNull { it.id.toString().equals(card.membership_plan) }
                    val agent = scrapableAgents.firstOrNull { it.membershipPlanId.toString().equals(card.membership_plan) }

                    Log.d("LocalPointScrape", "Credentials ${credentials?.email} ${credentials?.password}")
                    Log.d("LocalPointScrape", "Agent ${agent?.merchant?.name}")

                    val pointScrapingUtil = PointScrapingUtil()

                    if (credentials == null || agent == null) {
                        //Try next card
                        tryScrapeCards(index + 1, cards, context, parentView, callback)
                    } else {
                        pointScrapingUtil.performScrape(context, agent?.merchant, parentView, credentials?.email, credentials?.password) { pointScrapeResponse ->

                            Log.d("LocalPointScrape", "isDone ${pointScrapeResponse.isDone()}")

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
                    callback(membershipCards)
                    Log.d("LocalPointScrape", "${e.localizedMessage}")
                }

            }
        }
    }

}
