package com.bink.wallet.scenes.transactions_screen

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import com.bink.wallet.BaseFragment
import com.bink.wallet.R
import com.bink.wallet.databinding.TransactionFragmentBinding
import com.bink.wallet.modal.generic.GenericModalParameters
import com.bink.wallet.utils.navigateIfAdded
import com.bink.wallet.utils.textAndShow
import com.bink.wallet.utils.toolbar.FragmentToolbar
import org.koin.androidx.viewmodel.ext.android.viewModel

class TransactionsFragment : BaseFragment<TransactionViewModel, TransactionFragmentBinding>() {
    override fun builder(): FragmentToolbar {
        return FragmentToolbar.Builder()
            .with(binding.toolbar).shouldDisplayBack(requireActivity())
            .build()
    }

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

        binding.loyaltyCardHeader.setOnClickListener {
            viewModel.membershipPlan.value?.account?.plan_description?.let { planDescription ->
                findNavController().navigateIfAdded(
                    this,
                    TransactionsFragmentDirections.transactionsToBrandHeader(
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

        viewModel.membershipCard.observeForever { membershipCard ->
            if (membershipCard.plan?.feature_set?.transactions_available == true) {
                membershipCard.membership_transactions?.let { transactions ->
                    if (transactions.isEmpty()) {
                        binding.pointsDescription.text = getString(R.string.no_transactions_text)
                        binding.transactionsList.visibility = View.GONE
                    } else {
                        binding.transactionsList.adapter = TransactionAdapter(transactions)
                    }
                }
            } else {
                binding.pointsHistory.text = getString(R.string.points_history_not_available_title)
             
                binding.pointsDescription.textAndShow(
                    getString(
                        R.string.transaction_not_supported_description,
                        viewModel.membershipPlan.value?.account?.plan_name
                    )
                )

                binding.noTransactionsText.textAndShow(getString(R.string.no_transaction_history_yet))
            }
        }
    }

}
