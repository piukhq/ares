package com.bink.wallet.scenes.loyalty_wallet;

import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.Observer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bink.wallet.BaseFragment
import com.bink.wallet.R
import com.bink.wallet.databinding.FragmentLoyaltyWalletBinding
import com.bink.wallet.model.response.membership_card.MembershipCard
import com.bink.wallet.scenes.loyalty_wallet.RecyclerItemTouchHelper.RecyclerItemTouchHelperListener
import com.bink.wallet.scenes.wallets.WalletsFragmentDirections
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

    private lateinit var nestedNavController: NavController

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        nestedNavController = Navigation.findNavController(view)
    }

    private var TAG = LoyaltyWalletFragment::class.simpleName

    override val viewModel: LoyaltyViewModel by viewModel()
    override val layoutRes: Int
        get() = R.layout.fragment_loyalty_wallet

    val listener: RecyclerItemTouchHelperListener = object :
        RecyclerItemTouchHelperListener {

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int, position: Int) {
            if (viewHolder is LoyaltyWalletAdapter.LoyaltyWalletViewHolder) {
                if (direction == ItemTouchHelper.RIGHT) {
                    val card = viewModel.localMembershipCardData.value?.get(position)
                    if (viewModel.localMembershipPlanData.value != null) {
                        val plan =
                            viewModel.localMembershipPlanData.value?.first { it.id == card?.membership_plan }!!

                        val directions =
                            card?.card?.barcode_type?.let {
                                WalletsFragmentDirections.homeToBarcode(
                                    plan, card
                                )
                            }
                        if (findNavController().currentDestination?.id == R.id.loyalty_wallet_fragment) {
                            directions?.let {
                                findNavController().navigateIfAdded(
                                    this@LoyaltyWalletFragment, it
                                )
                            }
                            this@LoyaltyWalletFragment.onDestroy()
                        }
                    }
                } else {
                    viewModel.localMembershipCardData.value?.get(position)
                        ?.let { deleteDialog(it) }
                }
            }
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        setHasOptionsMenu(true)

        viewModel.deleteCard.observe(this, Observer { id ->
            viewModel.membershipCardData.value =
                viewModel.membershipCardData.value?.filter { it.id != id }
            (binding.loyaltyWalletList.adapter as LoyaltyWalletAdapter).deleteCard(id)
        })

        if (!viewModel.localPlansReceived.hasObservers())
            viewModel.localPlansReceived.observeNonNull(this) { plansReceived ->
                if (!viewModel.localCardsReceived.hasObservers())
                    viewModel.localCardsReceived.observeNonNull(this) { cardsReceived ->
                        binding.swipeLayout.isRefreshing = false
                        binding.swipeLayout.isEnabled = true
                        if (cardsReceived && plansReceived) {
                            binding.progressSpinner.visibility = View.GONE
                            binding.loyaltyWalletList.apply {
                                layoutManager =
                                    LinearLayoutManager(activity, RecyclerView.VERTICAL, false)
                                adapter =
                                    LoyaltyWalletAdapter(
                                        viewModel.localMembershipPlanData.value!!,
                                        viewModel.localMembershipCardData.value!!,
                                        itemDeleteListener = { }, onClickListener = {
                                            onCardClicked(it)
                                        })

                                val helperListener =
                                    RecyclerItemTouchHelper(0, ItemTouchHelper.LEFT, listener)

                                ItemTouchHelper(helperListener).attachToRecyclerView(this)
                                ItemTouchHelper(
                                    RecyclerItemTouchHelper(
                                        0,
                                        ItemTouchHelper.RIGHT,
                                        listener
                                    )
                                ).attachToRecyclerView(
                                    this
                                )
                            }
                        }
                    }
            }

        binding.progressSpinner.visibility = View.VISIBLE

        runBlocking {
            if (verifyAvailableNetwork(requireActivity())) {
                binding.swipeLayout.isEnabled = false
                binding.swipeLayout.isRefreshing = false
                viewModel.fetchMembershipPlans()
            } else {
                showNoInternetConnectionDialog()
            }
        }

        if (!viewModel.membershipCardData.hasObservers()) {
            viewModel.membershipCardData.observeNonNull(this) {
                viewModel.fetchLocalMembershipCards()
            }
        }

        if (!viewModel.membershipPlanData.hasObservers()) {
            viewModel.membershipPlanData.observeNonNull(this) {
                viewModel.fetchLocalMembershipPlans()
            }
        }

        if (!viewModel.localMembershipPlanData.hasObservers()) {
            viewModel.localMembershipPlanData.observeNonNull(this) {
                viewModel.fetchMembershipCards()
            }
        }

        if (!viewModel.localMembershipCardData.hasObservers()) {
            viewModel.localMembershipCardData.observeNonNull(this) {
                if (it.isNotEmpty()) {
                    viewModel.localPlansReceived.value = true
                    viewModel.localCardsReceived.value = true
                }
            }
        }

        binding.swipeLayout.setOnRefreshListener {
            if (verifyAvailableNetwork(activity!!)) {
                runBlocking {
                    viewModel.fetchMembershipPlans()
                }
            } else {
                showNoInternetConnectionDialog()
            }
        }
    }

    fun changeScreen() {
        viewModel.localMembershipPlanData.observe(this, Observer {
            val directions = it?.toTypedArray()?.let { plans ->
                WalletsFragmentDirections.homeToAdd(
                    plans
                )
            }
            viewModel.localMembershipPlanData.removeObservers(this)
            directions?.let { nestedNavController.navigateIfAdded(this, it) }
            this@LoyaltyWalletFragment.onDestroy()
        })
    }

    private fun onCardClicked(card: MembershipCard) {
        for (membershipPlan in viewModel.localMembershipPlanData.value!!) {
            if (card.membership_plan == membershipPlan.id) {
                val directions =
                    WalletsFragmentDirections.homeToDetail(
                        membershipPlan,
                        card
                    )
                findNavController().navigateIfAdded(
                    this@LoyaltyWalletFragment,
                    directions
                )
                this@LoyaltyWalletFragment.onDestroy()
            }
        }
    }

    fun deleteDialog(membershipCard: MembershipCard) {
        lateinit var dialog: AlertDialog
        val builder = context?.let { AlertDialog.Builder(it) }
        if (builder != null) {
            builder.setTitle(getString(R.string.loayalty_wallet_dialog_title))
            val dialogClickListener = DialogInterface.OnClickListener { _, which ->
                when (which) {
                    DialogInterface.BUTTON_POSITIVE -> {
                        if (verifyAvailableNetwork(activity!!)) {
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
}
