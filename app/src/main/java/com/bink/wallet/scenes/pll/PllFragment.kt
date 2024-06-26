package com.bink.wallet.scenes.pll

import android.app.AlertDialog
import android.os.Bundle
import android.view.View
import androidx.navigation.NavDirections
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bink.wallet.BaseFragment
import com.bink.wallet.R
import com.bink.wallet.data.SharedPreferenceManager
import com.bink.wallet.databinding.FragmentPllBinding
import com.bink.wallet.modal.generic.GenericModalParameters
import com.bink.wallet.model.response.payment_card.PaymentCard
import com.bink.wallet.utils.*
import com.bink.wallet.utils.FirebaseEvents.PLL_VIEW
import com.bink.wallet.utils.FirebaseEvents.getFirebaseIdentifier
import com.bink.wallet.utils.UtilFunctions.isNetworkAvailable
import com.bink.wallet.utils.toolbar.FragmentToolbar
import org.koin.androidx.viewmodel.ext.android.viewModel
import retrofit2.HttpException

class PllFragment : BaseFragment<PllViewModel, FragmentPllBinding>() {
    override val layoutRes: Int
        get() = R.layout.fragment_pll

    override val viewModel: PllViewModel by viewModel()

    override fun builder(): FragmentToolbar {
        return FragmentToolbar.Builder()
            .with(binding.toolbar)
            .shouldDisplayBack(requireActivity())
            .build()
    }

    private var directions: NavDirections? = null
    private var isAddJourney = false
    private val footerQuotient = 3
    private val unselectedCards = mutableListOf<PaymentCard>()
    private val selectedCards = mutableListOf<PaymentCard>()
    private val recyclerViewHelper: RecyclerViewHelper = RecyclerViewHelper()
    private lateinit var pendingAdapter: PllPendingAdapter


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        binding.viewModel = viewModel
        arguments?.let {
            PllFragmentArgs.fromBundle(it).apply {
                viewModel.membershipPlan.value = membershipPlan
                viewModel.membershipCard.value = membershipCard
                if (isAddJourney) {
                    this@PllFragment.isAddJourney = isAddJourney
                    binding.toolbar.navigationIcon = null
                }
                SharedPreferenceManager.isAddJourney = isAddJourney
            }
        }

        binding.item = viewModel.membershipPlan.value


        if (isNetworkAvailable(requireActivity())) {
            viewModel.getPaymentCards()
        } else {
            viewModel.getLocalPaymentCards()
        }

        binding.toolbar.setNavigationOnClickListener {
            val directions = viewModel.membershipCard.value?.let { membershipCard ->
                viewModel.membershipPlan.value?.let { membershipPlan ->
                    PllFragmentDirections.pllToLcd(
                        membershipPlan,
                        membershipCard
                    )
                }
            }
            directions?.let { _ -> findNavController().navigateIfAdded(this, directions) }
        }


        val adapter = PllPaymentCardAdapter(mutableListOf(), isAddJourney)
        pendingAdapter =
            PllPendingAdapter(mutableListOf(), clickListener = { goToPendingFaqArticle() })
        binding.rvPendingPaymentCards.layoutManager = LinearLayoutManager(context)
        binding.rvPendingPaymentCards.adapter = pendingAdapter

        viewModel.paymentCardsMerger.observeNonNull(this) {
            val (pendingCards, activeCards) = PaymentCardUtils.inDateCards(it)
                .partition { paymentCard -> paymentCard.status == PAYMENT_CARD_STATUS_PENDING }

            viewModel.membershipCard.value?.let { membershipCard ->
                val unsortedCards = (activeCards.sortedByDescending { card -> card.id })
                val sortedCards = WalletOrderingUtil.getSavedPaymentCardWalletForPll(unsortedCards)
                adapter.updateData(sortedCards, membershipCard)
                binding.brandModal.setOnClickListener {
                    viewModel.membershipPlan.value?.account?.plan_description?.let { planDescription ->
                        findNavController().navigate(
                            PllFragmentDirections.pllToBrandHeader(
                                GenericModalParameters(
                                    R.drawable.ic_close,
                                    true,
                                    viewModel.membershipPlan.value?.account?.plan_name
                                        ?: getString(R.string.plan_description),
                                    viewModel.membershipPlan.value?.account?.plan_summary ?: "",
                                    description2 = planDescription,
                                    firstButtonText = getString(R.string.go_to_site)
                                ), viewModel.membershipPlan.value?.account?.plan_url ?: ""

                            )
                        )
                    }

                }
            }

            if (pendingCards.isNotEmpty()) {
                showPendingCardsList(true)
                pendingAdapter.updateData(pendingCards.toMutableList())

            } else {
                showPendingCardsList(false)
            }
        }

        binding.paymentCards.adapter = adapter
        binding.paymentCards.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)


        viewModel.membershipCard.observeNonNull(this) {
            directions =
                viewModel.membershipCard.value?.let { membershipCard ->
                    viewModel.membershipPlan.value?.let { membershipPlan ->
                        PllFragmentDirections.pllToLcd(
                            membershipPlan,
                            membershipCard,
                            isFromPll = true
                        )
                    }
                }
        }

        viewModel.unlinkSuccesses.observeNonNull(this) {
            if (it.size == unselectedCards.size) {
                navigateToLCD()
            }
        }

        viewModel.linkSuccesses.observeNonNull(this) {
            val linkFailureCards = viewModel.linkErrors.value?.size ?: 0
            val totalCards = linkFailureCards + it.size

            if (it.size == selectedCards.size) {
                navigateToLCD()
            } else if (linkFailureCards > 0 && totalCards == selectedCards.size) {
                showLinkErrorMessage(isPlanAlreadyExistsError(viewModel.linkErrors.value), null)
            }
        }

        binding.buttonDone.setOnClickListener {
            when {
                viewModel.paymentCardsMerger.value.isNullOrEmpty() -> {
                    findNavController().popBackStack()
                }
                isNetworkAvailable(requireActivity(), true) -> {
                    viewModel.membershipCard.value?.let {
                        adapter.paymentCards.forEach { paymentCard ->

                            if (paymentCard.isSelected &&
                                !paymentCard.isLinkedToMembershipCard(it)
                            ) {
                                if (isAddJourney) {
                                    SharedPreferenceManager.hasAddedNewPll = true
                                }

                                selectedCards.add(paymentCard)
                            } else if (viewModel.membershipCard.value != null &&
                                !paymentCard.isSelected &&
                                paymentCard.isLinkedToMembershipCard(it)
                            ) {
                                unselectedCards.add(paymentCard)
                            }

                        }
                    }

                    viewModel.membershipCard.value?.let {
                        if (unselectedCards.isNotEmpty()) {
                            viewModel.unlinkPaymentCards(
                                unselectedCards,
                                it
                            )
                        }

                        if (selectedCards.isNotEmpty()) {
                            viewModel.linkPaymentCards(
                                selectedCards,
                                it
                            )
                        }
                    }

                    if (unselectedCards.isEmpty() && selectedCards.isEmpty()) {
                        navigateToLCD()
                    }
                }
            }

            logEvent(getFirebaseIdentifier(PLL_VIEW, binding.buttonDone.text.toString()))
        }

        viewModel.fetchError.observeErrorNonNull(requireContext(), false, this)

        viewModel.linkError.observeErrorNonNull(requireContext(), this, true) {
            if (!NetworkUtils.isConnected(requireContext())) {
                AlertDialog.Builder(requireContext())
                    .setTitle(getString(R.string.description_error))
                    .setMessage(getString(R.string.delete_and_update_card_internet_connection_error_message))
                    .setPositiveButton(
                        getString(R.string.ok)
                    ) { dialog, _ ->
                        dialog.dismiss()
                        if (findNavController().currentDestination?.id == R.id.pll_fragment) {
                            directions?.let { directions ->
                                findNavController().navigateIfAdded(
                                    this@PllFragment,
                                    directions
                                )
                            }
                        }
                    }
                    .show()
            }
        }

        viewModel.unlinkError.observeErrorNonNull(requireContext(), this, true) {
            if (!NetworkUtils.isConnected(requireContext())) {
                AlertDialog.Builder(requireContext())
                    .setTitle(getString(R.string.description_error))
                    .setMessage(getString(R.string.delete_and_update_card_internet_connection_error_message))
                    .setPositiveButton(
                        getString(R.string.ok)
                    ) { dialog, _ ->
                        dialog.dismiss()
                        if (findNavController().currentDestination?.id == R.id.pll_fragment) {
                            directions?.let { directions ->
                                findNavController().navigateIfAdded(
                                    this@PllFragment,
                                    directions
                                )
                            }
                        }

                    }
                    .show()
            }
        }

        viewModel.linkErrors.observeNonNull(this) {
            val linkSuccessCards = viewModel.linkSuccesses.value?.size ?: 0
            val totalCards = linkSuccessCards + it.size

            if (totalCards == selectedCards.size) {
                val shouldShowPlanAlreadyExists =
                    isPlanAlreadyExistsError(it)
                if (shouldShowPlanAlreadyExists) {
                    showLinkErrorMessage(shouldShowPlanAlreadyExists, null)
                } else {
                    viewModel.hasDisplayableError(requireContext(), it as ArrayList<Exception>)
                        ?.let { errorMessage ->
                            showLinkErrorMessage(false, errorMessage)
                        }
                }
            }
        }

        viewModel.unlinkErrors.observeNonNull(this) {
            viewModel.hasDisplayableError(requireContext(), it)?.let { errorMessage ->
                showLinkErrorMessage(false, errorMessage)
            }
        }
    }

    private fun goToPendingFaqArticle() {
        findNavController().navigate(
            PllFragmentDirections.globalToWeb(getString(R.string.faq_url))
        )
    }

    override fun onResume() {
        super.onResume()
        recyclerViewHelper.setFooterFadeEffect(
            mutableListOf(binding.buttonDone),
            binding.rvPendingPaymentCards,
            binding.bgPllBottomGradient,
            true,
            footerQuotient
        )
        recyclerViewHelper.registerFooterListener(binding.root)
        logScreenView(PLL_VIEW)
    }

    override fun onPause() {
        super.onPause()
        recyclerViewHelper.removeFooterListener(binding.root)
    }

    private fun showLinkErrorMessage(shouldShowPlanAlreadyExists: Boolean?, errorText: String?) {
        val membershipPlan = viewModel.membershipPlan.value
        val planName = membershipPlan?.account?.plan_name ?: ""
        val planNameCard = membershipPlan?.account?.plan_name_card ?: ""

        val title = if (shouldShowPlanAlreadyExists!!) {
            getString(R.string.payment_card_link_already_exists_title)
        } else {
            getString(R.string.pll_error_title)
        }

        val message = if (shouldShowPlanAlreadyExists) {
            getString(
                R.string.payment_card_link_already_exists_message,
                planName,
                planNameCard,
                planName,
                planNameCard
            )
        } else {
            if (errorText.isNullOrBlank()) {
                getString(
                    R.string.pll_error_message
                )
            } else {
                errorText
            }
        }

        AlertDialog.Builder(requireContext())
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton(
                getString(R.string.ok)
            ) { dialog, _ ->
                dialog.dismiss()
                navigateToLCD()

            }
            .setCancelable(false)
            .show()
    }

    private fun isPlanAlreadyExistsError(it: MutableList<Exception>?): Boolean {
        val firstException = it?.firstOrNull()
        return (firstException as HttpException).response()?.errorBody()?.string()
            ?.contains(PLAN_ALREADY_EXISTS) ?: false
    }

    private fun navigateToLCD() {
        if (findNavController().currentDestination?.id == R.id.pll_fragment) {
            directions?.let { directions ->
                findNavController().navigateIfAdded(
                    this@PllFragment,
                    directions
                )
            }
        }
    }

    private fun showPendingCardsList(shouldShowPendingCards: Boolean) {
        val visibility = if (shouldShowPendingCards) View.VISIBLE else View.GONE

        binding.rvPendingPaymentCards.visibility = visibility
        binding.pendingCardsTitle.visibility = visibility
        binding.pendingCardsDescription.visibility = visibility
    }
}
