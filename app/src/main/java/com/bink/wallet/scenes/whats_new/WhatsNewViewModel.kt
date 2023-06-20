package com.bink.wallet.scenes.whats_new

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.bink.wallet.BaseViewModel
import com.bink.wallet.data.DataStoreSourceImpl
import com.bink.wallet.model.WhatsNew
import com.bink.wallet.model.response.membership_card.MembershipCard
import com.bink.wallet.model.response.membership_plan.MembershipPlan
import com.bink.wallet.utils.ThemeHelper
import com.bink.wallet.utils.getIconTypeFromPlan
import kotlinx.coroutines.launch

class WhatsNewViewModel(private val dataStoreSource: DataStoreSourceImpl) : BaseViewModel() {

    var whatsNew = MutableLiveData<WhatsNew>()

    private lateinit var membershipPlans: Array<MembershipPlan>
    private lateinit var membershipCards: Array<MembershipCard>

    private val _theme = mutableStateOf(ThemeHelper.SYSTEM)
    val theme: MutableState<String>
        get() = _theme

    init {
        getSelectedTheme()
    }

    fun getFeaturedCards(plans: Array<MembershipPlan>, cards: Array<MembershipCard>) {
        membershipPlans = plans
        membershipCards = cards

        whatsNew.value?.merchants?.forEachIndexed { index, newMerchant ->
            val membershipPlan = plans.firstOrNull { it.id == newMerchant.membershipPlanId }
            whatsNew.value?.merchants!![index].membershipPlan = membershipPlan
        }
    }

    fun getPlansAndCards(): Pair<Array<MembershipPlan>, Array<MembershipCard>> {
        return Pair(membershipPlans, membershipCards)
    }

    private fun getSelectedTheme() {
        viewModelScope.launch {
            dataStoreSource.getCurrentlySelectedTheme().collect {
                _theme.value = it
            }
        }
    }
}