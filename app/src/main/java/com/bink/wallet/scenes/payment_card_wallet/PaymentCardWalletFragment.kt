package com.bink.wallet.scenes.payment_card_wallet

import android.content.DialogInterface
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.bink.wallet.BaseFragment
import com.bink.wallet.R
import com.bink.wallet.data.SharedPreferenceManager
import com.bink.wallet.databinding.PaymentCardWalletFragmentBinding
import com.bink.wallet.model.JoinCardItem
import com.bink.wallet.model.response.membership_card.MembershipCard
import com.bink.wallet.model.response.membership_plan.MembershipPlan
import com.bink.wallet.model.response.payment_card.PaymentCard
import com.bink.wallet.scenes.loyalty_wallet.RecyclerItemTouchHelper
import com.bink.wallet.scenes.wallets.WalletsFragmentDirections
import com.bink.wallet.utils.JOIN_CARD
import com.bink.wallet.utils.navigateIfAdded
import com.bink.wallet.utils.observeNonNull
import com.bink.wallet.utils.toolbar.FragmentToolbar
import com.bink.wallet.utils.verifyAvailableNetwork
import kotlinx.coroutines.runBlocking
import org.koin.androidx.viewmodel.ext.android.viewModel

class PaymentCardWalletFragment :
    BaseFragment<PaymentCardWalletViewModel, PaymentCardWalletFragmentBinding>() {

    override fun builder(): FragmentToolbar {
        return FragmentToolbar.Builder()
            .withId(FragmentToolbar.NO_TOOLBAR)
            .build()
    }

    private val walletItems = ArrayList<Any>()

    private val walletAdapter = PaymentCardWalletAdapter()

    override val layoutRes: Int
        get() = R.layout.payment_card_wallet_fragment

    override val viewModel: PaymentCardWalletViewModel by viewModel()


    val listener: RecyclerItemTouchHelper.RecyclerItemTouchHelperListener = object :
        RecyclerItemTouchHelper.RecyclerItemTouchHelperListener {
        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int, position: Int) {
            if (viewModel.paymentCards.value != null &&
                viewHolder is PaymentCardWalletAdapter.PaymentCardWalletHolder &&
                direction == ItemTouchHelper.LEFT
            ) {
                viewModel.paymentCards.value?.get(position)?.let { deleteDialog(it) }
            }
            if (direction == ItemTouchHelper.RIGHT) {
                binding.paymentCardRecycler.adapter?.notifyDataSetChanged()
            }
        }
    }

    fun deleteDialog(paymentCard: PaymentCard) {
        val dialog: AlertDialog
        val builder = requireContext().let { AlertDialog.Builder(it) }
        builder.setCancelable(false)
        builder.setTitle(getString(R.string.loayalty_wallet_dialog_title))
        val dialogClickListener = DialogInterface.OnClickListener { _, which ->
            when (which) {
                DialogInterface.BUTTON_POSITIVE -> {
                    if (verifyAvailableNetwork(requireActivity())) {
                        runBlocking {
                            viewModel.deletePaymentCard(paymentCard.id.toString())
                        }
                    } else {
                        showNoInternetConnectionDialog(R.string.delete_and_update_card_internet_connection_error_message)
                    }
                    binding.paymentCardRecycler.adapter?.notifyDataSetChanged()
                }
                DialogInterface.BUTTON_NEUTRAL -> {
                    binding.paymentCardRecycler.adapter?.notifyDataSetChanged()
                }
            }
        }
        builder.setPositiveButton(getString(R.string.yes_text), dialogClickListener)
        builder.setNeutralButton(getString(R.string.cancel_text_upper), dialogClickListener)
        dialog = builder.create()
        dialog.show()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        populateWallet()

        fetchPaymentCards()

        binding.swipeRefresh.setOnRefreshListener {
            binding.swipeRefresh.isRefreshing = false
            fetchPaymentCards()
        }

        viewModel.deleteRequest.observeNonNull(this) {
            viewModel.fetchLocalPaymentCards()
        }

        binding.paymentCardRecycler.apply {
            layoutManager = GridLayoutManager(context, 1)
            adapter = walletAdapter


            val helperListenerLeft =
                RecyclerItemTouchHelper(
                    0,
                    ItemTouchHelper.LEFT,
                    listener
                )

            ItemTouchHelper(helperListenerLeft).attachToRecyclerView(this)
        }

        viewModel.localMembershipPlanData.observeNonNull(this) { plans ->
            viewModel.localMembershipCardData.observeNonNull(this) { cards ->
                viewModel.paymentCards.observeNonNull(this) { paymentCards ->
                    viewModel.dismissedCardData.observeNonNull(this) { dismissedCards ->
                        binding.paymentCardRecycler.visibility = View.VISIBLE
                        binding.progressSpinner.visibility = View.GONE



                        SharedPreferenceManager.isPaymentEmpty = paymentCards.isNullOrEmpty()

                        walletItems.clear()

                        if (dismissedCards.firstOrNull { it.id == JOIN_CARD } == null &&
                            SharedPreferenceManager.isPaymentEmpty) {
                            if (!SharedPreferenceManager.isPaymentJoinHidden) {
                                walletItems.add(JoinCardItem())
                            }
                        }

                        walletItems.addAll(paymentCards)

                        walletAdapter.paymentCards = walletItems

                        walletAdapter.onClickListener = {
                            clickHandler(it, plans, cards)
                        }

                        walletAdapter.onRemoveListener = {
                            onBannerRemove(it)
                        }

                        walletAdapter.notifyDataSetChanged()
                    }
                }
            }
        }
    }

    private fun populateWallet() {
        viewModel.fetchLocalMembershipPlans()
        viewModel.fetchLocalMembershipCards()
        viewModel.fetchLocalPaymentCards()
        viewModel.fetchDismissedCards()
    }

    private fun onBannerRemove(item: Any) {
        SharedPreferenceManager.isPaymentJoinHidden = true
        viewModel.addPlanIdAsDismissed(JOIN_CARD)
        walletAdapter.paymentCards.remove(item)
        walletAdapter.notifyDataSetChanged()
    }

    private fun clickHandler(it: Any, plans: List<MembershipPlan>, cards: List<MembershipCard>) {
        when (it) {
            is PaymentCard -> {
                val action =
                    WalletsFragmentDirections.paymentWalletToDetails(
                        it,
                        plans.toTypedArray(),
                        cards.toTypedArray()
                    )
                findNavController().navigateIfAdded(
                    this@PaymentCardWalletFragment,
                    action
                )
            }
            else -> {
                findNavController().navigateIfAdded(
                    this@PaymentCardWalletFragment,
                    WalletsFragmentDirections.homeToPcd()
                )
            }
        }
    }

    private fun fetchPaymentCards() {

        if (verifyAvailableNetwork(requireActivity())) {
            runBlocking {
                binding.progressSpinner.visibility = View.VISIBLE
                binding.paymentCardRecycler.visibility = View.GONE
                viewModel.getPaymentCards()
            }
        } else {
            showNoInternetConnectionDialog()
        }
    }

    fun setData(
        membershipCards: List<MembershipCard>,
        membershipPlans: List<MembershipPlan>
    ) {
        viewModel.run {
            localMembershipCardData.value = membershipCards
            localMembershipPlanData.value = membershipPlans
        }
    }
}
