package com.bink.wallet.scenes.transactions_not_supported

import android.os.Bundle
import com.bink.wallet.BaseFragment
import com.bink.wallet.R
import com.bink.wallet.databinding.TransactionsNotSupportedFragmentBinding
import com.bink.wallet.utils.toolbar.FragmentToolbar
import org.koin.androidx.viewmodel.ext.android.viewModel


class TransactionsNotSupportedFragment :
    BaseFragment<TransactionsNotSupportedViewModel, TransactionsNotSupportedFragmentBinding>() {
    override fun builder(): FragmentToolbar {
        return FragmentToolbar.Builder()
            .with(binding.toolbar).shouldDisplayBack(requireActivity())
            .build()
    }

    override val layoutRes: Int
        get() = R.layout.transactions_not_supported_fragment

    override val viewModel: TransactionsNotSupportedViewModel by viewModel()

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        arguments?.let {
            viewModel.membershipCard.value =
                TransactionsNotSupportedFragmentArgs.fromBundle(it).membershipCard
            viewModel.logInType.value =
                TransactionsNotSupportedFragmentArgs.fromBundle(it).loginStatus
            binding.card = viewModel.membershipCard.value
        }
    }
}
