package com.bink.wallet.scenes.loyalty_wallet;

import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bink.wallet.R
import com.bink.wallet.databinding.FragmentLoyaltyWalletBinding
import com.bink.wallet.scenes.loyalty_wallet.RecyclerItemTouchHelper.RecyclerItemTouchHelperListener
import kotlinx.android.synthetic.main.fragment_loyalty_wallet.*
import org.koin.androidx.viewmodel.ext.android.viewModel


class LoyaltyWalletFragment : Fragment() {
    companion object {
        fun newInstance() = LoyaltyWalletFragment()
    }

    private var TAG = LoyaltyWalletFragment::class.simpleName
    private lateinit var binding: FragmentLoyaltyWalletBinding

    private val viewModel: LoyaltyViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_loyalty_wallet, container, false )
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val listener: RecyclerItemTouchHelperListener = object :
            RecyclerItemTouchHelperListener {

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int, position: Int) {
                if (viewHolder is LoyaltyWalletAdapter.MyViewHolder) {

                    if (direction == ItemTouchHelper.RIGHT) {
                        Log.i("LoyaltyWalletAdapter", "right swipe : barcode $position")
                    } else {
                        viewModel.deleteCard()
                    }

                }
            }
        }

        viewModel.deleteCard.observe(this, Observer {
            Log.e("Tag", "Deleted!")

        })

        activity?.let {
            it.setActionBar(binding.toolbar)
            it.actionBar?.setDisplayShowTitleEnabled(false)
        }

        viewModel.fetchMembershipCards()


        viewModel.membershipCardData.observe(this, Observer {
            loyalty_wallet_list.apply {
                layoutManager = LinearLayoutManager(activity, RecyclerView.VERTICAL, false)
                adapter = LoyaltyWalletAdapter(it)

                val helperListener = RecyclerItemTouchHelper(0, ItemTouchHelper.LEFT, listener)

                ItemTouchHelper(helperListener).attachToRecyclerView(this)
                ItemTouchHelper(RecyclerItemTouchHelper(0, ItemTouchHelper.RIGHT, listener)).attachToRecyclerView(this)
            }

            bottom_navigation.setOnNavigationItemSelectedListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.loyalty_menu_item -> Log.e(TAG, "Loyalty tab")
                    R.id.add_menu_item -> findNavController().navigate(R.id.home_to_add)
                    R.id.payment_menu_item -> Log.e(TAG, "Payment tab")
                }
                true
            }
        })
    }
}
