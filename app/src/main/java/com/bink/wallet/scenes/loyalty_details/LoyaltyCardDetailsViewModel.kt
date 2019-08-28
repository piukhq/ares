package com.bink.wallet.scenes.loyalty_details

import androidx.lifecycle.MutableLiveData
import com.bink.wallet.BaseViewModel
import com.bink.wallet.model.response.membership_plan.MembershipPlan

class LoyaltyCardDetailsViewModel(private val repository: LoyaltyCardDetailsRepository) :
    BaseViewModel() {
    var tiles = MutableLiveData<List<String>>()
    var membershipPlan = MutableLiveData<MembershipPlan>()
    var deletedCard = MutableLiveData<String>()
    var deleteError = MutableLiveData<String>()

    init {
        // TODO replace tiles with real image links
        val tileItems = ArrayList<String>()
        tileItems.add("bla")
        tiles.value = tileItems
    }


    suspend fun deleteCard(id: String?) {
        repository.deleteMembershipCard(id, deletedCard, deleteError)
    }
}