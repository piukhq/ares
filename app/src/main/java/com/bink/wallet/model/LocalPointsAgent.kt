package com.bink.wallet.model

import com.bink.wallet.model.response.membership_plan.PlanField

data class LocalPointsAgent(
    val merchant: String,
    val membership_plan_id: AgentMembershipPlanId,
    val enabled: AgentEnabled,
    val loyaltyScheme: AgentLoyaltyScheme,
    val points_collection_url: String,
    val fields: ArrayList<PlanField>,
    val script_file_name: String
)

data class AgentMembershipPlanId(val dev: String, val staging: String, val preprod: String, val production: String)

data class AgentEnabled(val android: Boolean, val android_debug: Boolean)

data class AgentLoyaltyScheme(val balance_currency: String?, val balance_prefix: String?, val balance_suffix: String?)
