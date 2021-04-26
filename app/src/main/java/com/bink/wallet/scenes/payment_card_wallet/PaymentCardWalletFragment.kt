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
import com.bink.wallet.model.DynamicAction
import com.bink.wallet.model.DynamicActionArea
import com.bink.wallet.model.DynamicActionLocation
import com.bink.wallet.model.response.membership_card.MembershipCard
import com.bink.wallet.model.response.membership_plan.MembershipPlan
import com.bink.wallet.model.response.payment_card.PaymentCard
import com.bink.wallet.utils.FirebaseEvents.DELETE_PAYMENT_CARD_REQUEST
import com.bink.wallet.utils.FirebaseEvents.DELETE_PAYMENT_CARD_RESPONSE_FAILURE
import com.bink.wallet.utils.FirebaseEvents.DELETE_PAYMENT_CARD_RESPONSE_SUCCESS
import com.bink.wallet.utils.FirebaseEvents.PAYMENT_WALLET_VIEW
import com.bink.wallet.utils.JOIN_CARD
import com.bink.wallet.utils.MembershipPlanUtils
import com.bink.wallet.utils.UtilFunctions.isNetworkAvailable
import com.bink.wallet.utils.WalletOrderingUtil
import com.bink.wallet.utils.getErrorBody
import com.bink.wallet.utils.logPaymentCardSuccess
import com.bink.wallet.utils.navigateIfAdded
import com.bink.wallet.utils.observeErrorNonNull
import com.bink.wallet.utils.observeNonNull
import com.bink.wallet.utils.requestCameraPermissionAndNavigate
import com.bink.wallet.utils.requestPermissionsResult
import com.bink.wallet.utils.scanResult
import com.bink.wallet.utils.toolbar.FragmentToolbar
import kotlinx.android.synthetic.main.loyalty_wallet_item.view.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import retrofit2.HttpException

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

    private var paymentCardId: String? = null

    override val layoutRes: Int
        get() = R.layout.payment_card_wallet_fragment

    override val viewModel: PaymentCardWalletViewModel by viewModel()

    private var isRefreshing = false

    override fun onResume() {
        super.onResume()

        viewModel.getPeriodicPaymentCards()
        viewModel.checkZendeskResponse()

        logScreenView(PAYMENT_WALLET_VIEW)
    }

    private var simpleCallback: ItemTouchHelper.SimpleCallback = object :
        ItemTouchHelper.SimpleCallback(
            ItemTouchHelper.UP + ItemTouchHelper.DOWN,
            ItemTouchHelper.LEFT
        ) {
        override fun onMove(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder
        ): Boolean {
            walletAdapter.onItemMove(viewHolder.adapterPosition, target.adapterPosition)
            return true
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            val position = viewHolder.adapterPosition
            val paymentCards = walletAdapter.paymentCards
            if (!paymentCards.isNullOrEmpty() && viewHolder is PaymentCardWalletAdapter.PaymentCardWalletHolder && direction == ItemTouchHelper.LEFT) {
                try {
                    deleteDialog(paymentCards[position] as PaymentCard)
                } catch (e: ClassCastException) {
                }
            }
        }

        override fun onChildDraw(
            c: Canvas,
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            dX: Float,
            dY: Float,
            actionState: Int,
            isCurrentlyActive: Boolean
        ) {
            val foregroundView = when (viewHolder) {
                is PaymentCardWalletAdapter.PaymentCardWalletHolder ->
                    viewHolder.binding.mainPayment
                else ->
                    null
            }

            if (foregroundView != null) {

                binding?.swipeRefresh?.isEnabled = false

                when {

                    dY != 0f && dX == 0f -> {
                        super.onChildDraw(
                            c,
                            recyclerView,
                            viewHolder,
                            dX,
                            dY,
                            actionState,
                            isCurrentlyActive
                        )
                    }

                    dX == 0f && dY == 0f -> {
                        binding?.swipeRefresh?.isEnabled = true
                    }

                    dX > 0 -> {
                        viewHolder.itemView.barcode_layout.visibility = View.VISIBLE
                        viewHolder.itemView.delete_layout.visibility = View.GONE
                        getDefaultUIUtil().onDraw(
                            c,
                            recyclerView,
                            foregroundView,
                            dX,
                            dY,
                            actionState,
                            isCurrentlyActive
                        )
                    }

                    dX < 0 -> {
                        viewHolder.itemView.barcode_layout.visibility = View.GONE
                        viewHolder.itemView.delete_layout.visibility = View.VISIBLE
                        getDefaultUIUtil().onDraw(
                            c,
                            recyclerView,
                            foregroundView,
                            dX,
                            dY,
                            actionState,
                            isCurrentlyActive
                        )
                    }

                }

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
                binding?.swipeRefresh?.isEnabled = true
                getDefaultUIUtil().clearView(foregroundView)
            }

        }

        override fun getMovementFlags(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder
        ): Int {
            val hasMultipleCards = walletAdapter.paymentCards.size > 1
            return ItemTouchHelper.Callback.makeMovementFlags(
                if (hasMultipleCards) ItemTouchHelper.UP + ItemTouchHelper.DOWN else 0,
                ItemTouchHelper.LEFT + ItemTouchHelper.RIGHT
            )
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
                        binding?.paymentCardRecycler?.adapter?.notifyDataSetChanged()
                        val paymentSchemeValue = paymentCard.card?.provider
                        paymentScheme = paymentSchemeValue
                        this.paymentCardId = paymentCard.id.toString()

                        if (paymentSchemeValue == null || paymentCardId == null) {
                            failedEvent(DELETE_PAYMENT_CARD_REQUEST)
                        } else {
                            logEvent(
                                DELETE_PAYMENT_CARD_REQUEST,
                                getDeletePaymentCardGenericMap(paymentSchemeValue, paymentCardId!!)
                            )
                        }

                    }
                }
                DialogInterface.BUTTON_NEUTRAL -> {
                    binding?.paymentCardRecycler?.adapter?.notifyDataSetChanged()
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

        binding?.swipeRefresh?.setOnRefreshListener {
            isRefreshing = true
            viewModel.getPaymentCards()
        }

        viewModel.localMembershipCardData.observeNonNull(this) {
            walletAdapter.membershipCards = it.toMutableList()
        }

        viewModel.deleteRequest.observeNonNull(this) {
            viewModel.fetchLocalData()
            val pScheme = paymentScheme
            val paymentCardId = this.paymentCardId
            if (pScheme == null || paymentCardId == null) {
                failedEvent(DELETE_PAYMENT_CARD_RESPONSE_SUCCESS)
            } else {
                logEvent(
                    DELETE_PAYMENT_CARD_RESPONSE_SUCCESS,
                    getDeletePaymentCardGenericMap(pScheme, paymentCardId)
                )
            }

        }

        viewModel.deleteError.observeNonNull(this) {
            viewModel.fetchLocalData()
            val pScheme = paymentScheme
            val paymentCardId = this.paymentCardId
            if (pScheme == null || paymentCardId == null) {
                failedEvent(DELETE_PAYMENT_CARD_RESPONSE_FAILURE)
            } else {
                val httpException = it as HttpException
                logEvent(
                    DELETE_PAYMENT_CARD_RESPONSE_FAILURE,
                    getDeletePaymentCardFailedMap(
                        pScheme,
                        paymentCardId,
                        httpException.code(),
                        httpException.getErrorBody()
                    )
                )
            }

        }

        viewModel.deleteCardError.observeErrorNonNull(requireContext(), true, this)

        binding?.paymentCardRecycler?.apply {
            layoutManager = GridLayoutManager(context, 1)
            adapter = walletAdapter

            ItemTouchHelper(simpleCallback).attachToRecyclerView(this)
        }

        viewModel.paymentCards.observeNonNull(this) {
            if (isRefreshing) {
                isRefreshing = false
                binding?.swipeRefresh?.isRefreshing = false
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
            binding?.swipeRefresh?.isRefreshing = false
            viewModel.fetchLocalData()
        }

        viewModel.hasZendeskResponse.observeNonNull(this) { hasZendeskResponse ->
            binding?.settingsButton?.setImageResource(if (hasZendeskResponse) R.drawable.ic_settings_notified else R.drawable.ic_settings)
        }

        binding?.settingsButton?.setOnClickListener {
            findNavController().navigateIfAdded(this, R.id.settings_screen)
        }
    }

    override fun createDynamicAction(
        dynamicActionLocation: DynamicActionLocation,
        dynamicAction: DynamicAction
    ) {
        dynamicActionLocation.area?.let { dynamicActionLocationArea ->
            when (dynamicActionLocationArea) {
                DynamicActionArea.LEFT_TOP_BAR -> {
                    binding?.leftTopBar?.text = getEmojiByUnicode(dynamicActionLocation.icon)
                    binding?.leftTopBar?.let { leftTopBar ->
                        bindEventToDynamicAction(
                            leftTopBar,
                            dynamicActionLocation,
                            dynamicAction
                        )
                    }
                }
            }
        }
    }


    private fun populateWallet() {
        viewModel.fetchLocalData()
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
            binding?.paymentCardRecycler?.visibility = View.GONE
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
        binding?.paymentCardRecycler?.visibility = View.VISIBLE

        SharedPreferenceManager.isPaymentEmpty =
            viewModel.paymentCards.value.isNullOrEmpty()

        viewModel.paymentCards.value?.let {
            SharedPreferenceManager.hasNoActivePaymentCards =
                MembershipPlanUtils.hasNoActiveCards(it)
        }

        walletItems.clear()

        viewModel.paymentCards.value?.let { paymentCards ->
            walletItems.addAll(paymentCards.sortedByDescending { card -> card.id })
        }

        walletAdapter.paymentCards = WalletOrderingUtil.getSavedPaymentCardWallet(walletItems)

        viewModel.localMembershipPlanData.value?.let { plans ->
            viewModel.localMembershipCardData.value?.let { cards ->
                walletAdapter.onClickListener = {
                    clickHandler(it, plans, cards)
                }
            }
        }

        walletAdapter.notifyDataSetChanged()
    }
}
