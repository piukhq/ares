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
import com.bink.wallet.utils.displayModalPopup
import com.bink.wallet.utils.isLinkedToMembershipCard
import com.bink.wallet.utils.navigateIfAdded
import com.bink.wallet.utils.observeNonNull
import com.bink.wallet.utils.toolbar.FragmentToolbar
import kotlinx.coroutines.runBlocking
import org.koin.androidx.viewmodel.ext.android.viewModel

class PllFragment: BaseFragment<PllViewModel, FragmentPllBinding>() {
    override val layoutRes: Int
        get() = R.layout.fragment_pll
    override val viewModel: PllViewModel by viewModel()

    override fun builder(): FragmentToolbar {
        return FragmentToolbar.Builder()
            .with(binding.toolbar).shouldDisplayBack(requireActivity())
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
                viewModel.title.set(getString(R.string.pll_unlinked_title)).takeIf { membershipCard.payment_cards?.isNullOrEmpty()!! }
                    ?: viewModel.title.set(getString(R.string.pll_linked_title))
                if (isAddJourney) {
                    this@PllFragment.isAddJourney = isAddJourney
                    binding.toolbar.navigationIcon = null
                }
                SharedPreferenceManager.isAddJourney = isAddJourney
            }
        }

        runBlocking {
            viewModel.getPaymentCards()
        }

        viewModel.paymentCards.observeNonNull(this) {
            runBlocking {
                viewModel.getLocalPaymentCards()
            }
        }
        val adapter = PllPaymentCardAdapter(viewModel.membershipCard.value, null, isAddJourney)
        binding.paymentCards.adapter = adapter
        binding.paymentCards.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        viewModel.localPaymentCards.observeNonNull(this) {
            val listPaymentCards = mutableListOf<PllPaymentCardWrapper>()
            it.forEach { card -> listPaymentCards.add(PllPaymentCardWrapper(card)) }
            adapter.paymentCards = listPaymentCards
            adapter.notifyDataSetChanged()

        }

        viewModel.membershipCard.observeNonNull(this) {
            directions =
                viewModel.membershipCard.value?.let { it1 ->
                    viewModel.membershipPlan.value?.let { it2 ->
                        PllFragmentDirections.pllToLcd(
                            it2, it1
                        )
                    }
                }
        }

        binding.buttonDone.setOnClickListener {
            adapter.paymentCards?.forEach { card ->
                if (card.isSelected && !card.paymentCard.isLinkedToMembershipCard(viewModel.membershipCard.value!!)) {
                    runBlocking {
                        viewModel.membershipCard.value?.id?.toInt()?.let { it1 ->
                            card.paymentCard.id?.let { it2 ->
                                viewModel.linkPaymentCard(
                                    it1.toString(), it2.toString()
                                )
                            }
                        }
                    }
                } else if (!card.isSelected && card.paymentCard.isLinkedToMembershipCard(viewModel.membershipCard.value!!)) {
                    runBlocking {
                        viewModel.unlinkPaymentCard(
                            card.paymentCard.id.toString(),
                            viewModel.membershipCard.value!!.id
                        )
                    }
                }

                if (findNavController().currentDestination?.id == R.id.pll_fragment) {
                    directions?.let { it1 ->
                        findNavController().navigateIfAdded(
                            this@PllFragment,
                            it1
                        )
                    }
                }
                }
            }

        viewModel.linkError.observeNonNull(this) {
            AlertDialog.Builder(requireContext())
                .setTitle(getString(R.string.description_error))
                .setMessage(it.toString())
                .setPositiveButton(
                    getString(R.string.ok)
                ) { _, _ ->
                    if (findNavController().currentDestination?.id == R.id.pll_fragment)
                        directions?.let { it1 ->
                            findNavController().navigateIfAdded(
                                this@PllFragment,
                                it1
                            )
                        }
                }
                .show()
        }

        viewModel.unlinkError.observeNonNull(this) {
            requireContext().displayModalPopup(getString(R.string.description_error), it.toString())
        }
    }
}
