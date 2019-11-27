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
import com.bink.wallet.model.response.membership_card.MembershipCard
import com.bink.wallet.model.response.membership_plan.MembershipPlan
import com.bink.wallet.model.response.payment_card.PaymentCard
import com.bink.wallet.scenes.loyalty_wallet.RecyclerItemTouchHelper
import com.bink.wallet.scenes.wallets.WalletsFragmentDirections
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

    val walletAdapter = PaymentCardWalletAdapter()

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
                        showNoInternetConnectionDialog()
                    }
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

        viewModel.fetchLocalMembershipPlans()
        viewModel.fetchLocalMembershipCards()
        viewModel.fetchLocalPaymentCards()

        fetchPaymentCards()

        binding.swipeRefresh.setOnRefreshListener {
            binding.swipeRefresh.isRefreshing = false
            fetchPaymentCards()
        }

        viewModel.deleteRequest.observeNonNull(this) {
            viewModel.fetchLocalPaymentCards()
        }

        viewModel.localMembershipPlanData.observeNonNull(this) { plans ->
            viewModel.localMembershipCardData.observeNonNull(this) { cards ->
                viewModel.paymentCards.observeNonNull(this) { paymentCards ->
                    binding.progressSpinner.visibility = View.GONE

                    SharedPreferenceManager.isPaymentEmpty = paymentCards.isNullOrEmpty()

                    walletItems.clear()

                    if (!SharedPreferenceManager.isPaymentJoinHidden) {
                        walletItems.add(Any())
                    }

                    walletAdapter.onClickListener = {
                        clickHandler(it, plans, cards)
                    }

                    walletAdapter.onRemoveListener = {
                        onBannerRemove(it)
                    }

                    walletAdapter.paymentCards = walletItems

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
                }
            }
        }
    }

    private fun onBannerRemove(item: Any) {
        SharedPreferenceManager.isPaymentJoinHidden = true
        walletItems.remove(item)
        walletAdapter.paymentCards = walletItems
        binding.paymentCardRecycler.adapter?.notifyDataSetChanged()
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
        runBlocking {
            binding.progressSpinner.visibility = View.VISIBLE
            viewModel.getPaymentCards()
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
