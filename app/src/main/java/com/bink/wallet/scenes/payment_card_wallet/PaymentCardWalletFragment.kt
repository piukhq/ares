package com.bink.wallet.scenes.payment_card_wallet

import android.content.DialogInterface
import android.content.Intent
import android.graphics.Canvas
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
import com.bink.wallet.scenes.loyalty_wallet.LoyaltyWalletAdapter
import com.bink.wallet.utils.*
import com.bink.wallet.utils.FirebaseEvents.DELETE_PAYMENT_CARD_REQUEST
import com.bink.wallet.utils.FirebaseEvents.DELETE_PAYMENT_CARD_RESPONSE_FAILURE
import com.bink.wallet.utils.FirebaseEvents.DELETE_PAYMENT_CARD_RESPONSE_SUCCESS
import com.bink.wallet.utils.FirebaseEvents.PAYMENT_WALLET_VIEW
import com.bink.wallet.utils.UtilFunctions.isNetworkAvailable
import com.bink.wallet.utils.toolbar.FragmentToolbar
import kotlinx.android.synthetic.main.loyalty_wallet_item.view.*
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

    private var paymentScheme: String? = null

    private var uuid: String? = null

    override val layoutRes: Int
        get() = R.layout.payment_card_wallet_fragment

    override val viewModel: PaymentCardWalletViewModel by viewModel()

    private var isRefreshing = false

    override fun onResume() {
        super.onResume()

        viewModel.getPeriodicPaymentCards()

        logScreenView(PAYMENT_WALLET_VIEW)

        if (ZendeskUtils.hasResponseBeenReceived()) {
            binding.settingsButton.setImageResource(R.drawable.ic_settings_notified)
        } else {
            binding.settingsButton.setImageResource(R.drawable.ic_settings)

        }
    }

    private var simpleCallback: ItemTouchHelper.SimpleCallback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
        override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
            return false
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            val position = viewHolder.adapterPosition
            if (viewModel.paymentCards.value != null && viewHolder is PaymentCardWalletAdapter.PaymentCardWalletHolder && direction == ItemTouchHelper.LEFT) {
                if (!viewModel.paymentCards.value.isNullOrEmpty()) {
                    viewModel.paymentCards.value?.get(position)?.let { deleteDialog(it) }
                }
            }
        }

        override fun onChildDraw(c: Canvas, recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean) {
            val foregroundView = when (viewHolder) {
                is PaymentCardWalletAdapter.PaymentCardWalletHolder ->
                    viewHolder.binding.mainPayment
                else ->
                    null
            }

            if (foregroundView != null) {

                when {
                    dX >= 0 -> {
                        viewHolder.itemView.barcode_layout.visibility = View.VISIBLE
                        viewHolder.itemView.delete_layout.visibility = View.GONE
                    }
                    else -> {
                        viewHolder.itemView.barcode_layout.visibility = View.GONE
                        viewHolder.itemView.delete_layout.visibility = View.VISIBLE
                    }
                }

                getDefaultUIUtil().onDraw(c, recyclerView, foregroundView, dX, dY, actionState, isCurrentlyActive)
            }
        }

        override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
            val foregroundView = when (viewHolder) {
                is PaymentCardWalletAdapter.PaymentCardWalletHolder ->
                    viewHolder.binding.mainPayment
                else ->
                    null
            }

            if (foregroundView != null) {
                getDefaultUIUtil().clearView(foregroundView)
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
                        val paymentSchemeValue = paymentCard.card?.provider
                        val uuidValue = paymentCard.uuid
                        paymentScheme = paymentSchemeValue
                        this.uuid = uuidValue

                        if (paymentSchemeValue == null || uuidValue == null) {
                            failedEvent(DELETE_PAYMENT_CARD_REQUEST)
                        } else {
                            logEvent(
                                DELETE_PAYMENT_CARD_REQUEST,
                                getDeletePaymentCardGenericMap(paymentSchemeValue, uuidValue)
                            )
                        }

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
            isRefreshing = true
            viewModel.getPaymentCards()
        }

        viewModel.localMembershipCardData.observeNonNull(this) {
            walletAdapter.membershipCards = it.toMutableList()
        }

        viewModel.deleteRequest.observeNonNull(this) {
            viewModel.fetchLocalData()
            val pScheme = paymentScheme
            val uuid = this.uuid
            if (pScheme == null || uuid == null) {
                failedEvent(DELETE_PAYMENT_CARD_RESPONSE_SUCCESS)
            } else {
                logEvent(
                    DELETE_PAYMENT_CARD_RESPONSE_SUCCESS,
                    getDeletePaymentCardGenericMap(pScheme, uuid)
                )
            }

        }

        viewModel.deleteError.observeNonNull(this) {
            viewModel.fetchLocalData()
            val pScheme = paymentScheme
            val uuid = this.uuid
            if (pScheme == null || uuid == null) {
                failedEvent(DELETE_PAYMENT_CARD_RESPONSE_FAILURE)
            } else {
                logEvent(
                    DELETE_PAYMENT_CARD_RESPONSE_FAILURE,
                    getDeletePaymentCardGenericMap(pScheme, uuid)
                )
            }


        }
        viewModel.deleteCardError.observeErrorNonNull(requireContext(), true, this)

        viewModel.deleteError.observeErrorNonNull(requireContext(), true, this)

        binding.paymentCardRecycler.apply {
            layoutManager = GridLayoutManager(context, 1)
            adapter = walletAdapter

            ItemTouchHelper(simpleCallback).attachToRecyclerView(this)
        }

        viewModel.paymentCards.observeNonNull(this) {
            if (isRefreshing) {
                isRefreshing = false
                binding.swipeRefresh.isRefreshing = false
                updatePaymentCardList()
            }
        }

        viewModel.loyaltyUpdateDone.observeNonNull(this) { loyaltyUpdateDone ->
            viewModel.paymentUpdateDone.observeNonNull(this) { paymentUpdateDone ->
                if (loyaltyUpdateDone && paymentUpdateDone) {
                    updatePaymentCardList()
                }
            }
        }

        viewModel.fetchError.observeErrorNonNull(requireContext(), this, isRefreshing) {
            isRefreshing = false
            binding.swipeRefresh.isRefreshing = false
            viewModel.fetchLocalData()
        }
        binding.settingsButton.setOnClickListener {
            findNavController().navigateIfAdded(this, R.id.settings_screen)
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
                    PaymentCardWalletFragmentDirections.paymentWalletToDetails(
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
                requestCameraPermissionAndNavigate(false, null)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        scanResult(
            requestCode,
            resultCode,
            data,
            { navigateToPaymentAddPaymentCard(it) },
            { logPaymentCardSuccess(it) })
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        requestPermissionsResult(
            requestCode,
            permissions,
            grantResults,
            null,
            { navigateToPaymentAddPaymentCard() },
            null
        )
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    private fun navigateToPaymentAddPaymentCard(cardNumber: String = "") {
        val directions = PaymentCardWalletFragmentDirections.paymentWalletToAddPaymentCard(
            cardNumber
        )
        findNavController().navigateIfAdded(this, directions)
    }

    private fun fetchPaymentCards(isRefreshing: Boolean) {
        if (isNetworkAvailable(requireActivity(), isRefreshing)) {
            binding.paymentCardRecycler.visibility = View.GONE
            viewModel.getPeriodicPaymentCards()
        }
    }

    //TODO Refactor: Why are we setting the data for payment here? Should be done in PaymentWalletFragment
    fun setData(
        membershipCards: List<MembershipCard>,
        membershipPlans: List<MembershipPlan>
    ) {
        viewModel.run {
            localMembershipCardData.value = membershipCards
            localMembershipPlanData.value = membershipPlans
        }
    }

    private fun updatePaymentCardList() {
        binding.paymentCardRecycler.visibility = View.VISIBLE

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
            walletItems.addAll(paymentCards.sortedByDescending { card -> card.id })
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
