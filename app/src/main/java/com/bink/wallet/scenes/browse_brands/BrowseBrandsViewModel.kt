package com.bink.wallet.scenes.browse_brands

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.bink.wallet.BaseViewModel
import com.bink.wallet.R
import com.bink.wallet.model.response.membership_plan.MembershipPlan
import com.bink.wallet.utils.EMPTY_STRING
import com.bink.wallet.utils.sortedByCardTypeAndCompany
import java.util.Locale

class BrowseBrandsViewModel : BaseViewModel() {
    private val membershipPlans = MutableLiveData<List<MembershipPlan>>()
    private val membershipCardIds = MutableLiveData<List<String>>()

    private val _filteredBrandItems = MutableLiveData<List<BrowseBrandsListItem>>()
    val filteredBrandItems: LiveData<List<BrowseBrandsListItem>>
        get() = _filteredBrandItems

    private val _activeFilters = MutableLiveData<String>()
    val activeFilters: LiveData<String>
        get() = _activeFilters

    val searchText = MutableLiveData<String>()
    val isClearButtonVisible: LiveData<Boolean> = Transformations.map(searchText) {
        !searchText.value.isNullOrEmpty()
    }

    fun setupBrandItems(
        membershipPlans: List<MembershipPlan>,
        membershipCardIds: List<String>
    ) {
        val formattedBrandItems = formatBrandItemsList(
            membershipPlans.sortedByCardTypeAndCompany(),
            membershipCardIds
        )
        this.membershipPlans.value = membershipPlans
        this.membershipCardIds.value = membershipCardIds
        _filteredBrandItems.value = formattedBrandItems
    }

    fun filterBrandItems(searchQuery: String, filters: List<String> = listOf()) {
        val currentBrandItems = membershipPlans.value?.toMutableList() ?: mutableListOf()
        if (searchQuery != EMPTY_STRING) {
            val searchList = mutableListOf<MembershipPlan>()
            currentBrandItems.forEach {
                if (it.account?.company_name?.toLowerCase(Locale.ENGLISH)
                        ?.contains(searchQuery.toLowerCase(Locale.ENGLISH)) == true
                ) {
                    searchList.add(it)
                }
            }
            membershipCardIds.value?.let {
                _filteredBrandItems.value =
                    formatBrandItemsList(searchList.sortedByCardTypeAndCompany(), it)
            }
        } else {
            membershipCardIds.value?.let {
                _filteredBrandItems.value =
                    formatBrandItemsList(currentBrandItems.sortedByCardTypeAndCompany(), it)
            }
        }
    }

    private fun formatBrandItemsList(
        membershipPlans: List<MembershipPlan>,
        membershipCardIds: List<String>
    ): List<BrowseBrandsListItem> {
        val browseBrandsItems = mutableListOf<BrowseBrandsListItem>()
        var hasAllSubtitle = false

        if (membershipPlans.firstOrNull()?.isPlanPLL() == true) {
            browseBrandsItems.add(BrowseBrandsListItem.SectionTitleItem(R.string.pll_title))
        }
        membershipPlans.forEachIndexed { index, membershipPlan ->
            val isLast = index == membershipPlans.size - 1
            var isBeforeSectionTitle = false

            if (!membershipPlan.isPlanPLL() && !hasAllSubtitle) {
                browseBrandsItems.add(BrowseBrandsListItem.SectionTitleItem(R.string.all_text))
                hasAllSubtitle = true
            }
            if (!isLast && !membershipPlans[index + 1].isPlanPLL() && !hasAllSubtitle) {
                isBeforeSectionTitle = true
            }
            browseBrandsItems.add(
                BrowseBrandsListItem.BrandItem(
                    membershipPlan,
                    membershipPlan.id in membershipCardIds,
                    !isLast && !isBeforeSectionTitle
                )
            )
        }
        return browseBrandsItems
    }
}
