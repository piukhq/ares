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
import com.bink.wallet.R
import com.bink.wallet.databinding.FragmentLoyaltyWalletBinding
import com.bink.wallet.model.JoinCardItem
import com.bink.wallet.model.response.membership_card.MembershipCard
import com.bink.wallet.model.response.membership_card.UserDataResult
import com.bink.wallet.model.response.membership_plan.MembershipPlan
import com.bink.wallet.scenes.loyalty_wallet.RecyclerItemTouchHelper.RecyclerItemTouchHelperListener
import com.bink.wallet.scenes.wallets.WalletsFragmentDirections
import com.bink.wallet.utils.JOIN_CARD
import com.bink.wallet.utils.UtilFunctions.isNetworkAvailable
import com.bink.wallet.utils.enums.CardType
import com.bink.wallet.utils.navigateIfAdded
import com.bink.wallet.utils.observeNonNull
import com.bink.wallet.utils.toolbar.FragmentToolbar
import kotlinx.coroutines.runBlocking
import org.koin.androidx.viewmodel.ext.android.viewModel

class LoyaltyWalletFragment : BaseFragment<LoyaltyViewModel, FragmentLoyaltyWalletBinding>() {

    override val viewModel: LoyaltyViewModel by viewModel()
    override val layoutRes: Int
        get() = R.layout.fragment_loyalty_wallet

    private val walletAdapter = LoyaltyWalletAdapter(
        onClickListener = {
            onCardClicked(it)
        },
        onRemoveListener = { onBannerRemove(it) }
    )
    private var walletItems = ArrayList<Any>()

    private val listener = object :
        RecyclerItemTouchHelperListener {
        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int, position: Int) {
            if (viewHolder is LoyaltyWalletAdapter.LoyaltyWalletViewHolder) {
                if (direction == ItemTouchHelper.RIGHT &&
                    walletItems[position] is MembershipCard
                ) {
                    val card = walletItems[position] as MembershipCard
                    val plan =
                        viewModel.localMembershipPlanData.value?.firstOrNull {
                            it.id == card.membership_plan
                        }

                    val directions =
                        card.card?.barcode_type?.let {
                            plan?.let {
                                WalletsFragmentDirections.homeToBarcode(
                                    plan, card
                                )
                            }
                        }
                    if (findNavController().currentDestination?.id == R.id.home_wallet) {
                        directions?.let {
                            findNavController().navigateIfAdded(
                                this@LoyaltyWalletFragment, it
                            )
                        }
                        this@LoyaltyWalletFragment.onDestroy()
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.cardsDataMerger.observe(viewLifecycleOwner, Observer { userDataResult ->
            setCardsData(userDataResult)
        })

        viewModel.localCardsDataMerger.observe(viewLifecycleOwner, Observer { localUserDataResult ->
            setCardsData(localUserDataResult)
        })

        viewModel.dismissedBannerDisplay.observe(viewLifecycleOwner, Observer {
            walletAdapter.deleteBannerDisplayById(it)
            viewModel.fetchDismissedCards()
            binding.progressSpinner.visibility = View.VISIBLE
            binding.swipeLayout.isEnabled = true
        })

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

        if (isNetworkAvailable(requireActivity(), false)) {
            runBlocking {
                viewModel.fetchMembershipPlans()
                viewModel.fetchMembershipCards()
            }
            binding.swipeLayout.isEnabled = false
            binding.swipeLayout.isRefreshing = false
        } else {
            disableIndicators()
        }

        viewModel.fetchLocalMembershipPlans()
        viewModel.fetchLocalMembershipCards()
        viewModel.fetchDismissedCards()

        binding.swipeLayout.setOnRefreshListener {
            if (isNetworkAvailable(requireActivity(), true)) {
                createFetchObservers()
                runBlocking {
                    viewModel.fetchMembershipPlans()
                    viewModel.fetchMembershipCards()
                }
            } else {
                disableIndicators()
            }
        }
    }

    override fun onPause() {
        binding.progressSpinner.visibility = View.INVISIBLE
        binding.swipeLayout.isRefreshing = false
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
                binding.progressSpinner.visibility = View.GONE
                binding.swipeLayout.isRefreshing = false
                walletItems.addAll(userDataResult.result.third)
                walletAdapter.membershipCards = ArrayList(userDataResult.result.third)
                walletAdapter.membershipPlans = ArrayList(userDataResult.result.second)
                walletAdapter.notifyDataSetChanged()
            }
        }
    }

    private fun disableIndicators() {
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
                        item
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
            builder.setTitle(getString(R.string.loayalty_wallet_dialog_title))
            val dialogClickListener = DialogInterface.OnClickListener { _, which ->
                when (which) {
                    DialogInterface.BUTTON_POSITIVE -> {
                        if (isNetworkAvailable(requireActivity(), true)) {
                            runBlocking {
                                viewModel.deleteCard(membershipCard.id)
                            }
                        } else {
                            disableIndicators()
                        }
                        binding.loyaltyWalletList.adapter?.notifyItemChanged(position)
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

    fun setData(
        membershipCards: List<MembershipCard>,
        membershipPlans: List<MembershipPlan>
    ) {
        viewModel.membershipCardData.value = membershipCards
        viewModel.membershipPlanData.value = membershipPlans
    }
}
