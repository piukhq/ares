package com.bink.wallet.scenes.payment_card_details

import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.bink.wallet.BaseFragment
import com.bink.wallet.BuildConfig
import com.bink.wallet.R
import com.bink.wallet.databinding.PaymentCardsDetailsFragmentBinding
import com.bink.wallet.modal.generic.GenericModalParameters
import com.bink.wallet.model.MembershipCardListWrapper
import com.bink.wallet.model.response.membership_card.MembershipCard
import com.bink.wallet.model.response.membership_plan.MembershipPlan
import com.bink.wallet.model.response.payment_card.PaymentCard
import com.bink.wallet.utils.*
import com.bink.wallet.utils.FirebaseEvents.PAYMENT_DETAIL_VIEW
import com.bink.wallet.utils.UtilFunctions.isNetworkAvailable
import com.bink.wallet.utils.enums.CardType
import com.bink.wallet.utils.toolbar.FragmentToolbar
import kotlinx.coroutines.runBlocking
import org.koin.androidx.viewmodel.ext.android.viewModel
import retrofit2.HttpException

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

    private var planAndPositionPair = mutableListOf<Pair<MembershipPlan?, Int?>>()

    private var countDownTimer: CountDownTimer? = null

    private var hasRefreshedAtLeastOnce = false

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

                if (paymentCard.value == null) {
                    paymentCard.value = currentBundle.paymentCard
                }
                if (membershipCardData.value == null) {
                    membershipCardData.value = currentBundle.membershipCards.toList()
                }
                if (membershipPlanData.value == null) {
                    membershipPlanData.value = currentBundle.membershipPlans.toList()
                        .filter { plan -> plan.canPlanBeAdded() }
                }
            }
        }

        binding.fragment = this
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
            builder.setMessage(getString(R.string.delete_card_modal_body))
            builder.setNeutralButton(getString(R.string.no_text)) { _, _ -> }
            builder.setPositiveButton(getString(R.string.yes_text)) { _, _ ->
                if (isNetworkAvailable(requireActivity(), true)) {
                    runBlocking {
                        viewModel.deletePaymentCard(viewModel.paymentCard.value?.id.toString())
                    }
                }
            }
            val dialog: AlertDialog = builder.create()
            dialog.show()
        }

        binding.footerFaqs.setOnClickListener {
            findNavController().navigate(
                PaymentCardsDetailsFragmentDirections.globalToWeb(getString(R.string.faq_url))
            )
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

        cardState(viewModel.paymentCard.value, viewModel.membershipCardData.value)

        viewModel.paymentCard.observeNonNull(this) {
            viewModel.getMembershipCards()
        }

        viewModel.membershipCardData.observeNonNull(this) {
            cardState(viewModel.paymentCard.value, it)
        }

        viewModel.getCardError.observeErrorNonNull(requireContext(), false, this)

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

        viewModel.linkError.observeNonNull(this) {
            (it.first as HttpException).response()?.errorBody()?.string()?.let { responseString ->
                if (responseString.contains(PLAN_ALREADY_EXISTS)) {
                    showLinkErrorMessage(it.second)
                }
            }
        }

        viewModel.unlinkError.observeNonNull(this) {
            val exception = (it as HttpException)
            val errorText = exception.response()?.errorBody()?.string()
            when (exception.code()) {
                403 -> errorText?.let { message -> showUnLinkErrorMessage(message) }
            }
        }
    }

    private fun cardState(paymentCard: PaymentCard?, membershipCard: List<MembershipCard>?) {
        paymentCard?.let { pCard ->
            membershipCard?.let { membershipCard ->

                binding.paymentCardDetail = pCard
                when (pCard.isCardActive()) {
                    true -> setActivePcdScreen(pCard, membershipCard)
                    else -> setInactivePcdScreen()
                }
                if (hasRefreshedAtLeastOnce) {
                    countDownTimer?.start()
                }
                viewModel.storePaymentCard(pCard)
            }
        }

    }

    override fun onResume() {
        super.onResume()
        logScreenView(PAYMENT_DETAIL_VIEW)
        binding.scrollView.postDelayed({
            binding.scrollView.scrollTo(0, scrollY)
        }, SCROLL_DELAY)

        countDownTimer?.start()
    }

    override fun onPause() {
        super.onPause()
        scrollY = binding.scrollView.scrollY

        countDownTimer?.cancel()
    }


    private fun goHome() {
        findNavController().navigateIfAdded(
            this,
            R.id.global_to_home
        )
    }


    @Suppress("UNCHECKED_CAST")
    private fun setActivePcdScreen(
        pCard: PaymentCard,
        membershipCards: List<MembershipCard>
    ) {
        setViewState(true)
        viewModel.membershipPlanData.value?.let { plans ->
            val pllPlansIds = mutableListOf<String>()
            plans.forEach { plan -> if (plan.getCardType() == CardType.PLL) pllPlansIds.add(plan.id) }

            val pllCards =
                membershipCards.filter { card -> pllPlansIds.contains(card.membership_plan) }
            binding.apply {
                hasAddedPllCards = pllCards.isNotEmpty() && pCard.card?.isExpired() == false
                availablePllList.apply {
                    layoutManager = GridLayoutManager(context, 1)

                    availablePllAdapter = AvailablePllAdapter(
                        pCard,
                        plans,
                        WalletOrderingUtil.getSavedLoyaltyCardWallet(pllCards as ArrayList<Any>) as ArrayList<MembershipCard>,
                        onLinkStatusChange = ::onLinkStatusChange,
                        onItemSelected = ::onItemSelected
                    )

                    adapter = availablePllAdapter
                }
                val unaddedCardsForPlan = mutableListOf<MembershipPlan>()
                for (plan in plans.filter { it.getCardType() == CardType.PLL }) {
                    if (membershipCards.none { card -> card.membership_plan == plan.id }) {
                        unaddedCardsForPlan.add(plan)
                    }
                }
                val shouldShowOtherCards =
                    unaddedCardsForPlan.isNotEmpty() && pCard.card?.isExpired() == false
                hasOtherCardsToAdd = shouldShowOtherCards
                shouldDisplayOtherCardsTitleAndDescription =
                    pllCards.isNotEmpty() && shouldShowOtherCards

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

    private fun setInactivePcdScreen() {
        setViewState(false)
        if (viewModel.paymentCard.value?.status?.let { PaymentCardUtils.cardStatus(it) } == PENDING_CARD) {
            countDownTimer = object : CountDownTimer(30000, 1000) {
                override fun onFinish() {
                    viewModel.paymentCard.value?.id?.let { viewModel.getPaymentCard(it) }
                    hasRefreshedAtLeastOnce = true
                }

                override fun onTick(millisUntilFinished: Long) {

                }

            }
        }


    }

    private fun setViewState(shouldShowViews: Boolean) {
        val expiredCard = viewModel.paymentCard.value?.card?.isExpired()
        val visibility = if (shouldShowViews) View.VISIBLE else View.GONE
        val invertedVisibility =
            if (expiredCard == true) View.VISIBLE else if (shouldShowViews) View.GONE else View.VISIBLE
        with(binding) {
            availablePllList.visibility = visibility
            otherCardsTitle.visibility = visibility
            otherCardsDescription.visibility = visibility
            otherCardsList.visibility = visibility
            separator.visibility = invertedVisibility
            footerFaqs.visibility = invertedVisibility
            deleteSeparator.visibility = invertedVisibility
        }

    }

    private fun onLinkStatusChange(
        currentItem: Triple<String?, Boolean, MembershipPlan?>,
        position: Int?
    ) {
        planAndPositionPair.clear()
        planAndPositionPair.add(Pair(currentItem.third, position))

        currentItem.first?.let {
            currentItem.third?.let { plan ->
                if (currentItem.second) {
                    viewModel.linkPaymentCard(
                        it,
                        plan.id
                    )
                } else {
                    viewModel.unlinkPaymentCard(
                        it
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

    private fun showLinkErrorMessage(planId: String) {
        val membershipPlan = planAndPositionPair.firstOrNull { pair -> pair.first?.id == planId }
        val planName = membershipPlan?.first?.account?.plan_name ?: ""
        val planNameCard = membershipPlan?.first?.account?.plan_name_card ?: ""
        val position = membershipPlan?.second

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
                position?.let { availablePllAdapter.notifyItemChanged(it) }
                dialog.dismiss()
            }
            .setCancelable(false)
            .show()
    }

    val onContactUsClicked: () -> Unit = {
        try {
            startActivity(Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse(getString(R.string.contact_us_mailto))
                putExtra(
                    Intent.EXTRA_EMAIL,
                    arrayOf(getString(R.string.contact_us_email_address))
                )
                putExtra(
                    Intent.EXTRA_SUBJECT,
                    getString(R.string.contact_us_email_subject, BuildConfig.VERSION_NAME)
                )
            })
        } catch (ex: ActivityNotFoundException) {
            requireContext().displayModalPopup(
                getString(R.string.contact_us_no_email_title),
                getString(R.string.contact_us_no_email_message),
                buttonText = R.string.ok
            )
        }
    }


}
