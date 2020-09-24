package com.bink.wallet.scenes.pll

import android.app.AlertDialog
import android.os.Bundle
import androidx.navigation.NavDirections
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bink.wallet.BaseFragment
import com.bink.wallet.R
import com.bink.wallet.data.SharedPreferenceManager
import com.bink.wallet.databinding.FragmentPllBinding
import com.bink.wallet.modal.generic.GenericModalParameters
import com.bink.wallet.model.response.membership_card.MembershipCard
import com.bink.wallet.model.response.payment_card.PaymentCard
import com.bink.wallet.utils.FirebaseEvents.PLL_VIEW
import com.bink.wallet.utils.FirebaseEvents.getFirebaseIdentifier
import com.bink.wallet.utils.NetworkUtils
import com.bink.wallet.utils.RecyclerViewHelper
import com.bink.wallet.utils.UtilFunctions.isNetworkAvailable
import com.bink.wallet.utils.isLinkedToMembershipCard
import com.bink.wallet.utils.navigateIfAdded
import com.bink.wallet.utils.observeErrorNonNull
import com.bink.wallet.utils.observeNonNull
import com.bink.wallet.utils.toolbar.FragmentToolbar
import kotlinx.coroutines.runBlocking
import org.koin.androidx.viewmodel.ext.android.viewModel

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
    val unselectedCards = mutableListOf<PaymentCard>()
    val selectedCards = mutableListOf<PaymentCard>()
    private val recyclerViewHelper: RecyclerViewHelper = RecyclerViewHelper()

    override fun onResume() {
        super.onResume()
        recyclerViewHelper.setFooterFadeEffect(
            mutableListOf(binding.buttonDone),
            binding.paymentCards,
            binding.bgPllBottomGradient,
            false,
            footerQuotient
        )
        recyclerViewHelper.registerFooterListener(binding.root)
        logScreenView(PLL_VIEW)
    }

    override fun onPause() {
        super.onPause()
        recyclerViewHelper.removeFooterListener(binding.root)
    }

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

        runBlocking {
            if (isNetworkAvailable(requireActivity())) {
                viewModel.getPaymentCards()
            } else {
                viewModel.getLocalPaymentCards()
            }
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

        val adapter = PllPaymentCardAdapter()

        viewModel.paymentCardsMerger.observeNonNull(this) {
            viewModel.membershipCard.value?.let { membershipCard ->
                val adapterItems = mutableListOf<PllAdapterItem>().apply {
                    viewModel.membershipPlan.value?.let { membershipPlan ->
                        add(PllAdapterItem.PllBrandHeaderItem(membershipPlan))
                    }
                    add(PllAdapterItem.PllTitleItem)
                    viewModel.membershipPlan.value?.account?.plan_name_card?.let { planName ->
                        add(PllAdapterItem.PllDescriptionItem(planName))
                    }
                    addAll(it.toPllPaymentCardWrapperList(isAddJourney, membershipCard))
                }
                adapter.paymentCards = adapterItems
                adapter.setOnBrandHeaderClickListener {
                    findNavController().navigate(
                        PllFragmentDirections.pllToBrandHeader(
                            GenericModalParameters(
                                R.drawable.ic_close,
                                true,
                                viewModel.membershipPlan.value?.account?.plan_name
                                    ?: getString(R.string.plan_description),
                                it
                            )
                        )
                    )
                }
                adapter.notifyDataSetChanged()
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
                showLinkErrorMessage()
            }
        }

        binding.buttonDone.setOnClickListener {
            when {
                viewModel.paymentCardsMerger.value.isNullOrEmpty() -> {
                    findNavController().popBackStack()
                }
                isNetworkAvailable(requireActivity(), true) -> {
                    viewModel.membershipCard.value?.let {
                        adapter.paymentCards.forEach { pllItem ->
                            if (pllItem is PllAdapterItem.PaymentCardItem) {
                                if (pllItem.isSelected &&
                                    !pllItem.paymentCard.isLinkedToMembershipCard(it)
                                ) {
                                    selectedCards.add(pllItem.paymentCard)
                                } else if (viewModel.membershipCard.value != null &&
                                    !pllItem.isSelected &&
                                    pllItem.paymentCard.isLinkedToMembershipCard(it)
                                ) {
                                    unselectedCards.add(pllItem.paymentCard)
                                }
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
                showLinkErrorMessage()
            }
        }
    }

    private fun showLinkErrorMessage() {
        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.pll_error_title))
            .setMessage(getString(R.string.pll_error_message))
            .setPositiveButton(
                getString(R.string.ok)
            ) { dialog, _ ->
                dialog.dismiss()
                navigateToLCD()

            }
            .show()
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

    companion object {
        private fun List<PaymentCard>.toPllPaymentCardWrapperList(
            isAddJourney: Boolean,
            membershipCard: MembershipCard
        ): List<PllAdapterItem.PaymentCardItem> {
            val listPaymentCards = mutableListOf<PllAdapterItem.PaymentCardItem>()
            this.forEach { card ->
                val isSelected = if (isAddJourney) {
                    true
                } else {
                    card.isLinkedToMembershipCard(membershipCard)
                }
                listPaymentCards.add(
                    PllAdapterItem.PaymentCardItem(
                        card,
                        isSelected
                    )
                )
            }
            return listPaymentCards
        }
    }
}
