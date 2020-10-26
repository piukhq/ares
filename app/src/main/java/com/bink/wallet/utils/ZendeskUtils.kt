package com.bink.wallet.utils

import com.bink.wallet.data.SharedPreferenceManager
import com.zendesk.service.ErrorResponse
import com.zendesk.service.ZendeskCallback
import zendesk.support.RequestUpdates
import zendesk.support.Support

object ZendeskUtils {

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
}