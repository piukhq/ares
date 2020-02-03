package com.bink.wallet.scenes.pll

import android.app.AlertDialog
import android.os.Bundle
import androidx.lifecycle.Observer
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
import com.bink.wallet.utils.UtilFunctions.isNetworkAvailable
import com.bink.wallet.utils.toolbar.FragmentToolbar
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
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

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        binding.viewModel = viewModel
        arguments?.let {
            PllFragmentArgs.fromBundle(it).apply {
                viewModel.membershipPlan.value = membershipPlan
                viewModel.membershipCard.value = membershipCard
                displayTitle(membershipCard.payment_cards?.isNullOrEmpty()!!)
                if (isAddJourney) {
                    this@PllFragment.isAddJourney = isAddJourney
                    binding.toolbar.navigationIcon = null
                }
                SharedPreferenceManager.isAddJourney = isAddJourney
            }
        }

        binding.brandHeader.setOnClickListener {
            viewModel.membershipPlan.value?.account?.plan_description?.let { planDescription ->
                findNavController().navigateIfAdded(
                    this,
                    PllFragmentDirections.pllToBrandHeader(
                        GenericModalParameters(
                            R.drawable.ic_close,
                            true,
                            viewModel.membershipPlan.value?.account?.plan_name
                                ?: getString(R.string.plan_description),
                            planDescription
                        )
                    )
                )
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

        val adapter = PllPaymentCardAdapter(viewModel.membershipCard.value)

        viewModel.paymentCards.observeNonNull(this) {
            viewModel.membershipCard.value?.let { membershipCard ->
                adapter.notifyChanges(it.toPllPaymentCardWrapperList(isAddJourney, membershipCard))
            }
        }

        binding.paymentCards.adapter = adapter
        binding.paymentCards.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        viewModel.localPaymentCards.observeNonNull(this) {
            viewModel.membershipCard.value?.let { membershipCard ->
                adapter.paymentCards = it.toPllPaymentCardWrapperList(isAddJourney, membershipCard)
                adapter.notifyDataSetChanged()
            }
        }

        viewModel.membershipCard.observeNonNull(this) {
            directions =
                viewModel.membershipCard.value?.let { membershipCard ->
                    viewModel.membershipPlan.value?.let { membershipPlan ->
                        PllFragmentDirections.pllToLcd(
                            membershipPlan, membershipCard
                        )
                    }
                }
        }

        binding.buttonDone.setOnClickListener {
            when {
                viewModel.paymentCards.value.isNullOrEmpty() &&
                        viewModel.localPaymentCards.value.isNullOrEmpty() -> {
                    findNavController().popBackStack()
                }
                isNetworkAvailable(requireActivity(), true) -> {
                    // display loading indicator?
                    val jobs = ArrayList<Deferred<Unit>>()

                    adapter.paymentCards.forEach { card ->
                        if (card.isSelected &&
                            !card.paymentCard.isLinkedToMembershipCard(viewModel.membershipCard.value!!)
                        ) {
                            runBlocking {
                                viewModel.membershipCard.value?.id?.toInt()?.let { membershipCard ->
                                    card.paymentCard.id?.let { paymentCard ->
                                        jobs.add(
                                            async {
                                                viewModel.linkPaymentCard(
                                                    membershipCard.toString(),
                                                    paymentCard.toString()
                                                )
                                            }
                                        )
                                    }
                                }
                            }
                        } else if (viewModel.membershipCard.value != null &&
                            !card.isSelected &&
                            card.paymentCard.isLinkedToMembershipCard(viewModel.membershipCard.value!!)
                        ) {
                            runBlocking {
                                jobs.add(
                                    async {
                                        viewModel.unlinkPaymentCard(
                                            card.paymentCard.id.toString(),
                                            viewModel.membershipCard.value!!.id
                                        )
                                    }
                                )
                            }
                        }
                    }

                    runBlocking {
                        jobs.forEach {
                            it.await()
                        }
                        if (findNavController().currentDestination?.id == R.id.pll_fragment) {
                            directions?.let { directions ->
                                findNavController().navigateIfAdded(
                                    this@PllFragment,
                                    directions
                                )
                            }
                        }
                    }
                }
            }
        }

        viewModel.fetchError.observeNonNull(this) {
            if (!UtilFunctions.hasCertificatePinningFailed(it, requireContext())) {
                requireContext().displayModalPopup(
                    null,
                    getString(R.string.error_description)
                )
            }
        }

        viewModel.linkError.observeNonNull(this) {
            if (!UtilFunctions.hasCertificatePinningFailed(it, requireContext())) {
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

        viewModel.unlinkError.observeNonNull(this) {
            if (!UtilFunctions.hasCertificatePinningFailed(it, requireContext())) {
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

    private fun displayTitle(hasLinkedCards: Boolean) {
        viewModel.title.set(
            getString(
                if (hasLinkedCards) {
                    R.string.pll_linked_title
                } else {
                    R.string.pll_unlinked_title
                }
            )
        )
    }

    companion object {
        private fun List<PaymentCard>.toPllPaymentCardWrapperList(
            isAddJourney: Boolean,
            membershipCard: MembershipCard
        ): List<PllPaymentCardWrapper> {
            val listPaymentCards = mutableListOf<PllPaymentCardWrapper>()
            this.forEach { card ->
                val isSelected = if (isAddJourney) {
                    true
                } else {
                    card.isLinkedToMembershipCard(membershipCard)
                }
                listPaymentCards.add(
                    PllPaymentCardWrapper(
                        card,
                        isSelected
                    )
                )
            }
            return listPaymentCards
        }
    }
}
