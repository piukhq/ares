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
import com.bink.wallet.model.response.payment_card.PllPaymentCardWrapper
import com.bink.wallet.scenes.add_join.AddJoinFragmentDirections
import com.bink.wallet.utils.displayModalPopup
import com.bink.wallet.utils.isLinkedToMembershipCard
import com.bink.wallet.utils.navigateIfAdded
import com.bink.wallet.utils.observeNonNull
import com.bink.wallet.utils.toolbar.FragmentToolbar
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.runBlocking
import org.koin.androidx.viewmodel.ext.android.viewModel

class PllFragment: BaseFragment<PllViewModel, FragmentPllBinding>() {
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
            val directions = viewModel.membershipPlan.value?.account?.plan_description?.let { message ->
                GenericModalParameters(
                    R.drawable.ic_close,
                    getString(R.string.plan_description),
                    message, getString(R.string.ok)
                )
            }?.let { params -> PllFragmentDirections.pllToBrandHeader(params) }
            directions?.let { _ -> findNavController().navigateIfAdded(this, directions) }
        }

        runBlocking {
            viewModel.getPaymentCards()
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

        viewModel.paymentCards.observeNonNull(this) {
            runBlocking {
                viewModel.getLocalPaymentCards()
            }
        }

        val adapter = PllPaymentCardAdapter(viewModel.membershipCard.value, null)
        binding.paymentCards.adapter = adapter
        binding.paymentCards.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        viewModel.localPaymentCards.observeNonNull(this) {
            val listPaymentCards = mutableListOf<PllPaymentCardWrapper>()
            it.forEach { card ->
                val isSelected = if (isAddJourney) {
                    true
                } else {
                    card.isLinkedToMembershipCard(viewModel.membershipCard.value!!)
                }
                listPaymentCards.add(
                    PllPaymentCardWrapper(
                        card,
                        isSelected
                    )
                )
            }
            adapter.paymentCards = listPaymentCards
            adapter.notifyDataSetChanged()
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
            adapter.paymentCards?.forEach { card ->
                if (card.isSelected
                    && !card.paymentCard.isLinkedToMembershipCard(viewModel.membershipCard.value!!)
                ) {
                    runBlocking {
                        viewModel.membershipCard.value?.id?.toInt()?.let { membershipCard ->
                            card.paymentCard.id?.let { paymentCard ->
                                viewModel.linkPaymentCard(
                                    membershipCard.toString(), paymentCard.toString()
                                )
                            }
                        }
                    }
                } else if (viewModel.membershipCard.value != null
                    && !card.isSelected
                    && card.paymentCard.isLinkedToMembershipCard(viewModel.membershipCard.value!!)
                ) {
                    runBlocking {
                        viewModel.unlinkPaymentCard(
                            card.paymentCard.id.toString(),
                            viewModel.membershipCard.value!!.id
                        )
                    }
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

        viewModel.fetchError.observeNonNull(this) { throwable ->
            Snackbar.make(binding.root, throwable.toString(), Snackbar.LENGTH_SHORT).show()
        }

        viewModel.localFetchError.observeNonNull(this) { throwable ->
            Snackbar.make(binding.root, throwable.toString(), Snackbar.LENGTH_SHORT).show()
        }

        viewModel.linkError.observeNonNull(this) {
            AlertDialog.Builder(requireContext())
                .setTitle(getString(R.string.description_error))
                .setMessage(it.toString())
                .setPositiveButton(
                    getString(R.string.ok)
                ) { _, _ ->
                    if (findNavController().currentDestination?.id == R.id.pll_fragment)
                        directions?.let { directions ->
                            findNavController().navigateIfAdded(
                                this@PllFragment,
                                directions
                            )
                        }
                }
                .show()
        }

        viewModel.unlinkError.observeNonNull(this) {
            requireContext().displayModalPopup(getString(R.string.description_error), it.toString())
        }
    }

    private fun displayTitle(hasLinkedCards: Boolean) {
        if (hasLinkedCards) {
            viewModel.title.set(getString(R.string.pll_linked_title))
        } else {
            viewModel.title.set(getString(R.string.pll_unlinked_title))
        }
    }
}
