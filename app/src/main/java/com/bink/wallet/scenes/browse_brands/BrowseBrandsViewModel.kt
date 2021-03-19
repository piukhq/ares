package com.bink.wallet.scenes.browse_brands

import androidx.databinding.ObservableBoolean
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.bink.wallet.BaseViewModel
import com.bink.wallet.R
import com.bink.wallet.model.response.membership_plan.MembershipPlan
import com.bink.wallet.utils.EMPTY_STRING
import com.bink.wallet.utils.LocalPointScraping.WebScrapableManager
import com.bink.wallet.utils.getCategories
import com.bink.wallet.utils.logDebug
import com.bink.wallet.utils.sortedByCardTypeAndCompany
import java.util.*
import kotlin.system.measureTimeMillis

class BrowseBrandsViewModel : BaseViewModel() {
    private val membershipPlans = MutableLiveData<List<MembershipPlan>>()
    private val membershipCardIds = MutableLiveData<List<String>>()

    private val _filteredBrandItems = MutableLiveData<List<BrowseBrandsListItem>>()
    val filteredBrandItems: LiveData<List<BrowseBrandsListItem>>
        get() = _filteredBrandItems

    private val _activeFilters = MutableLiveData<List<String>>()
    val activeFilters: LiveData<List<String>>
        get() = _activeFilters

    val searchText = MutableLiveData<String>()
    val isClearButtonVisible: LiveData<Boolean> = Transformations.map(searchText) {
        !searchText.value.isNullOrEmpty()
    }

    val isFilterSelected = ObservableBoolean(false)

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
        _activeFilters.value = membershipPlans.getCategories()
    }

    fun filterBrandItems() {
        val currentBrandItems = membershipPlans.value?.toMutableList() ?: mutableListOf()
        val filteredBrandItems = mutableListOf<MembershipPlan>()
        val searchQuery = searchText.value ?: EMPTY_STRING
        val filters = _activeFilters.value ?: listOf()
        currentBrandItems.forEach {
            if (it.account?.category in filters) {
                filteredBrandItems.add(it)
            }
        }
        if (searchQuery != EMPTY_STRING) {
            val searchList = mutableListOf<MembershipPlan>()
            filteredBrandItems.forEach {
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
                    formatBrandItemsList(filteredBrandItems.sortedByCardTypeAndCompany(), it)
            }
        }
    }

    private fun formatBrandItemsList(
        membershipPlans: List<MembershipPlan>,
        membershipCardIds: List<String>
    ): List<BrowseBrandsListItem> {
        val browseBrandsItems = mutableListOf<BrowseBrandsListItem>()
        var hasAllSubtitle = false

        val (pllCards, RestOfCards) = membershipPlans.partition { it.isPlanPLL() }

        val (scrapableCards, storeCards) = RestOfCards.distinctBy { it.id }
            .partition { WebScrapableManager.canBeScraped(it.id) }

        if (membershipPlans.firstOrNull()?.isPlanPLL() == true) {
            browseBrandsItems.add(BrowseBrandsListItem.SectionTitleItem(R.string.pll_title,R.string.pll_browse_brands_description))
        }

        pllCards.forEachIndexed { index, membershipPlan ->
            browseBrandsItems.add(
                BrowseBrandsListItem.BrandItem(
                    membershipPlan,
                    membershipPlan.id in membershipCardIds,
                    index != pllCards.size - 1
                )
            )
        }

        browseBrandsItems.add(BrowseBrandsListItem.SectionTitleItem(R.string.balance_text,R.string.balance_description))

        scrapableCards.forEachIndexed { index, membershipPlan ->
            browseBrandsItems.add(
                BrowseBrandsListItem.BrandItem(
                    membershipPlan,
                    membershipPlan.id in membershipCardIds,
                    index != scrapableCards.size - 1
                )
            )
        }

        browseBrandsItems.add(BrowseBrandsListItem.SectionTitleItem(R.string.store_barcode,R.string.store_barcode_description))

        storeCards.forEachIndexed { index, membershipPlan ->
            browseBrandsItems.add(
                BrowseBrandsListItem.BrandItem(
                    membershipPlan,
                    membershipPlan.id in membershipCardIds,
                    index != storeCards.size - 1
                )
            )
        }


//        membershipPlans.forEachIndexed { index, membershipPlan ->
//            val isLast = index == membershipPlans.size - 1
//            var isBeforeSectionTitle = false
//
//            if (!membershipPlan.isPlanPLL() && !hasAllSubtitle) {
//                browseBrandsItems.add(BrowseBrandsListItem.SectionTitleItem(R.string.all_text))
//                hasAllSubtitle = true
//            }
//            if (!isLast && !membershipPlans[index + 1].isPlanPLL() && !hasAllSubtitle) {
//                isBeforeSectionTitle = true
//            }
//            browseBrandsItems.add(
//                BrowseBrandsListItem.BrandItem(
//                    membershipPlan,
//                    membershipPlan.id in membershipCardIds,
//                    !isLast && !isBeforeSectionTitle
//                )
//            )
//        }
        return browseBrandsItems
    }

    fun updateFilters(brandFilter: BrandsFilter) {
        val filters = _activeFilters.value?.toMutableList() ?: mutableListOf()
        if (brandFilter.isChecked) {
            filters.add(brandFilter.category)
        } else {
            filters.remove(brandFilter.category)
        }
        _activeFilters.value = filters
    }
}
