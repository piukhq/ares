package com.bink.wallet.scenes.browse_brands

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.bink.wallet.scenes.browse_brands.model.MembershipPlan

class BrowseBrandsViewModel(private val browseBrandsRepository: BrowseBrandsRepository) : ViewModel() {
    var membershipPlanData: MutableLiveData<List<MembershipPlan>> = MutableLiveData()

    fun fetchMembershipPlans() {
        membershipPlanData = browseBrandsRepository.fetchMembershipPlans()
    }
}
