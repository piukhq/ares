package com.bink.wallet.utils.LocalPointScraping

import com.bink.wallet.model.response.membership_plan.PlanField
import com.bink.wallet.scenes.add_auth_enrol.AddAuthItemWrapper
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
            addAuthItems.add(AddAuthItemWrapper(authField))
        }

        return addAuthItems
    }

}

enum class PointScrapeSite(val remoteName: String, val signInURL: String, val scrapeURL: String) {
    TESCO("tesco", "https://secure.tesco.com/account/en-GB/login?from=https://secure.tesco.com/Clubcard/MyAccount/home/Home", "https://secure.tesco.com/Clubcard/MyAccount/home/Home")
}