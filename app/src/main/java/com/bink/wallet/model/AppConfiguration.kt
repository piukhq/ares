package com.bink.wallet.model

data class ConfigFile(val local_points_collection: LocalPointsCollection, val app_config: AppConfiguration, val dynamic_actions: ArrayList<DynamicAction>, val beta: Beta)

data class AppConfiguration(val in_app_review_enabled: Boolean, val recommended_live_app_version: RecommendedLiveAppVersion)

data class LocalPointsCollection(val idle_threshold: Int, val idle_retry_limit: Int, val agents: ArrayList<LocalPointsAgent>)

data class Beta(val features: ArrayList<BetaFeature>, val users: ArrayList<BetaUser>)

data class BetaUser(val uid: String)

data class BetaFeature(val slug: String, val type: String, val title: String, val description: String, val enabled: Boolean)

fun LocalPointsCollection.currentAgent(membershipPlanId: Int?): LocalPointsAgent? {
    val currentAgent = agents.filter { it.membership_plan_id.getId() == membershipPlanId }
    if (currentAgent.isNotEmpty()) {
        return currentAgent[0]
    }

    return null
}

