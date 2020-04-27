package com.bink.wallet.scenes.add

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.bink.wallet.BaseViewModel
import com.bink.wallet.data.MembershipCardDao
import com.bink.wallet.data.MembershipPlanDao
import com.bink.wallet.model.response.membership_card.MembershipCard
import com.bink.wallet.model.response.membership_plan.MembershipPlan
import kotlinx.coroutines.launch

class AddViewModel(
    private val membershipCardDao: MembershipCardDao,
    private val membershipPlansDao: MembershipPlanDao
) : BaseViewModel() {

    private val _membershipCards = MutableLiveData<List<MembershipCard>>()
    val membershipCards: LiveData<List<MembershipCard>>
        get() = _membershipCards

    private val _membershipPlans = MutableLiveData<List<MembershipPlan>>()
    val membershipPlans: LiveData<List<MembershipPlan>>
        get() = _membershipPlans

    fun getLocalMembershipCards() {
        viewModelScope.launch {
            _membershipCards.value = membershipCardDao.getAllAsync()
        }
    }

    fun getLocalMembershipPlans() {
        viewModelScope.launch {
            _membershipPlans.value = membershipPlansDao.getAllAsync()
        }
    }
}