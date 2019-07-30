package com.bink.wallet.scenes.loyalty_wallet;

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bink.wallet.R
import com.bink.wallet.scenes.loyalty_wallet.RecyclerItemTouchHelper.RecyclerItemTouchHelperListener
import kotlinx.android.synthetic.main.fragment_loyalty_wallet.*

class LoyaltyWalletFragment : Fragment() {
    companion object {
        fun newInstance() = LoyaltyWalletFragment()
    }

    private lateinit var viewModel: LoyaltyViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_loyalty_wallet, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(LoyaltyViewModel::class.java)
        super.onActivityCreated(savedInstanceState)

        val listener: RecyclerItemTouchHelperListener = object :
            RecyclerItemTouchHelperListener {

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int, position: Int) {
                if (viewHolder is LoyaltyWalletAdapter.MyViewHolder) {

                    if (direction == ItemTouchHelper.RIGHT) {
                        Log.i("LoyaltyWalletAdapter", "right swipe : barcode $position")
                    } else {
                        Log.i("LoyaltyWalletAdapter", "left swipe : delete $position")
                    }

                }
            }
        }

        loyalty_wallet_list.apply {
            layoutManager = LinearLayoutManager(activity, RecyclerView.VERTICAL, false)
            adapter = LoyaltyWalletAdapter()

            var helperListener = RecyclerItemTouchHelper(0, ItemTouchHelper.LEFT, listener)

            ItemTouchHelper(helperListener).attachToRecyclerView(this)
            ItemTouchHelper(RecyclerItemTouchHelper(0, ItemTouchHelper.RIGHT, listener)).attachToRecyclerView(this)

        }
    }

}
