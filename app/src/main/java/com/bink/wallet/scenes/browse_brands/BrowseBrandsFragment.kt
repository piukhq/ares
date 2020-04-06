package com.bink.wallet.scenes.browse_brands

import android.os.Bundle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bink.wallet.BaseFragment
import com.bink.wallet.R
import com.bink.wallet.databinding.BrowseBrandsFragmentBinding
import com.bink.wallet.model.response.membership_plan.MembershipPlan
import com.bink.wallet.utils.FirebaseEvents.BROWSE_BRANDS_VIEW
import com.bink.wallet.utils.enums.CardType
import com.bink.wallet.utils.navigateIfAdded
import com.bink.wallet.utils.toolbar.FragmentToolbar
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.Locale

class BrowseBrandsFragment : BaseFragment<BrowseBrandsViewModel, BrowseBrandsFragmentBinding>() {
    private val args by navArgs<BrowseBrandsFragmentArgs>()
    override fun builder(): FragmentToolbar {
        return FragmentToolbar.Builder()
            .with(binding.toolbar).shouldDisplayBack(requireActivity())
            .build()
    }

    override val layoutRes: Int
        get() = R.layout.browse_brands_fragment

    override val viewModel: BrowseBrandsViewModel by viewModel()

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
        var plans = args.membershipPlans
        val plansList = mutableListOf<BrowseBrandsListItem>()
        var splitPosition = 0

        if (plans.isNotEmpty()) {
            plans = plans.sortedWith(Comparator<MembershipPlan> { membershipPlan1,
                                                                  membershipPlan2 ->
                comparePlans(membershipPlan1, membershipPlan2)
            }.thenBy { it.account?.company_name?.toLowerCase(Locale.ENGLISH) }).toTypedArray()
            plansList.add(BrowseBrandsListItem.BrandsSectionTitleItem(getString(R.string.pll_title)))
        }

        for (position in 1 until plans.size) {
            if (plans[position - 1].getCardType() == CardType.PLL &&
                plans[position].getCardType() != CardType.PLL
            ) {
                plansList.add(BrowseBrandsListItem.BrandsSectionTitleItem(getString(R.string.all_text)))
                plansList.add(BrowseBrandsListItem.MembershipPlanItem(plans[position]))
                splitPosition = position - 1
            } else {
                plansList.add(BrowseBrandsListItem.MembershipPlanItem(plans[position]))
            }
        }
        binding.browseBrandsContainer.adapter =
            BrowseBrandsAdapter(
                plansList,
                splitPosition,
                itemClickListener = { toAddJoinScreen(it) })
    }

    private fun toAddJoinScreen(membershipPlan: MembershipPlan) {
        val action = BrowseBrandsFragmentDirections.browseToAddJoin(
            membershipPlan,
            null,
            isFromJoinCard = false,
            isRetryJourney = false
        )
        findNavController().navigateIfAdded(this, action)
    }
}
