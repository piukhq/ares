package com.bink.wallet.scenes.pll

import android.os.Bundle
import androidx.navigation.fragment.findNavController
import com.bink.wallet.BaseFragment
import com.bink.wallet.R
import com.bink.wallet.databinding.FragmentPllEmptyBinding
import com.bink.wallet.modal.generic.GenericModalParameters
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

        arguments.let { bundle ->
            if (bundle != null) {
                currentMembershipCard = PllEmptyFragmentArgs.fromBundle(bundle).membershipCard
                currentMembershipPlan = PllEmptyFragmentArgs.fromBundle(bundle).membershipPlan
            }
        }

        binding.header.setOnClickListener {
            currentMembershipPlan?.account?.plan_description?.let { planDescription ->
                findNavController().navigateIfAdded(
                    this,
                    PllEmptyFragmentDirections.pllEmptyToBrandHeader(
                        GenericModalParameters(
                            R.drawable.ic_close,
                            true,
                            currentMembershipPlan?.account?.plan_name
                                ?: getString(R.string.plan_description),
                            planDescription
                        )
                    )
                )
            }
        }

        currentMembershipPlan?.let {
            binding.membershipPlan = currentMembershipPlan
        }

        binding.buttonDone.setOnClickListener {
            val directions =
                currentMembershipPlan?.let { membershipPlan ->
                    currentMembershipCard?.let { membershipCard ->
                        PllEmptyFragmentDirections.pllEmptyToDetail(
                            membershipPlan, membershipCard
                        )
                    }
                }
            directions?.let { _ -> findNavController().navigateIfAdded(this, directions) }
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
