package com.bink.wallet.utils

import androidx.fragment.app.Fragment
import zendesk.core.AnonymousIdentity
import zendesk.core.Zendesk
import zendesk.support.requestlist.RequestListActivity

fun Fragment.setZendeskIdentity(email:String, firstName:String, lastName:String){
    val identity = AnonymousIdentity.Builder()
        .withEmailIdentifier(email)
        .withNameIdentifier("$firstName $lastName")
        .build()
    Zendesk.INSTANCE.setIdentity(identity)

}

fun Fragment.goToContactUsForm(){
    RequestListActivity.builder()
        .show(requireActivity())
}