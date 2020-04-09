package com.bink.wallet.scenes.add

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.bink.wallet.BaseViewModel
import com.bink.wallet.data.MembershipCardDao
import com.bink.wallet.model.response.membership_card.MembershipCard
import kotlinx.coroutines.launch

class AddViewModel(private val membershipCardDao: MembershipCardDao) : BaseViewModel() {
    private val _membershipCards = MutableLiveData<List<MembershipCard>>()
    val membershipCards: LiveData<List<MembershipCard>>
    get() = _membershipCards

    fun getLocalMembershipCards() {
        viewModelScope.launch {
            _membershipCards.value = membershipCardDao.getAllAsync()
        }
    }
}