package com.bink.wallet.scenes.add_auth_enrol

import androidx.navigation.fragment.findNavController
import com.bink.wallet.R
import com.bink.wallet.modal.generic.GenericModalParameters
import com.bink.wallet.model.response.membership_card.MembershipCard
import com.bink.wallet.model.response.membership_plan.MembershipPlan
import com.bink.wallet.scenes.add_auth_enrol.screens.AddCardFragmentDirections
import com.bink.wallet.scenes.add_auth_enrol.screens.BaseAddAuthFragment
import com.bink.wallet.scenes.add_auth_enrol.screens.BaseAddAuthFragmentDirections
import com.bink.wallet.utils.navigateIfAdded

class AuthNavigationHandler(
    val fragment: BaseAddAuthFragment,
    val membershipPlan: MembershipPlan?
) {

    private val context = fragment.requireContext()

    fun navigateToBrandHeader() {
        membershipPlan.let { plan ->
            if (plan?.account?.plan_description != null) {
                fragment.findNavController().navigateIfAdded(
                    fragment,
                    BaseAddAuthFragmentDirections.baseAddAuthToBrandHeader(
                        GenericModalParameters(
                            R.drawable.ic_close,
                            true,
                            plan.account.plan_name ?: context.getString(R.string.plan_description),
                             plan.account.plan_summary?:"",
                            description2 = plan.account.plan_description,
                            firstButtonText = context.getString(R.string.go_to_site)
                        ), plan.account.plan_url ?: ""
                    )
                )
            } else if (plan?.account?.plan_name_card != null) {
                plan.account.plan_name?.let { planName ->
                    fragment.findNavController().navigateIfAdded(
                        fragment,
                        BaseAddAuthFragmentDirections.baseAddAuthToBrandHeader(
                            GenericModalParameters(
                                R.drawable.ic_close,
                                true,
                                planName,
                                firstButtonText = context.getString(R.string.go_to_site)
                            ), plan.account.plan_url ?: ""
                        )
                    )
                }
            }
        }
    }

    fun navigateToPllEmpty(membershipCard: MembershipCard) {
        membershipPlan?.let {
            fragment.findNavController().navigateIfAdded(
                fragment,
                BaseAddAuthFragmentDirections.authToPllEmpty(
                    it,
                    membershipCard,
                    false
                )
            )
        }
    }

    fun navigateToPll(membershipCard: MembershipCard) {
        membershipPlan?.let {
            fragment.findNavController().navigateIfAdded(
                fragment,
                BaseAddAuthFragmentDirections.authToPll(
                    membershipCard,
                    it,
                    true
                )
            )
        }
    }

    fun navigateToLCD(membershipCard: MembershipCard) {
        membershipPlan?.let {
            fragment.findNavController().navigateIfAdded(
                fragment,
                BaseAddAuthFragmentDirections.authToLoyaltyDetails(
                    it,
                    membershipCard
                )
            )
        }
    }

}