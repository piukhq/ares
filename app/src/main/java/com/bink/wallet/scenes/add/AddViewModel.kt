package com.bink.wallet.scenes.add

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.bink.wallet.BaseViewModel
import com.bink.wallet.data.MembershipCardDao
import com.bink.wallet.data.MembershipPlanDao
import com.bink.wallet.model.response.membership_card.MembershipCard
import com.bink.wallet.model.response.membership_plan.MembershipPlan
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * TODO:
 * Refactor for this class needed at some point. We are retrieving data directly from the DAO rather
 * than from the repository like the rest of the viewModels
 */
class AddViewModel(
    private val membershipCardDao: MembershipCardDao,
    private val membershipPlanDao: MembershipPlanDao
) : BaseViewModel() {
    private val _membershipCards = MutableLiveData<List<MembershipCard>>()
    private val _membershipPlans = MutableLiveData<List<MembershipPlan>>()
    val membershipCards: LiveData<List<MembershipCard>>
        get() = _membershipCards
    val membershipPlans: LiveData<List<MembershipPlan>>
        get() = _membershipPlans

    fun getLocalMembershipCards() {
        viewModelScope.launch {
            try {
                val membershipCards = async(Dispatchers.IO) { membershipCardDao.getAllAsync() }
                val membershipPlans = async(Dispatchers.IO) { membershipPlanDao.getAllAsync() }
                _membershipCards.value = membershipCards.await()
                _membershipPlans.value = membershipPlans.await()
            } catch (e: Exception) {

            }

        }
    }
}