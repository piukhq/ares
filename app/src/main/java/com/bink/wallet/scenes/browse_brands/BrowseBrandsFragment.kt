package com.bink.wallet.scenes.browse_brands

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bink.wallet.BaseFragment
import com.bink.wallet.BrowseBrandsBinding
import com.bink.wallet.R
import com.bink.wallet.model.response.membership_card.MembershipCard
import com.bink.wallet.model.response.membership_plan.MembershipPlan
import com.bink.wallet.utils.EMPTY_STRING
import com.bink.wallet.utils.FirebaseEvents.BROWSE_BRANDS_VIEW
import com.bink.wallet.utils.setVisible
import com.bink.wallet.utils.toolbar.FragmentToolbar
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.Locale

class BrowseBrandsFragment : BaseFragment<BrowseBrandsViewModel, BrowseBrandsBinding>() {

    private val args by navArgs<BrowseBrandsFragmentArgs>()
    private lateinit var adapter: BrowseBrandsAdapter
    private val filtersAdapter = BrandsFiltersAdapter()
    private var membershipCardIds = listOf<String>()
    override val layoutRes = R.layout.browse_brands_fragment
    override val viewModel: BrowseBrandsViewModel by viewModel()

    override fun builder(): FragmentToolbar {
        return FragmentToolbar.Builder()
            .with(binding.toolbar).shouldDisplayBack(requireActivity())
            .build()
    }

    override fun onResume() {
        super.onResume()
        logScreenView(BROWSE_BRANDS_VIEW)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        binding.viewModel = viewModel

        adapter = BrowseBrandsAdapter().apply {
            setOnBrandItemClickListener { membershipPlan ->
                findNavController().navigate(
                    BrowseBrandsFragmentDirections.browseToAddJoin(
                        membershipPlan,
                        null,
                        isFromJoinCard = false,
                        isRetryJourney = false
                    )
                )
            }
        }
        binding.brandsRecyclerView.adapter = adapter
        binding.filtersList.adapter = filtersAdapter.apply {
            setOnFilterClickListener {
                Log.d("WRKR", it.toString())
            }
            setFilters(args.membershipPlans.toList().getCategories().map { BrandsFilter(it) })
        }
        binding.filtersList.layoutManager = GridLayoutManager(context, 2)
        membershipCardIds = args.membershipCards.toList().getOwnedMembershipCardsIds()
        adapter.submitList(
            formatBrandItemsList(args.membershipPlans.toList().sortedByCardTypeAndCompany())
        )

        binding.buttonClearSearch.setOnClickListener {
            viewModel.searchText.value = EMPTY_STRING
        }

        binding.buttonFilters.setOnClickListener {
            binding.filtersList.setVisible(binding.filtersList.visibility != View.VISIBLE)
        }

        adapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                binding.brandsRecyclerView.scrollToPosition(0)
            }
        })
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lifecycleOwner = this

        viewModel.searchText.observe(viewLifecycleOwner, Observer { searchQuery ->
            val filteredBrands = filterBrands(
                args.membershipPlans.toList(),
                searchQuery
            ).sortedByCardTypeAndCompany()
            adapter.submitList(formatBrandItemsList(filteredBrands))
//            binding.brandsRecyclerView.setVisible(!filteredBrands.isNullOrEmpty())
            binding.labelNoMatch.setVisible(filteredBrands.isNullOrEmpty())
        })
    }

    private fun filterBrands(
        membershipPlans: List<MembershipPlan>,
        searchQuery: String
    ): List<MembershipPlan> {
        return if (searchQuery != EMPTY_STRING) {
            val searchList = mutableListOf<MembershipPlan>()
            membershipPlans.forEach {
                if (it.account?.company_name?.toLowerCase(Locale.ENGLISH)
                        ?.contains(searchQuery.toLowerCase(Locale.ENGLISH)) == true
                ) {
                    searchList.add(it)
                }
            }
            searchList
        } else {
            membershipPlans
        }
    }

    private fun formatBrandItemsList(membershipPlans: List<MembershipPlan>): List<BrowseBrandsListItem> {
        val browseBrandsItems = mutableListOf<BrowseBrandsListItem>()
        var hasAllSubtitle = false

        if (membershipPlans.firstOrNull()?.isPlanPLL() == true) {
            browseBrandsItems.add(BrowseBrandsListItem.SectionTitleItem(getString(R.string.pll_title)))
        }
        membershipPlans.forEachIndexed { index, membershipPlan ->
            val isLast = index == membershipPlans.size - 1
            var isBeforeSectionTitle = false

            if (!membershipPlan.isPlanPLL() && !hasAllSubtitle) {
                browseBrandsItems.add(BrowseBrandsListItem.SectionTitleItem(getString(R.string.all_text)))
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

    companion object {
        private fun List<MembershipCard>.getOwnedMembershipCardsIds(): List<String> {
            val membershipCardsIds = mutableListOf<String>()
            this.forEach { membershipCard ->
                membershipCard.membership_plan?.let { membership_planId ->
                    if (membership_planId !in membershipCardsIds) {
                        membershipCardsIds.add(membership_planId)
                    }
                }
            }
            return membershipCardsIds
        }

        private fun List<MembershipPlan>.sortedByCardTypeAndCompany(): List<MembershipPlan> =
            this.sortedWith(
                Comparator<MembershipPlan> { membershipPlan1, membershipPlan2 ->
                    membershipPlan1.comparePlans(membershipPlan2)
                }.thenBy {
                    it.account?.company_name?.toLowerCase(Locale.ENGLISH)
                }
            )

        private fun List<MembershipPlan>.getCategories(): List<String> {
            val categories = mutableListOf<String>()
            this.forEach { membershipPlan ->
                membershipPlan.account?.category?.let { category ->
                    if (category !in categories) {
                        categories.add(category)
                    }
                }
            }
            return categories
        }
    }
}
