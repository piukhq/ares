package com.bink.wallet.scenes.loyalty_details

import androidx.lifecycle.MutableLiveData
import com.bink.wallet.BaseViewModel

class LoyaltyCardDetailsViewModel: BaseViewModel() {
    var tiles = MutableLiveData<List<String>>()

    init {
        val tileItems = ArrayList<String>()
        tileItems.add("bla")
        tiles.value = tileItems
    }
}