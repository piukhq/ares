package com.bink.wallet.scenes.loyalty_wallet

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.bink.wallet.BaseFragment
import com.bink.wallet.MainViewModel
import com.bink.wallet.R
import com.bink.wallet.databinding.FragmentLoyaltyWalletBinding
import com.bink.wallet.model.JoinCardItem
import com.bink.wallet.model.response.membership_card.MembershipCard
import com.bink.wallet.model.response.membership_card.UserDataResult
import com.bink.wallet.model.response.membership_plan.MembershipPlan
import com.bink.wallet.scenes.loyalty_wallet.RecyclerItemTouchHelper.RecyclerItemTouchHelperListener
import com.bink.wallet.scenes.wallets.WalletsFragmentDirections
import com.bink.wallet.utils.ApiErrorUtils
import com.bink.wallet.utils.FirebaseEvents.DELETE_LOYALTY_CARD_REQUEST
import com.bink.wallet.utils.FirebaseEvents.DELETE_LOYALTY_CARD_RESPONSE_FAILURE
import com.bink.wallet.utils.FirebaseEvents.DELETE_LOYALTY_CARD_RESPONSE_SUCCESS
import com.bink.wallet.utils.FirebaseEvents.LOYALTY_WALLET_VIEW
import com.bink.wallet.utils.UtilFunctions
import com.bink.wallet.utils.ZendeskUtils
import com.bink.wallet.utils.displayModalPopup
import com.bink.wallet.utils.logDebug
import com.bink.wallet.utils.logPaymentCardSuccess
import com.bink.wallet.utils.navigateIfAdded
import com.bink.wallet.utils.observeErrorNonNull
import com.bink.wallet.utils.observeNonNull
import com.bink.wallet.utils.requestCameraPermissionAndNavigate
import com.bink.wallet.utils.requestPermissionsResult
import com.bink.wallet.utils.scanResult
import com.bink.wallet.utils.toolbar.FragmentToolbar
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
        onRemoveListener = { onBannerRemove(it) }
    ).apply {
        setHasStableIds(true)
    }

    private var walletItems = ArrayList<Any>()
    private var isRefresh = false
    private var isErrorShowing = false
    private var deletedCard: MembershipCard? = null

    private val listener = object :
        RecyclerItemTouchHelperListener {
        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int, position: Int) {
            if (walletItems[position] is MembershipCard) {
                if (viewHolder is LoyaltyWalletAdapter.LoyaltyWalletViewHolder) {
                    if (direction == ItemTouchHelper.RIGHT) {
                        val card = walletItems[position] as MembershipCard
                        val membershipPlanData = viewModel.membershipPlanData.value
                            ?: viewModel.localMembershipPlanData.value
                        val plan =
                            membershipPlanData?.firstOrNull {
                                it.id == card.membership_plan
                            }

                        if (findNavController().currentDestination?.id == R.id.home_wallet) {
                            if (card.card?.barcode.isNullOrEmpty() && card.card?.membership_id.isNullOrEmpty()
                            ) {
                                displayNoBarcodeDialog(position)
                            } else {
                                plan?.let {
                                    findNavController().navigate(
                                        WalletsFragmentDirections.homeToBarcode(
                                            plan,
                                            card
                                        )
                                    )
                                }
                                this@LoyaltyWalletFragment.onDestroy()
                            }
                        }
                    } else {
                        deleteDialog(walletItems[position] as MembershipCard, position)
                    }
                }
            } else {
                onCardClicked(walletItems[position])
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onResume() {
        super.onResume()
        viewModel.fetchPeriodicMembershipCards()
        logScreenView(LOYALTY_WALLET_VIEW)

        if (ZendeskUtils.hasResponseBeenReceived()) {
            binding.settingsButton.setImageResource(R.drawable.ic_settings_notified)
        } else {
            binding.settingsButton.setImageResource(R.drawable.ic_settings)

        }
        logDebug("WalletFragmentLoyalty",findNavController().currentDestination.toString())

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


        viewModel.isLoading.observeNonNull(this){
            binding.swipeLayout.isRefreshing = it
        }

        mainViewModel.isLoading.observeNonNull(this) {
            binding.swipeLayout.isRefreshing = it
        }

        binding.loyaltyWalletList.apply {
            layoutManager = GridLayoutManager(requireContext(), 1)
            adapter = walletAdapter

            val helperListenerLeft =
                RecyclerItemTouchHelper(0, ItemTouchHelper.LEFT, listener)

            val helperListenerRight =
                RecyclerItemTouchHelper(0, ItemTouchHelper.RIGHT, listener)

            ItemTouchHelper(helperListenerLeft).attachToRecyclerView(this)
            ItemTouchHelper(helperListenerRight).attachToRecyclerView(this)
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
            viewModel.fetchLocalMembershipCards(false)
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
            val planId = deletedCard?.membership_plan
            val uuid = deletedCard?.uuid
            if (planId == null || uuid == null) {
                failedEvent(DELETE_LOYALTY_CARD_RESPONSE_FAILURE)
            } else {
                logEvent(
                    DELETE_LOYALTY_CARD_RESPONSE_FAILURE,
                    getDeleteLoyaltyCardGenericMap(planId, uuid)
                )

            }
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        mainViewModel.membershipPlanDatabaseLiveData.observe(viewLifecycleOwner, Observer {
            viewModel.fetchLocalMembershipPlans()
            viewModel.fetchLocalMembershipCards(false)
            viewModel.fetchDismissedCards()
        })

        binding.settingsButton.setOnClickListener {
            findNavController().navigateIfAdded(this, R.id.settings_screen)
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

    private fun setCardsData(userDataResult: UserDataResult) {
        isRefresh = false
        when (userDataResult) {
            is UserDataResult.UserDataSuccess -> {
                walletItems = ArrayList()
                walletItems.addAll(userDataResult.result.third)
                // We should only stop loading & show membership cards if we have membership plans too
                if (userDataResult.result.second.isNotEmpty()) {
                    walletAdapter.membershipCards = ArrayList(userDataResult.result.third)
                    disableIndicators()
                }
                walletAdapter.membershipPlans = ArrayList(userDataResult.result.second)
                walletAdapter.notifyDataSetChanged()
            }
        }
    }

    private fun manageRecyclerView() {
        binding.loyaltyWalletList.apply {
            layoutManager = GridLayoutManager(requireContext(), 1)
            adapter = walletAdapter

            val helperListenerLeft =
                RecyclerItemTouchHelper(0, ItemTouchHelper.LEFT, listener)

            val helperListenerRight =
                RecyclerItemTouchHelper(0, ItemTouchHelper.RIGHT, listener)

            ItemTouchHelper(helperListenerLeft).attachToRecyclerView(this)
            ItemTouchHelper(helperListenerRight).attachToRecyclerView(this)
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
                                WalletsFragmentDirections.homeToDetail(
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
                    WalletsFragmentDirections.homeToAddJoin(
                        item,
                        null,
                        true,
                        isRetryJourney = false
                    )
                )
            }
            else ->
                requestCameraPermissionAndNavigate(false, null)

        }
    }

    private fun navigateToAddPaymentCard(cardNumber: String = "") {
        val directions = WalletsFragmentDirections.homeToPcd(
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
            viewModel.fetchLocalMembershipCards(false)
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
                        logDebug(
                            LoyaltyWalletFragment::class.java.simpleName,
                            getString(R.string.loyalty_wallet_dialog_description)
                        )
                        binding.loyaltyWalletList.adapter?.notifyItemChanged(position)
                    }
                }
            }
            builder.setPositiveButton(getString(R.string.yes_text), dialogClickListener)
            builder.setNeutralButton(getString(R.string.cancel_text_upper), dialogClickListener)
            dialog = builder.create()
            dialog.show()
        }
    }

    private fun displayNoBarcodeDialog(position: Int) {
        requireContext().displayModalPopup(
            getString(R.string.loyalty_wallet_no_barcode_title),
            getString(R.string.loyalty_wallet_no_barcode_message),
            okAction = {
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
}
