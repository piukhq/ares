package com.bink.wallet.scenes.transactions_screen

import android.os.Bundle
import androidx.recyclerview.widget.GridLayoutManager
import com.bink.wallet.BaseFragment
import com.bink.wallet.R
import com.bink.wallet.databinding.TransactionFragmentBinding
import kotlinx.android.synthetic.main.transaction_fragment.*
import org.koin.androidx.viewmodel.ext.android.viewModel

class TransactionsFragment : BaseFragment<TransactionViewModel, TransactionFragmentBinding>() {

    override val layoutRes: Int
        get() = R.layout.transaction_fragment

    override val viewModel: TransactionViewModel by viewModel()

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        arguments?.let {
            viewModel.membershipCard.value =
                TransactionsFragmentArgs.fromBundle(it).membershipCard
            viewModel.membershipPlan.value =
                TransactionsFragmentArgs.fromBundle(it).membershipPlan
            binding.viewModel = viewModel
        }

        binding.toolbar.setNavigationIcon(R.drawable.ic_back)
        binding.toolbar.setNavigationOnClickListener {
            activity?.onBackPressed()
        }

        viewModel.membershipCard.observeForever {
            transactions_list.apply {
                layoutManager = GridLayoutManager(activity, 1)
                adapter = TransactionAdapter(it.membership_transactions)
            }
        }
    }

}
