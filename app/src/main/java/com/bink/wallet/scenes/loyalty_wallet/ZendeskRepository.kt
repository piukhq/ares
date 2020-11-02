package com.bink.wallet.scenes.loyalty_wallet

import com.bink.wallet.data.SharedPreferenceManager
import com.bink.wallet.utils.LocalStoreUtils
import com.zendesk.service.ErrorResponse
import com.zendesk.service.ZendeskCallback
import zendesk.core.AnonymousIdentity
import zendesk.core.Zendesk
import zendesk.support.RequestUpdates
import zendesk.support.Support

class ZendeskRepository {

    fun hasResponseBeenReceived(): Boolean {
        var responseReceived = false
        val request = Support.INSTANCE.provider()?.requestProvider()

        request?.getUpdatesForDevice(object : ZendeskCallback<RequestUpdates>() {
            override fun onSuccess(p0: RequestUpdates?) {
                responseReceived = p0?.hasUpdatedRequests() ?: false
                SharedPreferenceManager.isResponseAvailable = responseReceived

                if (responseReceived) {
                    SharedPreferenceManager.hasContactUsBeenClicked = false
                }
            }

            override fun onError(p0: ErrorResponse?) {
                responseReceived = false
            }

        })

        return responseReceived
    }

    fun shouldShowUserDetailsDialog(): Boolean {
        var userFirstName = ""
        var userSecondName = ""

        LocalStoreUtils.getAppSharedPref(LocalStoreUtils.KEY_FIRST_NAME)?.let { safeFirstName ->
            userFirstName = safeFirstName
        }

        LocalStoreUtils.getAppSharedPref(LocalStoreUtils.KEY_SECOND_NAME)?.let { safeSecondName ->
            userSecondName = safeSecondName
        }

        return userFirstName.isEmpty() || userSecondName.isEmpty()
    }

    fun setIdentity(firstName: String = "", lastName: String = "") {
        val userFirstName = if (firstName.isEmpty()) getUsersFirstName() else firstName
        val userLastName = if (lastName.isEmpty()) getUsersLastName() else lastName

        val identity = AnonymousIdentity.Builder()
            .withEmailIdentifier(getUserEmail())
            .withNameIdentifier("$userFirstName $userLastName")
            .build()
        Zendesk.INSTANCE.setIdentity(identity)
    }

    private fun getUserEmail(): String {
        var userEmail = ""
        LocalStoreUtils.getAppSharedPref(LocalStoreUtils.KEY_EMAIL)?.let { safeEmail ->
            userEmail = safeEmail
        }

        return userEmail
    }

    private fun getUsersFirstName(): String {
        var userFirstName = ""
        LocalStoreUtils.getAppSharedPref(LocalStoreUtils.KEY_FIRST_NAME)?.let { safeFirstName ->
            userFirstName = safeFirstName
        }

        return userFirstName
    }

    private fun getUsersLastName(): String {
        var userLastName = ""
        LocalStoreUtils.getAppSharedPref(LocalStoreUtils.KEY_SECOND_NAME)?.let { safeLastName ->
            userLastName = safeLastName
        }

        return userLastName
    }



}