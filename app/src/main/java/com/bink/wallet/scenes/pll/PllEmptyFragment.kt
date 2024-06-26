package com.bink.wallet.scenes.pll

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bink.wallet.BaseFragment
import com.bink.wallet.R
import com.bink.wallet.databinding.FragmentPllEmptyBinding
import com.bink.wallet.modal.generic.GenericModalParameters
import com.bink.wallet.model.response.membership_card.MembershipCard
import com.bink.wallet.model.response.membership_plan.MembershipPlan
import com.bink.wallet.model.response.payment_card.PaymentCard
import com.bink.wallet.utils.*
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

    private lateinit var pendingAdapter: PllPendingAdapter

    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                // Permission is granted. Continue the action or workflow in your
                // app.
                requestPermissionsResult(
                    null,
                    { navigateToAddPaymentCards() },
                    null,
                    isGranted
                )

            } else {
                // Explain to the user that the feature is unavailable because the
                // features requires a permission that the user has denied. At the
                // same time, respect the user's decision. Don't link to system
                // settings in an effort to convince the user to change their
                // decision.
            }
        }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        binding.viewModel = viewModel
        binding.fragment = this

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
                            currentMembershipPlan?.account?.plan_summary ?: "",
                            description2 = planDescription,
                            firstButtonText = getString(R.string.go_to_site)
                        ), currentMembershipPlan?.account?.plan_url ?: ""
                    )
                )
            }
        }

        pendingAdapter =
            PllPendingAdapter(mutableListOf(), clickListener = { goToPendingFaqArticle() })

        binding.rvPendingCards.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = pendingAdapter
        }
        viewModel.getPaymentCards()

        viewModel.paymentCards.observeNonNull(this) {
            if (pendingCards(it).isNotEmpty()) {
                showPendingCardsList(true)
                pendingAdapter.updateData(PaymentCardUtils.inDateCards(it).toMutableList())

            } else {
                showPendingCardsList(false)

            }
        }

        viewModel.paymentCardsError.observeErrorNonNull(requireContext(), false, this)

        currentMembershipPlan?.let {
            binding.membershipPlan = it
        }

        binding.back.setOnClickListener {
            navigateToLCDScreen()
        }

        binding.buttonDone.setOnClickListener {
            navigateToLCDScreen()
        }

        binding.buttonAddPaymentCardNonModal.setOnClickListener {
            requestCameraPermissionAndNavigate(
                requestPermissionLauncher,
                false,
                null,
                { navigateToAddPaymentCards() },
                null
            )
        }

        binding.addPaymentCardModal.setOnClickListener {
            navigateToAddPaymentCards()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        scanResult(
            requestCode,
            resultCode,
            data,
            { navigateToAddPaymentCards(it) },
            { logPaymentCardSuccess(it) })
    }

    private fun navigateToAddPaymentCards(cardNumber: String = "") {
        val directions = PllEmptyFragmentDirections.pllEmptyToNewPaymentCard(
            cardNumber
        )
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

    private fun pendingCards(paymentCards: List<PaymentCard>): List<PaymentCard> {
        return paymentCards.filter { it.status == PAYMENT_CARD_STATUS_PENDING }
    }

    private fun showPendingCardsList(shouldShowList: Boolean) {
        if (shouldShowList) {
            binding.rvPendingCards.visibility = View.VISIBLE
            binding.pllEmptyTitle.text = getString(R.string.pll_pending_cards_title)
            binding.pllEmptyDescriptionPart1.visibility = View.GONE
            binding.pllEmptyDescriptionPart2.text = getString(R.string.pending_pll_card_description)
        } else {
            binding.rvPendingCards.visibility = View.GONE
            binding.pllEmptyTitle.text = getString(R.string.link_payment_cards)
            binding.pllEmptyDescriptionPart1.visibility = View.VISIBLE
            binding.pllEmptyDescriptionPart2.text =
                getString(R.string.link_payment_card_description_part_2)

        }

    }

    private fun goToPendingFaqArticle() {
        findNavController().navigate(
            PllEmptyFragmentDirections.globalToWeb(getString(R.string.faq_url))
        )
    }
}
