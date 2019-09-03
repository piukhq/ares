package com.bink.wallet.scenes.transactions_not_supported

import android.os.Bundle
import com.bink.wallet.BaseFragment
import com.bink.wallet.R
import com.bink.wallet.databinding.TransactionsNotSupportedFragmentBinding
import org.koin.androidx.viewmodel.ext.android.viewModel


class TransactionsNotSupportedFragment :
    BaseFragment<TransactionsNotSupportedViewModel, TransactionsNotSupportedFragmentBinding>() {

    override val layoutRes: Int
        get() = R.layout.transactions_not_supported_fragment

    override val viewModel: TransactionsNotSupportedViewModel by viewModel()

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        arguments?.let {
            viewModel.membershipCard.value =
                TransactionsNotSupportedFragmentArgs.fromBundle(it).membershipCard
            binding.card = viewModel.membershipCard.value
        }

        binding.toolbar.setNavigationIcon(R.drawable.ic_back)
        binding.toolbar.setNavigationOnClickListener {
            activity?.onBackPressed()
        }
    }
}
