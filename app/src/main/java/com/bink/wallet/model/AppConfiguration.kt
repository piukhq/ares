package com.bink.wallet.model

data class ConfigFile(val local_points_collection: LocalPointsCollection, val app_config: AppConfiguration, val dynamic_actions: ArrayList<DynamicAction>)

data class AppConfiguration(val in_app_review_enabled: Boolean, val recommended_live_app_version: RecommendedLiveAppVersion)

data class LocalPointsCollection(val idle_threshold: Int, val idle_retry_limit: Int, val agents: ArrayList<LocalPointsAgent>)

fun LocalPointsCollection.currentAgent(membershipPlanId: Int?) : LocalPointsAgent? {
    val currentAgent = agents.filter { it.membership_plan_id.getId() == membershipPlanId }
    if (currentAgent.isNotEmpty()) {
        return currentAgent[0]
    }

    return null
}

