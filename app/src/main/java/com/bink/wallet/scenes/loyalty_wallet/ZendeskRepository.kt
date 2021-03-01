package com.bink.wallet.scenes.loyalty_wallet

import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.bink.wallet.R
import com.bink.wallet.data.SharedPreferenceManager
import com.bink.wallet.model.auth.User
import com.bink.wallet.utils.LocalStoreUtils
import com.zendesk.service.ErrorResponse
import com.zendesk.service.ZendeskCallback
import zendesk.core.AnonymousIdentity
import zendesk.core.Zendesk
import zendesk.support.RequestUpdates
import zendesk.support.Support
import zendesk.support.requestlist.RequestListActivity

class ZendeskRepository {

    fun launchZendesk(fragment: Fragment, callbackUser: (User) -> Unit) {

        setIdentity(getUsersFirstName(), getUsersLastName())

        if (shouldShowUserDetailsDialog()) {
            buildAndShowUserDetailsDialog(fragment, callbackUser)
        } else {
            fragment.context?.let { context ->
                RequestListActivity.builder()
                    .show(context)
            }
        }
    }

    private fun buildAndShowUserDetailsDialog(fragment: Fragment, callbackUser: (User) -> Unit) {
        val dialog: AlertDialog
        fragment.context?.let { context ->
        val builder = AlertDialog.Builder(context)
            builder.setTitle(fragment.getString(R.string.zendesk_user_details_prompt_title))
            val container = fragment.layoutInflater.inflate(R.layout.layout_zendesk_user_details, null)
            val etFirstName = container.findViewById<EditText>(R.id.et_first_name)
            val etSecondName = container.findViewById<EditText>(R.id.et_last_name)
            builder.setView(container)
                .setPositiveButton(
                    fragment.getString(R.string.zendesk_user_details_prompt_cta), null
                )
                .setNegativeButton(fragment.getString(android.R.string.cancel)) { dialog, _ ->
                    dialog.cancel()
                }
            dialog = builder.create()
            dialog.show()
            dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                .setOnClickListener {
                    if (etFirstName.text.isNotEmpty() && etSecondName.text.isNotEmpty()) {
                        setIdentity(etFirstName.text.toString(), etSecondName.text.toString())
                        callbackUser(
                            User(
                                etFirstName.text.toString(),
                                etSecondName.text.toString()
                            )
                        )

                        RequestListActivity.builder()
                            .show(context)

                        dialog.dismiss()
                    }
                }
        }
    }

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
        return getUsersFirstName().isEmpty() || getUsersLastName().isEmpty()
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