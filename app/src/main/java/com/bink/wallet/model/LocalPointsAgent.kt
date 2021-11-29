package com.bink.wallet.model

import com.bink.wallet.BuildConfig
import com.bink.wallet.model.request.membership_card.PlanFieldsRequest
import com.bink.wallet.model.response.membership_plan.PlanField
import com.bink.wallet.network.ApiConfig
import com.bink.wallet.network.ApiConstants
import com.bink.wallet.scenes.add_auth_enrol.AddAuthItemWrapper
import com.bink.wallet.utils.enums.BuildTypes
import com.bink.wallet.utils.enums.TypeOfField
import java.util.*

data class LocalPointsAgent(
    val merchant: String,
    val membership_plan_id: AgentMembershipPlanId,
    val enabled: AgentEnabled,
    val loyalty_scheme: AgentLoyaltyScheme,
    val points_collection_url: String,
    val fields: AgentFields,
    val script_file_name: String
)

fun LocalPointsAgent.isEnabled(): Boolean {
    return if (BuildConfig.BUILD_TYPE.toLowerCase(Locale.ENGLISH) != BuildTypes.RELEASE.type) {
        enabled.android_debug
    } else {
        enabled.android
    }
}

data class AgentMembershipPlanId(val dev: Int, val staging: Int, val preprod: Int, val production: Int)

fun AgentMembershipPlanId.getId(): Int {
    return when (ApiConstants.BASE_URL) {
        ApiConfig.PROD_URL -> production
        ApiConfig.STAGING_URL -> staging
        ApiConfig.DEV_URL -> dev
        else -> preprod
    }
}

data class AgentEnabled(val android: Boolean, val android_debug: Boolean)

data class AgentLoyaltyScheme(val balance_currency: String?, val balance_prefix: String?, val balance_suffix: String?)

data class AgentFields(val username_field_common_name: String, val required_credentials: ArrayList<String>, val auth_fields: ArrayList<PlanField>)

fun AgentFields.getRemoteAuthFields(): List<AddAuthItemWrapper> {
    val addAuthItems = ArrayList<AddAuthItemWrapper>()

    for (authField in auth_fields) {
        authField.typeOfField = TypeOfField.AUTH
        val fieldsRequest = PlanFieldsRequest(authField.column, null)
        addAuthItems.add(AddAuthItemWrapper(authField, fieldsRequest))
    }

    return addAuthItems
}