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
import com.bink.wallet.utils.*
import com.bink.wallet.utils.FirebaseEvents.PAYMENT_WALLET_VIEW
import com.bink.wallet.utils.UtilFunctions.isNetworkAvailable
import com.bink.wallet.utils.toolbar.FragmentToolbar
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

    override fun onResume() {
        super.onResume()

        viewModel.getPeriodicPaymentCards()

        logScreenView(PAYMENT_WALLET_VIEW)
    }

    val listener: RecyclerItemTouchHelper.RecyclerItemTouchHelperListener = object :
        RecyclerItemTouchHelper.RecyclerItemTouchHelperListener {
        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int, position: Int) {
            if (viewModel.paymentCards.value != null &&
                viewHolder is PaymentCardWalletAdapter.PaymentCardWalletHolder &&
                direction == ItemTouchHelper.LEFT
            ) {
                if (!viewModel.paymentCards.value.isNullOrEmpty()) {
                    viewModel.paymentCards.value?.get(position)?.let { deleteDialog(it) }
                }
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
        builder.setTitle(getString(R.string.loyalty_wallet_dialog_title))
        val dialogClickListener = DialogInterface.OnClickListener { _, which ->
            when (which) {
                DialogInterface.BUTTON_POSITIVE -> {
                    if (isNetworkAvailable(requireActivity(), true)) {
                        viewModel.deletePaymentCard(paymentCard.id.toString())
                        binding.paymentCardRecycler.adapter?.notifyDataSetChanged()
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

        populateWallet()

        fetchPaymentCards(false)

        binding.swipeRefresh.setOnRefreshListener {
            binding.swipeRefresh.isRefreshing = false
            viewModel.fetchLocalData()

            viewModel.getPaymentCards()
        }

        viewModel.localMembershipCardData.observeNonNull(this) {
            walletAdapter.membershipCards = it.toMutableList()
        }

        viewModel.deleteRequest.observeNonNull(this) {
            viewModel.fetchLocalData()
        }

        viewModel.deleteCardError.observeErrorNonNull(requireContext(), true, this)

        viewModel.deleteError.observeErrorNonNull(requireContext(), true, this)

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

        viewModel.loyaltyUpdateDone.observeNonNull(this) { loyaltyUpdateDone ->
            viewModel.paymentUpdateDone.observeNonNull(this) { paymentUpdateDone ->
                if (loyaltyUpdateDone && paymentUpdateDone) {
                    binding.paymentCardRecycler.visibility = View.VISIBLE
                    binding.progressSpinner.visibility = View.GONE

                    SharedPreferenceManager.isPaymentEmpty =
                        viewModel.paymentCards.value.isNullOrEmpty()

                    walletItems.clear()

                    if (viewModel.dismissedCardData.value?.firstOrNull { it.id == JOIN_CARD } == null &&
                        SharedPreferenceManager.isPaymentEmpty) {
                        if (!SharedPreferenceManager.isPaymentJoinBannerDismissed) {
                            walletItems.add(JoinCardItem())
                        }
                    }

                    viewModel.paymentCards.value?.let { paymentCards ->
                        walletItems.addAll(paymentCards)
                    }

                    walletAdapter.paymentCards = walletItems

                    viewModel.localMembershipPlanData.value?.let { plans ->
                        viewModel.localMembershipCardData.value?.let { cards ->
                            walletAdapter.onClickListener = {
                                clickHandler(it, plans, cards)
                            }
                        }
                    }

                    walletAdapter.onRemoveListener = {
                        onBannerRemove(it)
                    }

                    walletAdapter.notifyDataSetChanged()
                }
            }
        }
    }

    private fun populateWallet() {
        viewModel.fetchLocalData()
    }

    private fun onBannerRemove(item: Any) {
        SharedPreferenceManager.isPaymentJoinBannerDismissed = true
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

    private fun fetchPaymentCards(isRefreshing: Boolean) {
        if (isNetworkAvailable(requireActivity(), isRefreshing)) {
            binding.progressSpinner.visibility = View.VISIBLE
            binding.paymentCardRecycler.visibility = View.GONE
            viewModel.getPeriodicPaymentCards()
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
