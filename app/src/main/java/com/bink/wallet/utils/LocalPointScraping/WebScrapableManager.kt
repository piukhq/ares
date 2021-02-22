package com.bink.wallet.utils.LocalPointScraping

import android.content.Context
import android.util.Log
import androidx.constraintlayout.widget.ConstraintLayout
import com.bink.wallet.model.request.membership_card.MembershipCardRequest
import com.bink.wallet.model.response.membership_card.MembershipCard
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import org.koin.core.KoinComponent
import org.koin.core.inject

object WebScrapableManager : KoinComponent {

    private val webScrapeViewModel: WebScrapeViewModel by inject()

    private val scrapableAgents = arrayListOf(TescoScrapableAgent())

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

    suspend fun tryScrapeCards(membershipCards: List<MembershipCard>, context: Context?, parentView: ConstraintLayout): List<MembershipCard>? {
        Log.d("LocalPointScrape", "tryScrapeCards")
        if (context != null) {

            val storedCredentials = webScrapeViewModel.getWebScrapeCredentials()

            for (card in membershipCards) {

                //swap this if statement to true
                if (!card.isAuthorised()) {
                    Log.d("LocalPointScrape", "Attempt on ${card.id} - ${card.plan?.account?.company_name}")

                    val credentials = storedCredentials?.firstOrNull { it.id.toString().equals(card.membership_plan) }
                    val agent = scrapableAgents.firstOrNull { it.membershipPlanId.toString().equals(card.membership_plan) }

                    Log.d("LocalPointScrape", "Credentials ${credentials?.email} ${credentials?.password}")
                    Log.d("LocalPointScrape", "Agent ${agent?.merchant?.name}")

                    val pointScrapingUtil = PointScrapingUtil()

                    coroutineScope {
                        val scrapeRequest = async { pointScrapingUtil.performScrape(context, agent?.merchant, parentView, credentials?.email, credentials?.password) }
                        val scrapeResponse = scrapeRequest.await()

                        //Log.d("LocalPointScrape", "isDone ${scrapeResponse?.first}, ${scrapeResponse?.second}")
                        //Once we have the points we need to store it against the card
                        //we also need to store the card as authorized
                    }

                }

            }

            return membershipCards


        } else { return null }

    }

}
