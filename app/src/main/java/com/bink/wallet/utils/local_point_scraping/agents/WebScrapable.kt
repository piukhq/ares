package com.bink.wallet.utils.local_point_scraping.agents

import com.bink.wallet.model.request.membership_card.PlanFieldsRequest
import com.bink.wallet.model.response.membership_plan.PlanField
import com.bink.wallet.scenes.add_auth_enrol.AddAuthItemWrapper
import com.bink.wallet.utils.REMOTE_CONFIG_LPC_MASTER_ENABLED
import com.bink.wallet.utils.enums.TypeOfField
import com.bink.wallet.utils.getDebugSuffix
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.gson.Gson
import com.google.gson.JsonParseException
import com.google.gson.reflect.TypeToken

abstract class WebScrapable {

    abstract val merchant: PointScrapeSite
    abstract val membershipPlanId: Int
    abstract val usernameFieldTitle: String
    abstract val passwordFieldTitle: String
    abstract val cardBalancePrefix: String
    abstract val cardBalanceSuffix: String

    fun isEnabled(firebaseRemoteConfig: FirebaseRemoteConfig): Boolean {
        val remoteConfigKey = "LPC_${merchant.remoteName}_enabled"
        val masterEnabled = firebaseRemoteConfig.getBoolean(REMOTE_CONFIG_LPC_MASTER_ENABLED.getDebugSuffix())
        if (!masterEnabled) return false
        return firebaseRemoteConfig.getBoolean(remoteConfigKey.getDebugSuffix())
    }

    fun getRemoteAuthFields(firebaseRemoteConfig: FirebaseRemoteConfig): List<AddAuthItemWrapper> {
        val remoteConfigKey = "LPC_${merchant.remoteName}_auth_fields"
        val authFieldsString = firebaseRemoteConfig.getString(remoteConfigKey.getDebugSuffix())
        val authFields: ArrayList<PlanField>
        val addAuthItems = ArrayList<AddAuthItemWrapper>()

        try {
            authFields = Gson().fromJson(authFieldsString, object : TypeToken<java.util.ArrayList<PlanField?>?>() {}.type)
        } catch (e: JsonParseException) {
            return arrayListOf()
        }

        for (authField in authFields) {
            authField.typeOfField = TypeOfField.AUTH
            val fieldsRequest = PlanFieldsRequest(authField.column, null)
            addAuthItems.add(AddAuthItemWrapper(authField, fieldsRequest))
        }

        return addAuthItems
    }

}

enum class PointScrapeSite(val remoteName: String, val signInURL: String, val scrapeURL: String) {
    TESCO("tesco", "https://secure.tesco.com/account/en-GB/login?from=https://secure.tesco.com/Clubcard/MyAccount/home/Home", "https://secure.tesco.com/Clubcard/MyAccount/home/Home"),
    WATERSTONES("waterstones", "https://www.waterstones.com/plus/signin", "https://www.waterstones.com/account/waterstonescard")
}