package com.bink.wallet.scenes.loyalty_wallet;

import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bink.wallet.BaseFragment
import com.bink.wallet.R
import com.bink.wallet.databinding.FragmentLoyaltyWalletBinding
import com.bink.wallet.scenes.loyalty_wallet.RecyclerItemTouchHelper.RecyclerItemTouchHelperListener
import com.bink.wallet.scenes.loyalty_wallet.model.MembershipCard
import com.bink.wallet.utils.navigateIfAdded
import org.koin.androidx.viewmodel.ext.android.viewModel


class LoyaltyWalletFragment : BaseFragment<LoyaltyViewModel, FragmentLoyaltyWalletBinding>() {
    companion object {
        fun newInstance() = LoyaltyWalletFragment()
    }

    private var TAG = LoyaltyWalletFragment::class.simpleName

    override val viewModel: LoyaltyViewModel by viewModel()
    override val layoutRes: Int
        get() = R.layout.fragment_loyalty_wallet

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val listener: RecyclerItemTouchHelperListener = object :
            RecyclerItemTouchHelperListener {

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int, position: Int) {
                if (viewHolder is LoyaltyWalletAdapter.MyViewHolder) {
                    if (direction == ItemTouchHelper.RIGHT) {
                        val card = viewModel.membershipCardData.value?.get(position)
                        if (viewModel.membershipPlanData.value != null) {
                            for (plan in viewModel.membershipPlanData.value!!) {
                                if (plan.id.toString() == card?.membership_plan) {
                                    val directions =
                                        card.card?.barcode_type?.let {
                                            LoyaltyWalletFragmentDirections.homeToBarcode(plan, card.card?.barcode,
                                                it
                                            )
                                        }
                                    directions?.let { findNavController().navigateIfAdded(this@LoyaltyWalletFragment, it) }
                                }
                            }
                        }
                    } else {
                        viewModel.membershipCardData.value?.get(position)?.let { deleteDialog(it) }
                    }
                }
            }
        }

        viewModel.deleteCard.observe(this, Observer { id ->
            viewModel.membershipCardData.value = viewModel.membershipCardData.value?.filter { it.id != id }
        })

        activity?.let {
            it.setActionBar(binding.toolbar)
            it.actionBar?.setDisplayShowTitleEnabled(false)
        }

        viewModel.fetchMembershipPlans()
        viewModel.fetchMembershipCards()

        viewModel.membershipPlanData.observe(this, Observer { plans ->
            viewModel.membershipCardData.observe(this, Observer { cards ->
                binding.loyaltyWalletList.apply {
                    layoutManager = LinearLayoutManager(activity, RecyclerView.VERTICAL, false)
                    adapter = LoyaltyWalletAdapter(plans, cards, itemDeleteListener = { })

                    val helperListener = RecyclerItemTouchHelper(0, ItemTouchHelper.LEFT, listener)

                    ItemTouchHelper(helperListener).attachToRecyclerView(this)
                    ItemTouchHelper(RecyclerItemTouchHelper(0, ItemTouchHelper.RIGHT, listener)).attachToRecyclerView(
                        this
                    )
                }
            })
        })

        binding.bottomNavigation.setOnNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.loyalty_menu_item -> Log.e(TAG, "Loyalty tab")
                R.id.add_menu_item -> {
                    val directions = viewModel.membershipPlanData.value?.toTypedArray()?.let {
                        LoyaltyWalletFragmentDirections.homeToAdd(
                            it
                        )
                    }
                    directions?.let { findNavController().navigateIfAdded(this, it) }
                }
                R.id.payment_menu_item -> Log.e(TAG, "Payment tab")
            }
            true
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
                        viewModel.deleteCard(membershipCard.id)
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
