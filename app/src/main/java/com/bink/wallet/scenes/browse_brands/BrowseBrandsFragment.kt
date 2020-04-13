package com.bink.wallet.scenes.browse_brands

import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bink.wallet.BaseFragment
import com.bink.wallet.BrowseBrandsBinding
import com.bink.wallet.R
import com.bink.wallet.model.response.membership_card.MembershipCard
import com.bink.wallet.model.response.membership_plan.MembershipPlan
import com.bink.wallet.utils.EMPTY_STRING
import com.bink.wallet.utils.FirebaseEvents.BROWSE_BRANDS_VIEW
import com.bink.wallet.utils.enums.CardType
import com.bink.wallet.utils.toolbar.FragmentToolbar
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.Locale

class BrowseBrandsFragment : BaseFragment<BrowseBrandsViewModel, BrowseBrandsBinding>() {

    private val args by navArgs<BrowseBrandsFragmentArgs>()
    private lateinit var adapter: BrowseBrandsAdapter
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

        adapter = BrowseBrandsAdapter(
            listOf(),
            0
        ).apply {
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
        membershipCardIds = args.membershipCards.toList().getOwnedMembershipCardsIds()
        setupBrandsAdapter(
            args.membershipPlans.toList(),
            true
        )

        binding.buttonClearSearch.setOnClickListener {
            binding.inputSearch.setText("")
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lifecycleOwner = this

        viewModel.searchText.observe(viewLifecycleOwner, Observer { searchQuery ->
            filterBrands(args.membershipPlans.toList(), searchQuery)
        })
    }

    private fun setupBrandsAdapter(
        membershipPlans: List<MembershipPlan>,
        hasSectionTitles: Boolean
    ) {
        val browseBrandsItemsList = mutableListOf<BrowseBrandsListItem>()
        var splitPosition = 0
        val sortedMembershipPlans = membershipPlans.getSortedMembershipPlans()

        if (hasSectionTitles) {
            browseBrandsItemsList.add(BrowseBrandsListItem.SectionTitleItem(getString(R.string.pll_title)))
        }
        browseBrandsItemsList.add(
            BrowseBrandsListItem.BrandItem(
                sortedMembershipPlans[0],
                sortedMembershipPlans[0].id in membershipCardIds
            )
        )

        for (position in 1 until sortedMembershipPlans.size) {
            if (sortedMembershipPlans[position - 1].getCardType() == CardType.PLL &&
                sortedMembershipPlans[position].getCardType() != CardType.PLL
            ) {
                if (hasSectionTitles) {
                    browseBrandsItemsList.add(BrowseBrandsListItem.SectionTitleItem(getString(R.string.all_text)))
                }
                browseBrandsItemsList.add(
                    BrowseBrandsListItem.BrandItem(
                        sortedMembershipPlans[position],
                        sortedMembershipPlans[position].id in membershipCardIds
                    )
                )
                splitPosition = position
            } else {
                browseBrandsItemsList.add(
                    BrowseBrandsListItem.BrandItem(
                        sortedMembershipPlans[position],
                        sortedMembershipPlans[position].id in membershipCardIds
                    )
                )
            }
        }
        adapter.setSearchResultList(browseBrandsItemsList, splitPosition)
    }

    private fun filterBrands(membershipPlans: List<MembershipPlan>, searchQuery: String) {
        if (searchQuery != EMPTY_STRING) {
            val searchList = mutableListOf<MembershipPlan>()
            membershipPlans.forEach {
                if (it.account?.company_name?.toLowerCase(Locale.ENGLISH)
                        ?.contains(searchQuery.toLowerCase(Locale.ENGLISH)) == true
                ) {
                    searchList.add(it)
                }
            }
            if (!searchList.isNullOrEmpty()) {
                binding.labelNoMatch.visibility = View.GONE
                setupBrandsAdapter(
                    searchList,
                    false
                )
            } else {
                binding.labelNoMatch.visibility = View.VISIBLE
                adapter.setSearchResultList(listOf())
            }
        } else {
            binding.labelNoMatch.visibility = View.GONE
            setupBrandsAdapter(
                args.membershipPlans.toList(),
                true
            )
        }
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

        private fun List<MembershipPlan>.getSortedMembershipPlans(): List<MembershipPlan> =
            this.sortedWith(
                Comparator<MembershipPlan> { membershipPlan1, membershipPlan2 ->
                    membershipPlan1.comparePlans(membershipPlan2)
                }.thenBy {
                    it.account?.company_name?.toLowerCase(Locale.ENGLISH)
                }
            )
    }
}
