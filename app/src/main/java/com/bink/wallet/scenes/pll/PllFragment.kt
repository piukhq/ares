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
import com.bink.wallet.model.response.payment_card.PllPaymentCardWrapper
import com.bink.wallet.utils.*
import com.bink.wallet.utils.FirebaseEvents.PLL_VIEW
import com.bink.wallet.utils.FirebaseEvents.getFirebaseIdentifier
import com.bink.wallet.utils.UtilFunctions.isNetworkAvailable
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
    val unselectedCards = mutableListOf<String>()
    val selectedCards = mutableListOf<String>()

    override fun onResume() {
        super.onResume()
        logScreenView(PLL_VIEW)
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

//        binding.brandHeader.setOnClickListener {
//            viewModel.membershipPlan.value?.account?.plan_description?.let { planDescription ->
//                findNavController().navigateIfAdded(
//                    this,
//                    PllFragmentDirections.pllToBrandHeader(
//                        GenericModalParameters(
//                            R.drawable.ic_close,
//                            true,
//                            viewModel.membershipPlan.value?.account?.plan_name
//                                ?: getString(R.string.plan_description),
//                            planDescription
//                        )
//                    )
//                )
//            }
//        }

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
                    add(PllAdapterItem.PllBrandHeaderItem(viewModel.membershipPlan.value!!))
                    add(PllAdapterItem.PllTitleItem)
                    add(PllAdapterItem.PllDescriptionItem(viewModel.membershipPlan.value?.account?.plan_name_card!!))
                    addAll(it.toPllPaymentCardWrapperList(isAddJourney, membershipCard))
                }
                adapter.paymentCards = adapterItems
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
            if (it.size == selectedCards.size) {
                navigateToLCD()
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
                            if (pllItem is PllAdapterItem.PaymentCardWrapperItem) {
                                val card = pllItem.pllPaymentCardWrapper
                                if (card.isSelected &&
                                    !card.paymentCard.isLinkedToMembershipCard(it)
                                ) {
                                    selectedCards.add(card.paymentCard.id.toString())
                                } else if (viewModel.membershipCard.value != null &&
                                    !card.isSelected &&
                                    card.paymentCard.isLinkedToMembershipCard(it)
                                ) {
                                    unselectedCards.add(card.paymentCard.id.toString())
                                }
                            }
                        }
                    }

                    viewModel.membershipCard.value?.id?.let {
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
        ): List<PllAdapterItem.PaymentCardWrapperItem> {
            val listPaymentCards = mutableListOf<PllAdapterItem.PaymentCardWrapperItem>()
            this.forEach { card ->
                val isSelected = if (isAddJourney) {
                    true
                } else {
                    card.isLinkedToMembershipCard(membershipCard)
                }
                listPaymentCards.add(
                    PllAdapterItem.PaymentCardWrapperItem(
                        PllPaymentCardWrapper(
                            card,
                            isSelected
                        )
                    )
                )
            }
            return listPaymentCards
        }
    }
}
