package com.bink.wallet.scenes.payment_card_wallet

import android.content.DialogInterface
import android.content.Intent
import android.graphics.Canvas
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
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
import com.bink.wallet.utils.*
import com.bink.wallet.utils.FirebaseEvents.DELETE_PAYMENT_CARD_REQUEST
import com.bink.wallet.utils.FirebaseEvents.DELETE_PAYMENT_CARD_RESPONSE_FAILURE
import com.bink.wallet.utils.FirebaseEvents.DELETE_PAYMENT_CARD_RESPONSE_SUCCESS
import com.bink.wallet.utils.FirebaseEvents.PAYMENT_WALLET_VIEW
import com.bink.wallet.utils.UtilFunctions.isNetworkAvailable
import com.bink.wallet.utils.toolbar.FragmentToolbar
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

    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                // Permission is granted. Continue the action or workflow in your
                // app.
                requestPermissionsResult(
                    null,
                    { navigateToPaymentAddPaymentCard() },
                    null,
                    isGranted
                )

            } else {
                // Explain to the user that the feature is unavailable because the
                // features requires a permission that the user has denied. At the
                // same time, respect the user's decision. Don't link to system
                // settings in an effort to convince the user to change their
                // decision.
            }
        }

    override fun onResume() {
        super.onResume()

        viewModel.getPeriodicPaymentCards()

        logScreenView(PAYMENT_WALLET_VIEW)
    }

    override fun onPause() {
        binding.paymentCardRecycler.layoutManager?.let { layoutManager ->
            SharedPreferenceManager.paymentWalletPosition = layoutManager.onSaveInstanceState()
        }
        super.onPause()
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
            val currentPosition = viewHolder.adapterPosition
            var card: PaymentCard? = null
            try {
                card = walletAdapter.paymentCards[currentPosition] as PaymentCard
            } catch (e: ClassCastException) {
                //User attempting to drag join plan
            }

            card?.let {
                return walletAdapter.onItemMove(viewHolder.adapterPosition, target.adapterPosition)
            }

            return false
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            val position = viewHolder.adapterPosition
            val paymentCards = walletAdapter.paymentCards
            if (paymentCards.isNotEmpty() && viewHolder is PaymentCardWalletAdapter.PaymentCardWalletHolder && direction == ItemTouchHelper.LEFT) {
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

                binding.swipeRefresh.isEnabled = false

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
                        binding.swipeRefresh.isEnabled = true
                    }

                    dX < 0 -> {
                        (viewHolder as PaymentCardWalletAdapter.PaymentCardWalletHolder).binding.barcodeLayout.visibility =
                            View.GONE
                        viewHolder.binding.deleteLayout.visibility = View.VISIBLE
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
                binding.swipeRefresh.isEnabled = true
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
                ItemTouchHelper.LEFT
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
                        binding.paymentCardRecycler.adapter?.notifyDataSetChanged()
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

        binding.paymentCardRecycler.apply {
            layoutManager = LinearLayoutManager(context)
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

    override fun createDynamicAction(
        dynamicActionLocation: DynamicActionLocation,
        dynamicAction: DynamicAction
    ) {
        dynamicActionLocation.area?.let { dynamicActionLocationArea ->
            when (dynamicActionLocationArea) {
                DynamicActionArea.LEFT_TOP_BAR -> {
                    binding.leftTopBar.text = getEmojiByUnicode(dynamicActionLocation.icon)
                    bindEventToDynamicAction(
                        binding.leftTopBar,
                        dynamicActionLocation,
                        dynamicAction
                    )
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
                requestCameraPermissionAndNavigate(
                    requestPermissionLauncher,
                    false,
                    null,
                    { navigateToPaymentAddPaymentCard() }, null
                )
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

        SharedPreferenceManager.paymentWalletPosition?.let {
            binding.paymentCardRecycler.layoutManager?.onRestoreInstanceState(it)
            SharedPreferenceManager.paymentWalletPosition = null
        }

    }
}
