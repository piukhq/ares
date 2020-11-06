package com.bink.wallet.utils

import androidx.fragment.app.Fragment
import zendesk.support.requestlist.RequestListActivity


fun Fragment.goToContactUsForm(){
    RequestListActivity.builder()
        .show(requireActivity())
}