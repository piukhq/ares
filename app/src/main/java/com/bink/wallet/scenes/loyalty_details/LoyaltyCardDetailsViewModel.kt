package com.bink.wallet.scenes.loyalty_details

import androidx.lifecycle.MutableLiveData
import com.bink.wallet.BaseViewModel
import com.bink.wallet.scenes.browse_brands.model.MembershipPlan

class LoyaltyCardDetailsViewModel: BaseViewModel() {
    var tiles = MutableLiveData<List<String>>()
    var membershipPlan = MutableLiveData<MembershipPlan>()

    init {
        val tileItems = ArrayList<String>()
        tileItems.add("bla")
        tiles.value = tileItems
    }
}