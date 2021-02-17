package com.bink.wallet.utils.LocalPointScraping

import android.util.Log
import com.bink.wallet.model.request.membership_card.MembershipCardRequest
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

    /** To test if it stores, uncomment this and call somewhere.
    fun logCreds() {
        webScrapeViewModel.getWebScrapeCredentials{
            Log.d("TescoLPS", "Stored creds $it")
        }
    }
     **/

}