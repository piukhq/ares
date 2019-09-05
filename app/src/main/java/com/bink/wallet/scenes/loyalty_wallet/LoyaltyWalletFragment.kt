package com.bink.wallet.scenes.loyalty_wallet;

import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bink.wallet.BaseFragment
import com.bink.wallet.R
import com.bink.wallet.databinding.FragmentLoyaltyWalletBinding
import com.bink.wallet.model.response.membership_card.MembershipCard
import com.bink.wallet.scenes.loyalty_wallet.RecyclerItemTouchHelper.RecyclerItemTouchHelperListener
import com.bink.wallet.utils.navigateIfAdded
import com.bink.wallet.utils.observeNonNull
import kotlinx.android.synthetic.main.fragment_loyalty_wallet.*
import kotlinx.coroutines.runBlocking
import org.koin.androidx.viewmodel.ext.android.viewModel


class LoyaltyWalletFragment : BaseFragment<LoyaltyViewModel, FragmentLoyaltyWalletBinding>() {
    private var TAG = LoyaltyWalletFragment::class.simpleName

    override val viewModel: LoyaltyViewModel by viewModel()
    override val layoutRes: Int
        get() = R.layout.fragment_loyalty_wallet

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    val listener: RecyclerItemTouchHelperListener = object :
        RecyclerItemTouchHelperListener {

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int, position: Int) {
            if (viewHolder is LoyaltyWalletAdapter.MyViewHolder) {
                if (direction == ItemTouchHelper.RIGHT) {
                    val card = viewModel.localMembershipCardData.value?.get(position)
                    if (viewModel.localMembershipPlanData.value != null) {

                        val plan =
                            viewModel.localMembershipPlanData.value?.first { it.id == card?.membership_plan }!!

                        val directions =
                            card?.card?.barcode_type?.let {
                                LoyaltyWalletFragmentDirections.homeToBarcode(
                                    plan, card.card?.barcode, it
                                )
                            }
                        directions?.let {
                            findNavController().navigateIfAdded(
                                this@LoyaltyWalletFragment, it
                            )
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

        viewModel.deleteCard.observe(this, Observer { id ->
            viewModel.membershipCardData.value =
                viewModel.membershipCardData.value?.filter { it.id != id }
        })

        activity?.let {
            it.setActionBar(binding.toolbar)
            it.actionBar?.setDisplayShowTitleEnabled(false)
        }

        if (viewModel.localCardsReceived.value != true || viewModel.localPlansReceived.value != true) {
            binding.progressSpinner.visibility = View.VISIBLE
            binding.swipeLayout.isEnabled = false
            binding.swipeLayout.isRefreshing = false
            viewModel.fetchMembershipCards()
            runBlocking {
                viewModel.fetchMembershipPlans()
            }

            viewModel.membershipCardData.observeNonNull(this) {
                runBlocking {
                    viewModel.fetchMembershipPlans()
                }
            }

            viewModel.membershipPlanData.observeNonNull(this){
                viewModel.fetchLocalMembershipPlans()
            }

            viewModel.localMembershipPlanData.observeNonNull(this) {
                if (it.isNotEmpty()) {
                    viewModel.localPlansReceived.value = true
                    viewModel.fetchLocalMembershipCards()
                }
            }

            viewModel.localMembershipCardData.observeNonNull(this) {
                if (it.isNotEmpty()) {
                    viewModel.localCardsReceived.value = true
                }
            }
        }

        binding.swipeLayout.setOnRefreshListener {
            runBlocking {
                viewModel.fetchMembershipPlans()
                viewModel.fetchMembershipCards()
            }
        }

        viewModel.localCardsReceived.observeNonNull(this) { cardsReceived ->
            viewModel.localPlansReceived.observeNonNull(this) { plansReceived ->
                binding.swipeLayout.isRefreshing = false
                swipe_layout.isEnabled = true
                if (cardsReceived && plansReceived) {
                    binding.progressSpinner.visibility = View.GONE
                    binding.loyaltyWalletList.apply {
                        layoutManager = LinearLayoutManager(activity, RecyclerView.VERTICAL, false)
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

        binding.bottomNavigation.setOnNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.loyalty_menu_item -> Log.e(TAG, "Loyalty tab")
                R.id.add_menu_item -> {
                    viewModel.localMembershipPlanData.observe(this, Observer {
                        val directions = it?.toTypedArray()?.let { plans ->
                            LoyaltyWalletFragmentDirections.homeToAdd(
                                plans
                            )
                        }
                        viewModel.localMembershipPlanData.removeObservers(this)
                        directions?.let { findNavController().navigateIfAdded(this, it) }

                    })

                }
                R.id.payment_menu_item -> Log.e(TAG, "Payment tab")
            }
            true
        }
    }

    private fun onCardClicked(card: MembershipCard) {
        for (membershipPlan in viewModel.localMembershipPlanData.value!!) {
            if (card.membership_plan == membershipPlan.id) {
                val directions =
                    LoyaltyWalletFragmentDirections.homeToDetail(
                        membershipPlan,
                        card
                    )
                findNavController().navigateIfAdded(
                    this@LoyaltyWalletFragment,
                    directions
                )
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
                        runBlocking {
                            viewModel.deleteCard(membershipCard.id)
                        }
                        binding.loyaltyWalletList.adapter?.notifyDataSetChanged()
                    }
                    DialogInterface.BUTTON_NEUTRAL -> {
                        Log.d(
                            LoyaltyWalletFragment::class.java.simpleName,
                            getString(R.string.loayalty_wallet_dialog_description)
                        )
                        binding.loyaltyWalletList.adapter?.notifyDataSetChanged()
                    }

                }
            }
            builder.setPositiveButton(getString(R.string.yes_text), dialogClickListener)
            builder.setNeutralButton(getString(R.string.cancel_text), dialogClickListener)
            dialog = builder.create()
            dialog.show()
        }
    }
}
