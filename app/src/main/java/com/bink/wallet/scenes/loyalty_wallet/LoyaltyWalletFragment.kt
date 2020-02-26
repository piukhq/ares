package com.bink.wallet.scenes.loyalty_wallet

import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
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
import com.bink.wallet.utils.FirebaseUtils.LOYALTY_WALLET_VIEW
import com.bink.wallet.utils.UtilFunctions
import com.bink.wallet.utils.displayModalPopup
import com.bink.wallet.utils.navigateIfAdded
import com.bink.wallet.utils.observeNonNull
import com.bink.wallet.utils.toolbar.FragmentToolbar
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class LoyaltyWalletFragment : BaseFragment<LoyaltyViewModel, FragmentLoyaltyWalletBinding>() {

    override val viewModel: LoyaltyViewModel by viewModel()
    val mainViewModel: MainViewModel by sharedViewModel()
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

                        val directions =
                            plan?.let {
                                WalletsFragmentDirections.homeToBarcode(
                                    plan, card
                                )
                            }

                        if (findNavController().currentDestination?.id == R.id.home_wallet) {
                            if (card.card?.barcode.isNullOrEmpty() ||
                                card.card?.membership_id.isNullOrEmpty()
                            ) {
                                displayNoBarcodeDialog(position)
                            } else {
                                directions?.let {
                                    findNavController().navigateIfAdded(
                                        this@LoyaltyWalletFragment, it
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

        logScreenView(LOYALTY_WALLET_VIEW)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.cardsDataMerger.observeNonNull(this) { userDataResult ->
            setCardsData(userDataResult)
        }
        viewModel.localCardsDataMerger.observeNonNull(this) { localUserDataResult ->
            setCardsData(localUserDataResult)
        }
        viewModel.dismissedBannerDisplay.observeNonNull(this) {
            walletAdapter.deleteBannerDisplayById(it)
            viewModel.fetchDismissedCards()
            binding.progressSpinner.visibility = View.VISIBLE
            binding.swipeLayout.isEnabled = true
        }

        viewModel.localPaymentCards.observeNonNull(this) {
            walletAdapter.paymentCards = it.toMutableList()
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
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        setHasOptionsMenu(true)
        fetchData()

        viewModel.deleteCard.observeNonNull(this) { id ->
            fetchData()
        }

        manageRecyclerView()

        viewModel.fetchLocalPaymentCards()

        binding.swipeLayout.setOnRefreshListener {
            if (UtilFunctions.isNetworkAvailable(requireActivity(), true)) {
                binding.progressSpinner.visibility = View.VISIBLE
                viewModel.fetchMembershipPlans(false)
                viewModel.fetchMembershipCards()
                viewModel.fetchDismissedCards()
            } else {
                disableIndicators()
            }
        }

        viewModel.loadCardsError.observeNonNull(this) {
            viewModel.fetchLocalMembershipCards()
        }
        viewModel.loadPlansError.observeNonNull(this) {
            viewModel.fetchLocalMembershipPlans()
        }

        viewModel.deleteCardError.observeNonNull(this) {
            if (!UtilFunctions.hasCertificatePinningFailed(it, requireContext())) {
                requireContext().displayModalPopup(
                    null,
                    getString(R.string.error_description)
                )
            }
        }
        viewModel.cardsDataMerger.observeNonNull(this) { userDataResult ->
            setCardsData(userDataResult)
        }

        viewModel.localCardsDataMerger.observeNonNull(this) { localUserDataResult ->
            setCardsData(localUserDataResult)
        }

        viewModel.dismissedBannerDisplay.observeNonNull(this) {
            walletAdapter.deleteBannerDisplayById(it)
            viewModel.fetchDismissedCards()
            binding.progressSpinner.visibility = View.VISIBLE
            binding.swipeLayout.isEnabled = true
        }

        mainViewModel.membershipPlanDatabaseLiveData.observe(this, Observer {
            viewModel.fetchLocalMembershipPlans()
            viewModel.fetchLocalMembershipCards()
            viewModel.fetchDismissedCards()
        })
    }

    override fun onPause() {
        disableIndicators()
        super.onPause()
    }

    override fun builder(): FragmentToolbar {
        return FragmentToolbar.Builder()
            .withId(FragmentToolbar.NO_TOOLBAR)
            .build()
    }

    private fun setCardsData(userDataResult: UserDataResult) {
        when (userDataResult) {
            is UserDataResult.UserDataSuccess -> {
                walletItems = ArrayList()
                walletItems.addAll(userDataResult.result.third)
                walletAdapter.membershipCards = ArrayList(userDataResult.result.third)
                walletAdapter.membershipPlans = ArrayList(userDataResult.result.second)
                walletAdapter.notifyDataSetChanged()
                disableIndicators()
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
        binding.swipeLayout.isRefreshing = false
        binding.progressSpinner.visibility = View.GONE
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
                val directions =
                    WalletsFragmentDirections.homeToAddJoin(
                        item,
                        null,
                        true,
                        isRetryJourney = false
                    )
                findNavController().navigateIfAdded(
                    this@LoyaltyWalletFragment,
                    directions
                )
            }
            else ->
                findNavController().navigateIfAdded(
                    this@LoyaltyWalletFragment,
                    WalletsFragmentDirections.homeToPcd()
                )
        }
    }

    private fun fetchData() {
        binding.progressSpinner.visibility = View.VISIBLE
        if (UtilFunctions.isNetworkAvailable(requireActivity())) {
            viewModel.fetchMembershipPlans(true)
            viewModel.fetchMembershipCards()
            viewModel.fetchDismissedCards()
        } else {
            viewModel.fetchLocalMembershipPlans()
            viewModel.fetchLocalMembershipCards()
            viewModel.fetchDismissedCards()
        }
    }

    private fun onBannerRemove(item: Any) {
        binding.swipeLayout.isEnabled = false
        binding.progressSpinner.visibility = View.GONE
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
                            binding.progressSpinner.visibility = View.VISIBLE
                            viewModel.deleteCard(membershipCard.id)
                        } else {
                            disableIndicators()
                        }
                        binding.loyaltyWalletList.adapter?.notifyItemChanged(position)
                    }
                    DialogInterface.BUTTON_NEUTRAL -> {
                        Log.d(
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
}
