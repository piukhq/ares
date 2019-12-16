package com.bink.wallet.scenes.payment_card_details

import android.app.AlertDialog
import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.bink.wallet.BaseFragment
import com.bink.wallet.R
import com.bink.wallet.databinding.PaymentCardsDetailsFragmentBinding
import com.bink.wallet.modal.generic.GenericModalParameters
import com.bink.wallet.model.response.membership_card.MembershipCard
import com.bink.wallet.model.response.membership_plan.MembershipPlan
import com.bink.wallet.utils.*
import com.bink.wallet.utils.enums.CardType
import com.bink.wallet.utils.toolbar.FragmentToolbar
import kotlinx.coroutines.runBlocking
import org.koin.androidx.viewmodel.ext.android.viewModel

class PaymentCardsDetailsFragment :
    BaseFragment<PaymentCardsDetailsViewModel, PaymentCardsDetailsFragmentBinding>() {

    private lateinit var availablePllAdapter: AvailablePllAdapter

    override fun builder(): FragmentToolbar {
        return FragmentToolbar.Builder()
            .withId(FragmentToolbar.NO_TOOLBAR)
            .build()
    }

    override val viewModel: PaymentCardsDetailsViewModel by viewModel()

    override val layoutRes: Int
        get() = R.layout.payment_cards_details_fragment

    private var scrollY = 0

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        with(binding.toolbar) {
            setNavigationIcon(R.drawable.ic_close)
            setNavigationOnClickListener {
                goHome()
            }
        }

        val securityDialog = SecurityDialogs(requireContext())

        arguments?.let {
            val currentBundle = PaymentCardsDetailsFragmentArgs.fromBundle(it)

            with(viewModel) {
                paymentCard.value = currentBundle.paymentCard
                membershipCardData.value = currentBundle.membershipCards.toList()
                membershipPlanData.value = currentBundle.membershipPlans.toList()
            }
        }

        binding.paymentCardDetail = viewModel.paymentCard.value

        binding.footerSecurity.setOnClickListener {
            val action =
                PaymentCardsDetailsFragmentDirections.paymentDetailToSecurity(
                    GenericModalParameters(
                        R.drawable.ic_close,
                        getString(R.string.security_and_privacy_title),
                        getString(R.string.security_and_privacy_copy),
                        description2 = getString(R.string.security_and_privacy_copy_2)
                    )
                )
            findNavController().navigateIfAdded(this, action)
        }

        binding.footerDelete.setOnClickListener {
            val builder = AlertDialog.Builder(context)
            val dialog: AlertDialog
            builder.setMessage(getString(R.string.delete_card_modal_body))
            builder.setNeutralButton(getString(R.string.no_text)) { _, _ -> }
            builder.setPositiveButton(getString(R.string.yes_text)) { _, _ ->
                if (verifyAvailableNetwork(requireActivity())) {
                    runBlocking {
                        viewModel.deletePaymentCard(viewModel.paymentCard.value?.id.toString())
                    }
                } else {
                    showNoInternetConnectionDialog()
                }
            }
            dialog = builder.create()
            dialog.show()
        }

        with(viewModel.paymentCard) {
            if (value != null &&
                value!!.card != null &&
                value!!.card!!.isExpired()
            ) {
                with(binding.paymentHeader) {
                    cardExpired.visibility = View.VISIBLE
                    linkStatus.visibility = View.GONE
                    imageStatus.visibility = View.GONE
                }
            }
        }

        viewModel.membershipPlanData.observeNonNull(this) { plans ->
            val pllPlansIds = mutableListOf<String>()
            plans.forEach { plan -> if(plan.getCardType() == CardType.PLL) pllPlansIds.add(plan.id)}
            viewModel.membershipCardData.observeNonNull(this) { cards ->
                val pllCards = cards.filter { card -> pllPlansIds.contains(card.membership_plan) }
                viewModel.paymentCard.value?.let { paymentCard ->
                    availablePllAdapter = AvailablePllAdapter(
                        paymentCard,
                        plans,
                        pllCards,
                        onLinkStatusChange = ::onLinkStatusChange,
                        onItemSelected = ::onItemSelected
                    )
                }
                binding.apply {
                    paymentCardDetailsTitle.visibility = View.VISIBLE
                    paymentCardDetailsDescription.visibility = View.VISIBLE
                    availablePllList.apply {
                        visibility = View.VISIBLE
                        layoutManager = GridLayoutManager(context, 1)
                        adapter = availablePllAdapter
                    }

                    otherCardsList.apply {
                        val unaddedCardsForPlan = mutableListOf<MembershipPlan>()
                        for (plan in plans.filter { it.getCardType() == CardType.PLL }) {
                            if (cards.count { card -> card.membership_plan == plan.id } == 0) {
                                unaddedCardsForPlan.add(plan)
                            }
                        }
                        if (unaddedCardsForPlan.isNotEmpty()) {
                            visibility = View.VISIBLE
                            layoutManager = GridLayoutManager(context, 1)
                            adapter = SuggestedCardsAdapter(
                                unaddedCardsForPlan,
                                itemClickListener = {
                                    val directions =
                                        PaymentCardsDetailsFragmentDirections.paymentDetailsToAddJoin(
                                            it
                                        )
                                    findNavController().navigateIfAdded(
                                        this@PaymentCardsDetailsFragment,
                                        directions
                                    )
                                }
                            )
                        }
                    }
                    otherCardsDescription.visibility = View.VISIBLE
                    otherCardsTitle.visibility = View.VISIBLE
                }
            }
        }

        viewModel.deleteRequest.observeNonNull(this) {
            findNavController().navigateIfAdded(this, R.id.global_to_home)
        }

        viewModel.deleteError.observeNonNull(this) {
            requireContext().displayModalPopup(
                "",
                getString(R.string.card_error_dialog)
            )
        }

        viewModel.paymentCard.observeNonNull(this) {
            binding.paymentCardDetail = it
            availablePllAdapter.updatePaymentCard(it)
        }
    }

    override fun onPause() {
        super.onPause()
        scrollY = binding.scrollView.scrollY
    }

    override fun onResume() {
        super.onResume()
        binding.scrollView.postDelayed({
            binding.scrollView.scrollTo(0, scrollY)
        }, SCROLL_DELAY)
    }


    private fun addLoyaltyCard(plan: MembershipPlan) {
        val directions =
            PaymentCardsDetailsFragmentDirections.paymentDetailsToAddJoin(
                plan
            )
        findNavController().navigateIfAdded(
            this@PaymentCardsDetailsFragment,
            directions
        )
    }

    private fun goHome() {
        findNavController().navigateIfAdded(
            this,
            R.id.global_to_home
        )
    }

    private fun onLinkStatusChange(currentItem: Pair<String?, Boolean>) {
        if (currentItem.first != null) {
            runBlocking {
                if (currentItem.second) {
                    viewModel.linkPaymentCard(
                        currentItem.first!!,
                        viewModel.paymentCard.value?.id.toString()
                    )
                } else {
                    viewModel.unlinkPaymentCard(
                        currentItem.first!!,
                        viewModel.paymentCard.value?.id.toString()
                    )
                }
            }
        }
    }

    private fun onItemSelected(membershipPlan: MembershipPlan, membershipCard: MembershipCard) {
        val directions = PaymentCardsDetailsFragmentDirections.paymentDetailsToLoyaltyCardDetail(
            membershipPlan,
            membershipCard
        )
        directions.let { findNavController().navigateIfAdded(this, directions) }
    }
}
