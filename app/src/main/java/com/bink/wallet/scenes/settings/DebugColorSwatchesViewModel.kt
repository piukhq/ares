package com.bink.wallet.scenes.settings

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.bink.wallet.BaseViewModel
import com.bink.wallet.data.MembershipPlanDao
import com.bink.wallet.model.response.membership_plan.MembershipPlan
import kotlinx.coroutines.launch

class DebugColorSwatchesViewModel(private val membershipPlanDao: MembershipPlanDao) :
    BaseViewModel() {

    private val _membershipPlans = MutableLiveData<List<MembershipPlan>>()
    val membershipPlans: LiveData<List<MembershipPlan>>
        get() = _membershipPlans

    fun getLocalMembershipCards() {
        viewModelScope.launch {
            _membershipPlans.value = membershipPlanDao.getAllAsync()
        }
    }

}