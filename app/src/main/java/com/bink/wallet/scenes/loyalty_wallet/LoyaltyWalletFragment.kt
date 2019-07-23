package com.bink.wallet.scenes.loyalty_wallet;

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bink.wallet.R
import com.bink.wallet.scenes.loyalty_wallet.RecyclerItemTouchHelper.RecyclerItemTouchHelperListener
import kotlinx.android.synthetic.main.fragment_loyalty_wallet.*


interface LoyaltyWalletDisplayLogic {
    fun displaySomething(viewModel: LoyaltyWalletModels.Something.ViewModel)
}

class LoyaltyWalletFragment : Fragment(), LoyaltyWalletDisplayLogic {

    lateinit var interactor: LoyaltyWalletBusinessLogic
    lateinit var router: ILoyaltyWalletRouter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_loyalty_wallet, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setup()

        val listener: RecyclerItemTouchHelperListener = object : RecyclerItemTouchHelperListener {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int, position: Int) {
                if (viewHolder is LoyaltyWalletAdapter.MyViewHolder) {
                    /*// get the removed item name to display it in snack bar
                    val name = cartList.get(viewHolder.adapterPosition).getName()

                    // backup of removed item for undo purpose
                    val deletedItem = cartList.get(viewHolder.adapterPosition)
                    val deletedIndex = viewHolder.adapterPosition

                    // remove the item from recycler view
                    mAdapter.removeItem(viewHolder.adapterPosition)

                    // showing snack bar with Undo option
                    val snackbar = Snackbar
                        .make(coordinatorLayout, name + " removed from cart!", Snackbar.LENGTH_LONG)
                    snackbar.setAction("UNDO", View.OnClickListener {
                        // undo is selected, restore the deleted item
                        mAdapter.restoreItem(deletedItem, deletedIndex)
                    })
                    snackbar.setActionTextColor(Color.YELLOW)
                    snackbar.show()*/
                }
            }
        }

        loyalty_wallet_list.apply {
            layoutManager = LinearLayoutManager(activity, RecyclerView.VERTICAL ,false)
            adapter = LoyaltyWalletAdapter()
            ItemTouchHelper(RecyclerItemTouchHelper(0, ItemTouchHelper.LEFT, listener)).attachToRecyclerView(this)
            ItemTouchHelper(RecyclerItemTouchHelper(0, ItemTouchHelper.RIGHT, listener)).attachToRecyclerView(this)

        }


    }

    // Object lifecycle

    private fun setup() {
        // Setup the interactor, presenter, router and wire everything together

        val navController = findNavController()
        val fragment = this
        val interactor = LoyaltyWalletInteractor()
        val presenter = LoyaltyWalletPresenter()
        val router = LoyaltyWalletRouter()
        fragment.interactor = interactor
        fragment.router = router
        interactor.presenter = presenter
        presenter.fragment = fragment
        router.fragment = fragment
        router.dataStore = interactor
        router.navController = navController
    }

    // Do something

    fun doSomething() {
        val request = LoyaltyWalletModels.Something.Request()
        interactor?.doSomething(request)
    }

    override fun displaySomething(viewModel: LoyaltyWalletModels.Something.ViewModel) {

    }
}
