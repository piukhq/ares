package com.bink.wallet.scenes.transactions_screen

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import com.bink.wallet.BaseFragment
import com.bink.wallet.R
import com.bink.wallet.data.SharedPreferenceManager
import com.bink.wallet.databinding.TransactionFragmentBinding
import com.bink.wallet.modal.generic.GenericModalParameters
import com.bink.wallet.model.TransactionHistory
import com.bink.wallet.utils.navigateIfAdded
import com.bink.wallet.utils.textAndShow
import com.bink.wallet.utils.toolbar.FragmentToolbar
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.lang.reflect.Type

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
                            planDescription,
                            firstButtonText = getString(R.string.go_to_site)
                        ), viewModel.membershipPlan.value?.account?.plan_url ?: ""
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

                /**
                 * The way this is written ensures there can only be a single entry for each type of card.
                 */

                val gson = Gson()
                val type: Type = object : TypeToken<ArrayList<TransactionHistory?>?>() {}.type

                var previousTransactionHistoryVisitList: ArrayList<TransactionHistory> = arrayListOf()

                SharedPreferenceManager.lastSeenTransactions?.let {
                    previousTransactionHistoryVisitList = gson.fromJson(it, type)
                }

                val currentTransactionHistoryVisit = TransactionHistory(membershipCard.id, membershipCard.membership_transactions?.size ?: 0)

                val previousMatchingVisit = previousTransactionHistoryVisitList.firstOrNull { it.membershipId.equals(membershipCard.id) }

                if (previousMatchingVisit != null) {
                    SharedPreferenceManager.hasNewTransactions = previousMatchingVisit.transactionSize > membershipCard.membership_transactions?.size ?: 0
                    val foundIndex =
                        previousTransactionHistoryVisitList.indexOfFirst { it.membershipId.equals(membershipCard.id) && it.transactionSize == membershipCard.membership_transactions?.size ?: 0 }
                    previousTransactionHistoryVisitList.removeAt(foundIndex)
                } else {
                    SharedPreferenceManager.hasNewTransactions = false
                }

                previousTransactionHistoryVisitList.add(currentTransactionHistoryVisit)
                val newTransactionHistoryVisitList = gson.toJson(previousTransactionHistoryVisitList)
                SharedPreferenceManager.lastSeenTransactions = newTransactionHistoryVisitList

            } else {
                binding.pointsHistory.text = getString(R.string.points_history_not_available_title)
                binding.pointsDescription.textAndShow(
                    getString(
                        R.string.transaction_not_supported_description,
                        viewModel.membershipPlan.value?.account?.plan_name
                    )
                )
            }
        }
    }

}
