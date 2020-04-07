package com.bink.wallet.scenes.browse_brands

import android.os.Bundle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bink.wallet.BaseFragment
import com.bink.wallet.R
import com.bink.wallet.databinding.BrowseBrandsFragmentBinding
import com.bink.wallet.model.response.membership_card.MembershipCard
import com.bink.wallet.model.response.membership_plan.MembershipPlan
import com.bink.wallet.utils.FirebaseEvents.BROWSE_BRANDS_VIEW
import com.bink.wallet.utils.enums.CardType
import com.bink.wallet.utils.toolbar.FragmentToolbar
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.Locale

class BrowseBrandsFragment : BaseFragment<BrowseBrandsViewModel, BrowseBrandsFragmentBinding>() {

    private val args by navArgs<BrowseBrandsFragmentArgs>()
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

    private fun isPlanPLL(membershipPlan: MembershipPlan): Boolean {
        return membershipPlan.getCardType() == CardType.PLL
    }

    private fun comparePlans(
        membershipPlan1: MembershipPlan,
        membershipPlan2: MembershipPlan
    ): Int {
        membershipPlan1.getCardType()?.type?.let { type1 ->
            membershipPlan2.getCardType()?.type?.let { type2 ->
                return when {
                    (isPlanPLL(membershipPlan1) ||
                            isPlanPLL(membershipPlan2)) &&
                            (type1 > type2) -> -1
                    (isPlanPLL(membershipPlan1) ||
                            isPlanPLL(membershipPlan2)) &&
                            (type1 < type2) -> 1
                    else -> 0
                }
            }
        }
        return 0
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        binding.lifecycleOwner = this
        setupBrandsAdapter(
            args.membershipCards.toList().getOwnedMembershipCardsIds(),
            args.membershipPlans.toList()
        )
    }

    private fun setupBrandsAdapter(
        membershipCardIds: List<String>,
        membershipPlans: List<MembershipPlan>
    ) {
        val browseBrandsItemsList = mutableListOf<BrowseBrandsListItem>()
        var splitPosition = 0

        val sortedMembershipPlans =
            membershipPlans.sortedWith(
                Comparator<MembershipPlan> { membershipPlan1, membershipPlan2 ->
                    comparePlans(membershipPlan1, membershipPlan2)
                }.thenBy {
                    it.account?.company_name?.toLowerCase(Locale.ENGLISH)
                }
            ).toTypedArray()
        browseBrandsItemsList.add(BrowseBrandsListItem.SectionTitleItem(getString(R.string.pll_title)))
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
                browseBrandsItemsList.add(
                    BrowseBrandsListItem.SectionTitleItem(
                        getString(R.string.all_text)
                    )
                )
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
        binding.brandsRecyclerView.adapter = BrowseBrandsAdapter(
            browseBrandsItemsList,
            splitPosition
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
    }
}
