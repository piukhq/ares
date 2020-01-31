package com.bink.wallet.scenes.transactions_screen

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.bink.wallet.BaseFragment
import com.bink.wallet.R
import com.bink.wallet.databinding.TransactionFragmentBinding
import com.bink.wallet.modal.generic.GenericModalParameters
import com.bink.wallet.utils.getElapsedTime
import com.bink.wallet.utils.navigateIfAdded
import com.bink.wallet.utils.toolbar.FragmentToolbar
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.*

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

        viewModel.membershipCard.observeForever {
            it.membership_transactions?.let { transactions ->
                if (transactions.isEmpty() ||
                    viewModel.membershipPlan.value?.feature_set?.transactions_available != true
                ) {
                    with (binding) {
                        pointsHistory.text =
                            getString(R.string.points_history_not_available_title)
                        pointsDescription.text =
                            getString(
                                R.string.points_history_not_available_description,
                                viewModel.membershipPlan.value?.account?.plan_name
                            )
                        with (noTransactionsText) {
                            val balance =
                                viewModel.membershipCard.value?.balances?.first()
                            val updateTime = balance?.updated_at
                            val currentTime = Calendar.getInstance().timeInMillis / 1000
                            updateTime?.let { time ->
                                val timeSinceUpdate = currentTime - time
                                visibility = View.VISIBLE
                                text = getString(
                                    R.string.points_history_not_available_period,
                                    timeSinceUpdate.getElapsedTime(requireContext())
                                )
                            }
                        }
                    }
                } else {
                    binding.transactionsList.apply {
                        layoutManager = GridLayoutManager(activity, 1)
                        adapter = TransactionAdapter(transactions)
                    }
                }
            }
        }
    }
}
