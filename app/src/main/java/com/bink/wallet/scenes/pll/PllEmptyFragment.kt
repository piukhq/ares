package com.bink.wallet.scenes.pll

import android.os.Bundle
import androidx.navigation.fragment.findNavController
import com.bink.wallet.BaseFragment
import com.bink.wallet.R
import com.bink.wallet.databinding.FragmentPllEmptyBinding
import com.bink.wallet.model.response.membership_card.MembershipCard
import com.bink.wallet.model.response.membership_plan.MembershipPlan
import com.bink.wallet.utils.displayModalPopup
import com.bink.wallet.utils.navigateIfAdded
import com.bink.wallet.utils.toolbar.FragmentToolbar
import org.koin.androidx.viewmodel.ext.android.viewModel

class PllEmptyFragment : BaseFragment<PllEmptyViewModel, FragmentPllEmptyBinding>() {
    override fun builder(): FragmentToolbar {
        return FragmentToolbar.Builder()
            .withId(FragmentToolbar.NO_TOOLBAR)
            .build()
    }

    override val layoutRes: Int
        get() = R.layout.fragment_pll_empty

    private var currentMembershipCard: MembershipCard? = null
    private var currentMembershipPlan: MembershipPlan? = null

    override val viewModel: PllEmptyViewModel by viewModel()

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        arguments.let {
            if (it?.let { it1 ->
                    PllEmptyFragmentArgs.fromBundle(it1).membershipCard
                } != null) {
                currentMembershipCard =
                    it.let { it }.let { it1 ->
                        PllEmptyFragmentArgs.fromBundle(it1).membershipCard
                    }
                currentMembershipPlan =
                    it.let { it }.let { it1 -> PllEmptyFragmentArgs.fromBundle(it1).membershipPlan }
            }

            binding.membershipPlan = currentMembershipPlan

            binding.buttonDone.setOnClickListener {
                val directions =
                    currentMembershipCard?.let { membershipCard ->
                        currentMembershipPlan?.let { membershipPlan ->
                            PllEmptyFragmentDirections.pllEmptyToDetail(
                                membershipPlan, membershipCard
                            )
                        }
                    }
                directions?.let { it1 -> findNavController().navigateIfAdded(this, it1) }
            }

            binding.buttonAddPaymentCard.setOnClickListener {

                //TODO PCD is not implemented yet, for moment a dialog is displayed -> AB20-35(CTA change)

                context?.displayModalPopup(
                    getString(R.string.missing_destination_dialog_title),
                    getString(R.string.not_implemented_yet_text)
                )
            }
        }
    }
}
