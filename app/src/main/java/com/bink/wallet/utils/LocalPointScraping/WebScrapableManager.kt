package com.bink.wallet.utils.LocalPointScraping

import android.util.Log
import com.bink.wallet.model.request.membership_card.MembershipCardRequest

object WebScrapableManager {

    private val scrapableAgents = arrayListOf(TescoScrapableAgent())

    fun getCredentialsFromRequest(membershipCardRequest: MembershipCardRequest) {

        for (scrapableAgent in scrapableAgents) {
            if (scrapableAgent.membershipPlanId == membershipCardRequest.membership_plan?.toIntOrNull()) {
                membershipCardRequest.account?.authorise_fields?.let { authorizeFields ->

                    val username = authorizeFields.firstOrNull { (it.column ?: "").equals(scrapableAgent.usernameFieldTitle) }?.value
                    val password = authorizeFields.firstOrNull { (it.column ?: "").equals(scrapableAgent.passwordFieldTitle) }?.value

                    Log.d("TescoLPS", "$username, $password")

                }
            }
        }

    }

}