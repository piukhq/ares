package com.bink.wallet.scenes.loyalty_wallet

import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.bink.wallet.BaseFragment
import com.bink.wallet.R
import com.bink.wallet.data.SharedPreferenceManager
import com.bink.wallet.databinding.FragmentLoyaltyWalletBinding
import com.bink.wallet.model.JoinCardItem
import com.bink.wallet.model.response.membership_card.MembershipCard
import com.bink.wallet.model.response.membership_plan.MembershipPlan
import com.bink.wallet.scenes.loyalty_wallet.RecyclerItemTouchHelper.RecyclerItemTouchHelperListener
import com.bink.wallet.scenes.wallets.WalletsFragmentDirections
import com.bink.wallet.utils.JOIN_CARD
import com.bink.wallet.utils.enums.CardType
import com.bink.wallet.utils.navigateIfAdded
import com.bink.wallet.utils.observeNonNull
import com.bink.wallet.utils.toolbar.FragmentToolbar
import com.bink.wallet.utils.verifyAvailableNetwork
import kotlinx.coroutines.runBlocking
import org.koin.androidx.viewmodel.ext.android.viewModel

class LoyaltyWalletFragment : BaseFragment<LoyaltyViewModel, FragmentLoyaltyWalletBinding>() {

    override fun builder(): FragmentToolbar {
        return FragmentToolbar.Builder()
            .withId(FragmentToolbar.NO_TOOLBAR)
            .build()
    }

    override val viewModel: LoyaltyViewModel by viewModel()
    override val layoutRes: Int
        get() = R.layout.fragment_loyalty_wallet

    private val walletAdapter = LoyaltyWalletAdapter(
        onClickListener = {
            onCardClicked(it)
        },
        onRemoveListener = { onBannerRemove(it) }
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    private var walletItems = ArrayList<Any>()

    val listener = object :
        RecyclerItemTouchHelperListener {
        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int, position: Int) {
            if (viewHolder is LoyaltyWalletAdapter.LoyaltyWalletViewHolder) {
                if (direction == ItemTouchHelper.RIGHT &&
                    walletItems[position] is MembershipCard
                ) {
                    val card = walletItems[position] as MembershipCard
                    if (viewModel.membershipPlanData.value != null) {
                        val plan =
                            viewModel.membershipPlanData.value?.first {
                                it.id == card.membership_plan
                            }!!

                        val directions =
                            card.card?.barcode_type?.let {
                                WalletsFragmentDirections.homeToBarcode(
                                    plan, card
                                )
                            }
                        if (findNavController().currentDestination?.id == R.id.home_wallet) {
                            directions?.let {
                                findNavController().navigateIfAdded(
                                    this@LoyaltyWalletFragment, it
                                )
                            }
                            this@LoyaltyWalletFragment.onDestroy()
                        }
                    }
                } else {
                    walletItems[position].let {
                        if (it is MembershipCard)
                            deleteDialog(it, position)
                    }
                }
            }
        }
    }

    private fun createFetchObservers() {
        viewModel.membershipPlanData.observeNonNull(this) { plansReceived ->
            viewModel.membershipCardData.observeNonNull(this) { cardsReceived ->
                populateScreen(plansReceived, cardsReceived)
                viewModel.membershipCardData.removeObservers(this@LoyaltyWalletFragment)
                viewModel.membershipPlanData.removeObservers(this@LoyaltyWalletFragment)
            }
        }
    }

    private fun createLocalObservers() {
        viewModel.localMembershipPlanData.observeNonNull(this) { plansReceived ->
            viewModel.localMembershipCardData.observeNonNull(this) { cardsReceived ->
                populateScreen(plansReceived, cardsReceived)
                viewModel.localMembershipCardData.removeObservers(this@LoyaltyWalletFragment)
                viewModel.localMembershipPlanData.removeObservers(this@LoyaltyWalletFragment)
            }
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        setHasOptionsMenu(true)

        viewModel.deleteCard.observeNonNull(this) { id ->
            viewModel.localMembershipCardData.value =
                viewModel.localMembershipCardData.value?.filter { it.id != id }
            viewModel.membershipCardData.value =
                viewModel.membershipCardData.value?.filter { it.id != id }
            walletItems.firstOrNull {
                if (it is MembershipCard)
                    it.id == id
                else
                    false
            }.let { walletItems.remove(it) }
            binding.loyaltyWalletList.adapter?.notifyDataSetChanged()
        }

        binding.loyaltyWalletList.apply {
            layoutManager = GridLayoutManager(requireContext(), 1)
            adapter = walletAdapter

            val helperListenerLeft =
                RecyclerItemTouchHelper(0, ItemTouchHelper.LEFT, listener)

            val helperListenerRight =
                RecyclerItemTouchHelper(0, ItemTouchHelper.RIGHT, listener)

            ItemTouchHelper(helperListenerLeft).attachToRecyclerView(this)
            ItemTouchHelper(helperListenerRight).attachToRecyclerView(
                this
            )
        }

        binding.progressSpinner.visibility = View.VISIBLE

        createLocalObservers()
        createFetchObservers()

        if (verifyAvailableNetwork(requireActivity())) {
            runBlocking {
                viewModel.fetchMembershipPlans()
                viewModel.fetchMembershipCards()
            }
            binding.swipeLayout.isEnabled = false
            binding.swipeLayout.isRefreshing = false
            viewModel.fetchLocalMembershipPlans()
            viewModel.fetchLocalMembershipCards()
            viewModel.fetchDismissedCards()
        } else {
            showNoInternetConnectionDialog()
        }

        binding.swipeLayout.setOnRefreshListener {
            if (verifyAvailableNetwork(requireActivity())) {
                createFetchObservers()
                runBlocking {
                    viewModel.fetchMembershipPlans()
                    viewModel.fetchMembershipCards()
                }
            } else {
                showNoInternetConnectionDialog()
            }
        }
    }

    private fun onCardClicked(item: Any) {
        when (item) {
            is MembershipCard -> {
                for (membershipPlan in viewModel.localMembershipPlanData.value!!) {
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
            is MembershipPlan -> {
                val directions =
                    WalletsFragmentDirections.homeToAddJoin(
                        item, null
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

    fun deleteDialog(membershipCard: MembershipCard, position: Int) {
        lateinit var dialog: AlertDialog
        val builder = context?.let { AlertDialog.Builder(it) }
        if (builder != null) {
            builder.setTitle(getString(R.string.loayalty_wallet_dialog_title))
            val dialogClickListener = DialogInterface.OnClickListener { _, which ->
                when (which) {
                    DialogInterface.BUTTON_POSITIVE -> {
                        if (verifyAvailableNetwork(requireActivity())) {
                            runBlocking {
                                viewModel.deleteCard(membershipCard.id)
                            }
                        } else {
                            showNoInternetConnectionDialog()
                        }
                    }
                    DialogInterface.BUTTON_NEUTRAL -> {
                        Log.d(
                            LoyaltyWalletFragment::class.java.simpleName,
                            getString(R.string.loayalty_wallet_dialog_description)
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

    override fun onPause() {
        binding.progressSpinner.visibility = View.INVISIBLE
        binding.swipeLayout.isRefreshing = false
        super.onPause()
    }

    private fun merchantNoLoyalty(
        cardsReceived: List<MembershipCard>,
        plan: MembershipPlan
    ): Boolean {
        return cardsReceived.firstOrNull { card ->
            card.membership_plan == plan.id
        } == null
    }

    private fun populateScreen(
        plansReceived: List<MembershipPlan>,
        cardsReceived: List<MembershipCard>
    ) {
        viewModel.dismissedCardData.observeNonNull(this) { dismissedCards ->
            if (plansReceived.isNotEmpty()) {
                binding.swipeLayout.isRefreshing = false
                binding.swipeLayout.isEnabled = true
                binding.progressSpinner.visibility = View.GONE

                walletItems = ArrayList(plansReceived.filter {
                    it.getCardType() == CardType.PLL &&
                            merchantNoLoyalty(cardsReceived, it) &&
                            dismissedCards.firstOrNull { currentId ->
                                it.id == currentId.id
                            } == null
                })

                if (dismissedCards.firstOrNull { it.id == JOIN_CARD } == null &&
                    SharedPreferenceManager.isPaymentEmpty) {
                    walletItems.add(JoinCardItem())
                }

                walletItems.addAll(cardsReceived)

                walletAdapter.membershipPlans = plansReceived as ArrayList<MembershipPlan>
                walletAdapter.membershipCards = walletItems
                walletAdapter.notifyDataSetChanged()

                if (plansReceived.isNotEmpty() &&
                    cardsReceived.isNotEmpty()
                ) {
                    viewModel.dismissedCardData.removeObservers(this@LoyaltyWalletFragment)
                }
            }
        }
        viewModel.fetchDismissedCards()
    }

    private fun onBannerRemove(item: Any) {
        when (item) {
            is MembershipPlan -> viewModel.addPlanIdAsDismissed(item.id)
            else -> viewModel.addPlanIdAsDismissed((item as JoinCardItem).id)
        }
        walletAdapter.membershipCards.remove(item)
        walletAdapter.notifyDataSetChanged()
    }

    fun setData(
        membershipCards: List<MembershipCard>,
        membershipPlans: List<MembershipPlan>
    ) {
        viewModel.run {
            membershipCardData.value = membershipCards
            membershipPlanData.value = membershipPlans
        }
    }
}
