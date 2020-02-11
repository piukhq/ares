package com.bink.wallet.scenes.pll

import android.os.Bundle
import androidx.navigation.fragment.findNavController
import com.bink.wallet.BaseFragment
import com.bink.wallet.R
import com.bink.wallet.databinding.FragmentPllEmptyBinding
import com.bink.wallet.modal.generic.GenericModalParameters
import com.bink.wallet.model.response.membership_card.MembershipCard
import com.bink.wallet.model.response.membership_plan.MembershipPlan
import com.bink.wallet.utils.FirebaseUtils.ADD_PAYMENT_CARDS_ANALYTICS_IDENTIFIER
import com.bink.wallet.utils.FirebaseUtils.DONE_ANALYTICS_IDENTIFIER_PLL_EMPTY
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

        binding.viewModel = viewModel

        arguments.let { bundle ->
            if (bundle != null) {
                PllEmptyFragmentArgs.fromBundle(bundle).apply {
                    currentMembershipCard = membershipCard
                    currentMembershipPlan = membershipPlan
                    viewModel.isLCDJourney.set(isLCDJourney)
                }
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
            binding.membershipPlan = it
        }

        binding.back.setOnClickListener {
            navigateToLCDScreen()
        }

        binding.buttonDone.setOnClickListener {
            navigateToLCDScreen()

            logEvent(DONE_ANALYTICS_IDENTIFIER_PLL_EMPTY)
        }

        binding.buttonAddPaymentCardNonModal.setOnClickListener {
            navigateToAddPaymentCards()

            logEvent(ADD_PAYMENT_CARDS_ANALYTICS_IDENTIFIER)
        }

        binding.addPaymentCardModal.setOnClickListener {
            navigateToAddPaymentCards()
        }
    }

    private fun navigateToAddPaymentCards() {
        val directions = PllEmptyFragmentDirections.pllEmptyToNewPaymentCard()
        findNavController().navigateIfAdded(this, directions)
    }

    private fun navigateToLCDScreen() {
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
}
