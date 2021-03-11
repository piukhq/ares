package com.bink.wallet.scenes.loyalty_wallet

import android.content.DialogInterface
import android.content.Intent
import android.graphics.Canvas
import android.os.Bundle
import android.os.Handler
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.ItemTouchHelper.*
import androidx.recyclerview.widget.RecyclerView
import com.bink.wallet.BaseFragment
import com.bink.wallet.MainViewModel
import com.bink.wallet.R
import com.bink.wallet.databinding.FragmentLoyaltyWalletBinding
import com.bink.wallet.model.DynamicAction
import com.bink.wallet.model.DynamicActionArea
import com.bink.wallet.model.DynamicActionLocation
import com.bink.wallet.model.JoinCardItem
import com.bink.wallet.model.response.membership_card.MembershipCard
import com.bink.wallet.model.response.membership_card.UserDataResult
import com.bink.wallet.model.response.membership_plan.MembershipPlan
import com.bink.wallet.utils.*
import com.bink.wallet.utils.FirebaseEvents.DELETE_LOYALTY_CARD_REQUEST
import com.bink.wallet.utils.FirebaseEvents.DELETE_LOYALTY_CARD_RESPONSE_FAILURE
import com.bink.wallet.utils.FirebaseEvents.DELETE_LOYALTY_CARD_RESPONSE_SUCCESS
import com.bink.wallet.utils.FirebaseEvents.FIREBASE_REQUEST_REVIEW
import com.bink.wallet.utils.FirebaseEvents.FIREBASE_REQUEST_REVIEW_ADD
import com.bink.wallet.utils.FirebaseEvents.FIREBASE_REQUEST_REVIEW_TIME
import com.bink.wallet.utils.FirebaseEvents.LOYALTY_WALLET_VIEW
import com.bink.wallet.utils.toolbar.FragmentToolbar
import kotlinx.android.synthetic.main.loyalty_wallet_item.view.*
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import retrofit2.HttpException
import java.net.SocketTimeoutException

class LoyaltyWalletFragment : BaseFragment<LoyaltyViewModel, FragmentLoyaltyWalletBinding>() {

    override val viewModel: LoyaltyViewModel by viewModel()
    private val mainViewModel: MainViewModel by sharedViewModel()

    override val layoutRes: Int
        get() = R.layout.fragment_loyalty_wallet

    private val walletAdapter = LoyaltyWalletAdapter(
        onClickListener = {
            onCardClicked(it)
        },
        onRemoveListener = { onBannerRemove(it) },

        onCardLinkClickListener = {
            onCardLinkClicked(it)
        }
    ).apply {
        setHasStableIds(true)
    }

    private var walletItems = ArrayList<Any>()
    private var isRefresh = false
    private var isErrorShowing = false
    private var deletedCard: MembershipCard? = null
    private lateinit var cards: List<MembershipCard>
    private lateinit var plans: List<MembershipPlan>


    private var simpleCallback =
        ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(UP + DOWN, LEFT + RIGHT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                val currentPosition = viewHolder.adapterPosition
                var card: MembershipCard? = null

                try {
                    card = walletAdapter.membershipCards[currentPosition] as MembershipCard
                } catch (e: ClassCastException) {
                    //User attempting to drag join plan
                }

                card?.let {
                    return walletAdapter.onItemMove(currentPosition, target.adapterPosition)
                }
                return false
            }

            override fun isLongPressDragEnabled(): Boolean {
                return true
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                var card: MembershipCard? = null

                try {
                    card = walletAdapter.membershipCards[position] as MembershipCard
                } catch (e: ClassCastException) {
                    //User swiping membership plan
                }

                card?.let { card ->
                    if (direction == RIGHT) {
                        val membershipPlanData = viewModel.membershipPlanData.value
                            ?: viewModel.localMembershipPlanData.value
                        val plan = membershipPlanData?.firstOrNull {
                            it.id == card.membership_plan
                        }

                        if (findNavController().currentDestination?.id == R.id.loyalty_fragment) {
                            if (card.card?.barcode.isNullOrEmpty() && card.card?.membership_id.isNullOrEmpty()
                            ) {
                                displayNoBarcodeDialog(position)
                            } else {
                                plan?.let {
                                    findNavController().navigate(
                                        LoyaltyWalletFragmentDirections.loyaltyToBarcode(
                                            plan,
                                            card
                                        )
                                    )
                                }
                                this@LoyaltyWalletFragment.onDestroy()
                            }
                        }
                    } else {
                        deleteDialog(card, position)
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
                    is LoyaltyWalletAdapter.LoyaltyWalletViewHolder ->
                        viewHolder.binding.cardItem.mainLayout
                    else ->
                        null
                }

                if (foregroundView != null) {

                    binding.swipeLayout.isEnabled = false

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
                            binding.swipeLayout.isEnabled = true
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

            override fun clearView(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder
            ) {
                val foregroundView = when (viewHolder) {
                    is LoyaltyWalletAdapter.LoyaltyWalletViewHolder ->
                        viewHolder.binding.cardItem.mainLayout
                    else ->
                        null
                }

                if (foregroundView != null) {
                    Handler().postDelayed({
                        binding.swipeLayout.isEnabled = true
                    }, 1000)
                    getDefaultUIUtil().clearView(foregroundView)
                }

            }

            override fun getMovementFlags(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder
            ): Int {
                val hasMultipleCards =
                    walletAdapter.membershipCards.filterIsInstance<MembershipCard>().size > 1
                return Callback.makeMovementFlags(
                    if (hasMultipleCards) UP + DOWN else 0,
                    LEFT + RIGHT
                )
            }
        })

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onResume() {
        super.onResume()
        viewModel.fetchPeriodicMembershipCards()
        viewModel.checkZendeskResponse()
        RequestReviewUtil.triggerViaWallet(this) {
            logEvent(FIREBASE_REQUEST_REVIEW, getRequestReviewMap(FIREBASE_REQUEST_REVIEW_ADD))
        }
        logScreenView(LOYALTY_WALLET_VIEW)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.cardsDataMerger.observeNonNull(this) { userDataResult ->
            setCardsData(userDataResult)
        }
        mainViewModel.isLoading.value?.let {
            if (it) {
                binding.swipeLayout.isRefreshing = true
            }
        }
        viewModel.dismissedBannerDisplay.observeNonNull(this) {
            walletAdapter.deleteBannerDisplayById(it)
            viewModel.fetchDismissedCards()
            binding.swipeLayout.isEnabled = true
        }

        viewModel.localPaymentCards.observeNonNull(this) {
            walletAdapter.paymentCards = it.toMutableList()
        }


        viewModel.isLoading.observeNonNull(this) {
            binding.swipeLayout.isRefreshing = it
        }

        viewModel.hasZendeskResponse.observeNonNull(this) { hasZendeskResponse ->
            binding.settingsButton.setImageResource(if (hasZendeskResponse) R.drawable.ic_settings_notified else R.drawable.ic_settings)
        }

        mainViewModel.isLoading.observeNonNull(this) {
            binding.swipeLayout.isRefreshing = it
        }

        binding.loyaltyWalletList.apply {
            layoutManager = GridLayoutManager(requireContext(), 1)
            adapter = walletAdapter
        }

        setHasOptionsMenu(true)
        fetchData()

        viewModel.deleteCard.observeNonNull(this) {
            fetchData()
            val planId = deletedCard?.membership_plan
            val uuid = deletedCard?.uuid
            if (planId == null || uuid == null) {
                failedEvent(DELETE_LOYALTY_CARD_RESPONSE_SUCCESS)
            } else {
                logEvent(
                    DELETE_LOYALTY_CARD_RESPONSE_SUCCESS,
                    getDeleteLoyaltyCardGenericMap(planId, uuid)
                )

            }

        }

        manageRecyclerView()

        viewModel.fetchLocalPaymentCards()

        binding.swipeLayout.setOnRefreshListener {
            isRefresh = true
            if (UtilFunctions.isNetworkAvailable(requireActivity(), true)) {
                viewModel.fetchMembershipCardsAndPlansForRefresh()
            } else {
                isRefresh = false
                disableIndicators()
            }
        }

        viewModel.loadCardsError.observeNonNull(this) {
            handleServerDownError(it)
            viewModel.fetchLocalMembershipCards()
        }

        viewModel.loadPlansError.observeNonNull(this) {
            handleServerDownError(it)
            viewModel.fetchLocalMembershipPlans()
        }

        viewModel.deleteCardError.observeErrorNonNull(requireContext(), true, this)

        viewModel.dismissedBannerDisplay.observeNonNull(this) {
            walletAdapter.deleteBannerDisplayById(it)
            viewModel.fetchDismissedCards()
            binding.swipeLayout.isEnabled = true
        }

        viewModel.deleteCardError.observeNonNull(this) {
            fetchData()
            val planId = deletedCard?.membership_plan
            val cardId = deletedCard?.id
            if (planId == null || cardId == null) {
                failedEvent(DELETE_LOYALTY_CARD_RESPONSE_FAILURE)
            } else {
                val httpException = it as HttpException
                logEvent(
                    DELETE_LOYALTY_CARD_RESPONSE_FAILURE,
                    getDeleteLoyaltyCardFailMap(
                        planId,
                        cardId,
                        httpException.code(),
                        httpException.getErrorBody()
                    )
                )
            }
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        mainViewModel.membershipPlanDatabaseLiveData.observe(viewLifecycleOwner, Observer {
            viewModel.fetchLocalMembershipPlans()
            viewModel.fetchLocalMembershipCards()
            viewModel.fetchDismissedCards()
        })

        binding.settingsButton.setOnClickListener {
            findNavController().navigateIfAdded(
                this,
                LoyaltyWalletFragmentDirections.loyaltyToSettingsScreen()
            )
        }
    }

    override fun onPause() {
        disableIndicators()
        super.onPause()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        scanResult(
            requestCode,
            resultCode,
            data,
            { navigateToAddPaymentCard(it) },
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
            { navigateToAddPaymentCard() },
            null
        )
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun builder(): FragmentToolbar {
        return FragmentToolbar.Builder()
            .withId(FragmentToolbar.NO_TOOLBAR)
            .build()
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

    private fun setCardsData(userDataResult: UserDataResult) {
        isRefresh = false
        when (userDataResult) {
            is UserDataResult.UserDataSuccess -> {
                walletItems = ArrayList()
                walletItems.addAll(userDataResult.result.third)
                // We should only stop loading & show membership cards if we have membership plans too
                if (userDataResult.result.second.isNotEmpty()) {
                    cards = userDataResult.result.first
                    plans = userDataResult.result.second
                    walletAdapter.cards = userDataResult.result.first as MutableList<MembershipCard>
                    walletAdapter.membershipCards =
                        WalletOrderingUtil.getSavedLoyaltyCardWallet(
                            sortPlans(ArrayList(userDataResult.result.third))
                        )

                    disableIndicators()
                }
                walletAdapter.membershipPlans = ArrayList(userDataResult.result.second)
                walletAdapter.notifyDataSetChanged()

                if (userDataResult.result.first.size > 4) {
                    RequestReviewUtil.triggerViaCards(this) {
                        logEvent(
                            FIREBASE_REQUEST_REVIEW,
                            getRequestReviewMap(FIREBASE_REQUEST_REVIEW_TIME)
                        )
                    }
                }

            }
        }
    }

    private fun sortPlans(loyaltyCards: ArrayList<Any>): ArrayList<Any> {
        val allCards = ArrayList<Any>()
        val (cards, plans) = loyaltyCards.partition { cardType -> cardType is MembershipCard }

        if (plans.isNotEmpty() && shouldShowCardLink(this.cards,this.plans)) {
            val plan = plans.firstOrNull()
            plan?.let { allCards.add(it) }
        }
        allCards.addAll(cards)

        return allCards

    }

    private fun manageRecyclerView() {
        binding.loyaltyWalletList.apply {
            layoutManager = GridLayoutManager(requireContext(), 1)
            adapter = walletAdapter

            simpleCallback.attachToRecyclerView(this)
        }
    }

    private fun disableIndicators() {
        binding.loyaltyWalletList.visibility = View.VISIBLE
        mainViewModel.stopLoading()
    }

    private fun onCardClicked(item: Any) {
        when (item) {
            is MembershipCard -> {
                val list =
                    viewModel.localMembershipPlanData.value ?: viewModel.membershipPlanData.value
                list?.let {
                    for (membershipPlan in it) {
                        if (item.membership_plan == membershipPlan.id) {
                            val directions =
                                LoyaltyWalletFragmentDirections.loyaltyToDetail(
                                    membershipPlan,
                                    item
                                )
                            findNavController().navigateIfAdded(
                                this@LoyaltyWalletFragment,
                                directions
                            )
                        }
                    }
                }
            }
            is MembershipPlan -> {
                findNavController().navigate(
                    LoyaltyWalletFragmentDirections.loyaltyToBrowseBrands(
                        plans.toTypedArray(),
                        cards.toTypedArray()
                    )
                )
            }
            else ->
                requestCameraPermissionAndNavigate(false, null)

        }
    }

    private fun navigateToAddPaymentCard(cardNumber: String = "") {
        val directions = LoyaltyWalletFragmentDirections.loyaltyToAddPaymentCard(
            cardNumber
        )
        findNavController().navigateIfAdded(this, directions)
    }

    private fun fetchData() {
        viewModel.fetchDismissedCards()
        if (UtilFunctions.isNetworkAvailable(requireActivity())) {
            viewModel.fetchMembershipPlans(true)
            viewModel.fetchPeriodicMembershipCards()
        } else {
            viewModel.fetchLocalMembershipPlans()
            viewModel.fetchLocalMembershipCards()
        }
    }

    private fun onBannerRemove(item: Any) {
        binding.swipeLayout.isEnabled = false
        when (item) {
            is MembershipPlan -> viewModel.addPlanIdAsDismissed(item.id)
            else -> viewModel.addPlanIdAsDismissed((item as JoinCardItem).id)
        }
    }

    fun deleteDialog(membershipCard: MembershipCard, position: Int) {
        lateinit var dialog: AlertDialog
        val builder = context?.let { AlertDialog.Builder(it) }
        if (builder != null) {
            builder.setTitle(getString(R.string.loyalty_wallet_dialog_title))
            val dialogClickListener = DialogInterface.OnClickListener { _, which ->
                when (which) {
                    DialogInterface.BUTTON_POSITIVE -> {
                        if (UtilFunctions.isNetworkAvailable(requireActivity(), true)) {
                            viewModel.deleteCard(membershipCard.id)
                            deletedCard = membershipCard
                            val planId = membershipCard.membership_plan
                            val uuid = membershipCard.uuid
                            if (planId == null || uuid == null) {
                                failedEvent(DELETE_LOYALTY_CARD_REQUEST)
                            } else {
                                logEvent(
                                    DELETE_LOYALTY_CARD_REQUEST,
                                    getDeleteLoyaltyCardGenericMap(planId, uuid)
                                )

                            }
                        } else {
                            disableIndicators()
                        }
                        binding.loyaltyWalletList.adapter?.notifyItemChanged(position)
                    }
                    DialogInterface.BUTTON_NEUTRAL -> {
                        dialog.cancel()
                    }
                }
            }
            builder.setPositiveButton(getString(R.string.yes_text), dialogClickListener)
            builder.setNeutralButton(getString(R.string.cancel_text_upper), dialogClickListener)
            dialog = builder.create()
            dialog.show()

            dialog.setOnCancelListener {
                logDebug(
                    LoyaltyWalletFragment::class.java.simpleName,
                    getString(R.string.loyalty_wallet_dialog_description)
                )
                binding.loyaltyWalletList.adapter?.notifyItemChanged(position)
            }
        }
    }

    private fun displayNoBarcodeDialog(position: Int) {
        requireContext().displayModalPopup(
            getString(R.string.loyalty_wallet_no_barcode_title),
            getString(R.string.loyalty_wallet_no_barcode_message),
            okAction = {
                binding.loyaltyWalletList.adapter?.notifyItemChanged(position)
            },
            cancelAction = {
                binding.loyaltyWalletList.adapter?.notifyItemChanged(position)
            }
        )
    }

    private fun handleServerDownError(throwable: Throwable) {
        if (isRefresh) {
            if (((throwable is HttpException)
                        && throwable.code() >= ApiErrorUtils.SERVER_ERROR)
                || throwable is SocketTimeoutException
            ) {
                if (!isErrorShowing) {
                    isErrorShowing = true
                    requireContext().displayModalPopup(
                        requireContext().getString(R.string.error_server_down_title),
                        requireContext().getString(R.string.error_server_down_message), {
                            isErrorShowing = false
                        }
                    )
                }
            }
        }
    }

    private fun onCardLinkClicked(item: MembershipPlan) {
        val directions = LoyaltyWalletFragmentDirections.loyaltyToAddJoin(
            item,
            null, isFromJoinCard = false, isRetryJourney = false
        )
        findNavController().navigateIfAdded(this, directions, R.id.loyalty_fragment)
    }

    private fun shouldShowCardLink(cards:List<MembershipCard>,plan: List<MembershipPlan>):Boolean{

        cards.forEach { membershipCard ->
            plan.forEach { mPlan ->
                if (membershipCard.membership_plan == mPlan.id){
                    if(mPlan.isPlanPLL()){
                        return false
                    }
                }
            }

        }

        return true
    }
}
