package com.bink.wallet.scenes.pll

import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.bink.wallet.BaseFragment
import com.bink.wallet.R
import com.bink.wallet.databinding.FragmentPllBinding
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

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        binding.viewModel = viewModel
        arguments?.let {
            PllFragmentArgs.fromBundle(it).apply {
                viewModel.membershipPlan.value = membershipPlan
                viewModel.membershipCard.value = membershipCard
                viewModel.title.set(getString(R.string.pll_unlinked_title)).takeIf { membershipCard.payment_cards?.isNullOrEmpty()!! }
                    ?: viewModel.title.set(getString(R.string.pll_unlinked_title))

            }
        }
        runBlocking {
            viewModel.getPaymentCards()
        }
        val adapter = PllPaymentCardAdapter()
        binding.paymentCards.adapter = adapter
        binding.paymentCards.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        viewModel.paymentCards.observeNonNull(this) {
            adapter.paymentCards = it
            adapter.notifyDataSetChanged()

        }
    }
}