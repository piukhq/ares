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
import com.bink.wallet.model.MembershipCardListWrapper
import com.bink.wallet.model.response.membership_card.MembershipCard
import com.bink.wallet.model.response.membership_plan.MembershipPlan
import com.bink.wallet.utils.EMPTY_STRING
import com.bink.wallet.utils.FirebaseEvents.PAYMENT_DETAIL_VIEW
import com.bink.wallet.utils.SCROLL_DELAY
import com.bink.wallet.utils.UtilFunctions.isNetworkAvailable
import com.bink.wallet.utils.enums.CardType
import com.bink.wallet.utils.navigateIfAdded
import com.bink.wallet.utils.observeErrorNonNull
import com.bink.wallet.utils.observeNonNull
import com.bink.wallet.utils.toolbar.FragmentToolbar
import kotlinx.coroutines.runBlocking
import org.koin.androidx.viewmodel.ext.android.viewModel

class PaymentCardsDetailsFragment :
    BaseFragment<PaymentCardsDetailsViewModel, PaymentCardsDetailsFragmentBinding>() {

    override fun builder(): FragmentToolbar {
        return FragmentToolbar.Builder()
            .withId(FragmentToolbar.NO_TOOLBAR)
            .build()
    }

    override val viewModel: PaymentCardsDetailsViewModel by viewModel()

    override val layoutRes: Int
        get() = R.layout.payment_cards_details_fragment

    private var scrollY = 0

    private lateinit var availablePllAdapter: AvailablePllAdapter

    private var membershipPlan: MembershipPlan? = null
    private var position: Int? = null

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        with(binding.toolbar) {
            setNavigationIcon(R.drawable.ic_close)
            setNavigationOnClickListener {
                goHome()
            }
        }

        arguments?.let {
            val currentBundle = PaymentCardsDetailsFragmentArgs.fromBundle(it)
            with(viewModel) {
                paymentCard.value = currentBundle.paymentCard
                membershipCardData.value = currentBundle.membershipCards.toList()
                membershipPlanData.value = currentBundle.membershipPlans.toList()
            }
        }

        binding.paymentCardDetail = viewModel.paymentCard.value
        viewModel.membershipCardData.value?.let {
            binding.paymentHeader.membershipCardsWrapper =
                MembershipCardListWrapper(it.toMutableList())
        }
        binding.footerSecurity.setOnClickListener {
            val action =
                PaymentCardsDetailsFragmentDirections.paymentDetailToSecurity(
                    GenericModalParameters(
                        R.drawable.ic_close,
                        true,
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
                if (isNetworkAvailable(requireActivity(), true)) {
                    runBlocking {
                        viewModel.deletePaymentCard(viewModel.paymentCard.value?.id.toString())
                    }
                }
            }
            dialog = builder.create()
            dialog.show()
        }

        with(viewModel.paymentCard) {
            value?.let {
                it.card?.let { bankCard ->
                    if (bankCard.isExpired()) {
                        with(binding.paymentHeader) {
                            cardExpired.visibility = View.VISIBLE
                            linkStatus.visibility = View.GONE
                            imageStatus.visibility = View.GONE
                        }
                    }
                }
            }
        }

        viewModel.membershipPlanData.observeNonNull(this) { plans ->
            val pllPlansIds = mutableListOf<String>()
            plans.forEach { plan -> if (plan.getCardType() == CardType.PLL) pllPlansIds.add(plan.id) }
            viewModel.membershipCardData.observeNonNull(this) { cards ->
                val pllCards = cards.filter { card -> pllPlansIds.contains(card.membership_plan) }
                binding.apply {
                    hasAddedPllCards = pllCards.isNotEmpty()
                    availablePllList.apply {
                        layoutManager = GridLayoutManager(context, 1)
                        viewModel.paymentCard.value?.let {
                            availablePllAdapter = AvailablePllAdapter(
                                it,
                                plans,
                                pllCards,
                                onLinkStatusChange = ::onLinkStatusChange,
                                onItemSelected = ::onItemSelected
                            )
                        }

                        adapter = availablePllAdapter
                    }
                    val unaddedCardsForPlan = mutableListOf<MembershipPlan>()
                    for (plan in plans.filter { it.getCardType() == CardType.PLL }) {
                        if (cards.none { card -> card.membership_plan == plan.id }) {
                            unaddedCardsForPlan.add(plan)
                        }
                    }
                    hasOtherCardsToAdd = unaddedCardsForPlan.isNotEmpty()
                    shouldDisplayOtherCardsTitleAndDescription = pllCards.isNotEmpty() &&
                            unaddedCardsForPlan.isNotEmpty()
                    otherCardsList.apply {
                        layoutManager = GridLayoutManager(context, 1)
                        adapter = SuggestedCardsAdapter(
                            unaddedCardsForPlan,
                            itemClickListener = {
                                val directions =
                                    PaymentCardsDetailsFragmentDirections.paymentDetailsToAddJoin(
                                        it,
                                        null,
                                        true,
                                        isRetryJourney = false
                                    )
                                findNavController().navigateIfAdded(
                                    this@PaymentCardsDetailsFragment,
                                    directions
                                )
                            }
                        )
                    }
                }
            }
        }

        viewModel.deleteRequest.observeNonNull(this) {
            findNavController().navigateIfAdded(this, R.id.global_to_home)
        }

        viewModel.deleteError.observeErrorNonNull(
            requireContext(),
            this,
            EMPTY_STRING,
            getString(R.string.card_error_dialog),
            true,
            null
        )

        viewModel.paymentCard.observeNonNull(this) {
            binding.paymentCardDetail = it

            viewModel.storePaymentCard(it)

            viewModel.getMembershipCards()
        }

        viewModel.linkError.observeNonNull(this) {
            it.response()?.errorBody()?.string()?.let { responseString ->
                if (responseString.contains(PLAN_EXISTS)) {
                    showLinkErrorMessage()
                }
            }

        }

        viewModel.unlinkError.observeErrorNonNull(requireContext(), true, this)
    }

    override fun onPause() {
        super.onPause()
        scrollY = binding.scrollView.scrollY
    }

    override fun onResume() {
        super.onResume()
        logScreenView(PAYMENT_DETAIL_VIEW)
        binding.scrollView.postDelayed({
            binding.scrollView.scrollTo(0, scrollY)
        }, SCROLL_DELAY)
        if (isNetworkAvailable(requireActivity())) {
            viewModel.getMembershipCards()
        }
    }

    private fun goHome() {
        findNavController().navigateIfAdded(
            this,
            R.id.global_to_home
        )
    }

    private fun onLinkStatusChange(
        currentItem: Triple<String?, Boolean, MembershipPlan?>,
        position: Int?
    ) {
        currentItem.first?.let {
            runBlocking {
                membershipPlan = if (currentItem.second) {
                    viewModel.linkPaymentCard(
                        it,
                        viewModel.paymentCard.value?.id.toString()
                    )
                    currentItem.third
                } else {
                    viewModel.unlinkPaymentCard(
                        it,
                        viewModel.paymentCard.value?.id.toString()
                    )
                    null
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

    private fun showLinkErrorMessage() {
        val planName = membershipPlan?.account?.plan_name ?: ""
        val planNameCard = membershipPlan?.account?.plan_name_card ?: ""

        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.payment_card_link_already_exists_title))
            .setMessage(
                getString(
                    R.string.payment_card_link_already_exists_message,
                    planName,
                    planNameCard,
                    planName,
                    planNameCard
                )
            )
            .setPositiveButton(
                getString(R.string.ok)
            ) { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    companion object {
        const val PLAN_EXISTS = "PLAN_ALREADY_LINKED"
    }
}
