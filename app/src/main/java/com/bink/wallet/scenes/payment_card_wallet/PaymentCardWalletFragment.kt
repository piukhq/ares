package com.bink.wallet.scenes.payment_card_wallet

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import com.bink.wallet.BaseFragment
import com.bink.wallet.R
import com.bink.wallet.databinding.PaymentCardWalletFragmentBinding
import com.bink.wallet.utils.observeNonNull
import com.bink.wallet.utils.toolbar.FragmentToolbar
import kotlinx.coroutines.runBlocking
import org.koin.androidx.viewmodel.ext.android.viewModel

class PaymentCardWalletFragment :
    BaseFragment<PaymentCardWalletViewModel, PaymentCardWalletFragmentBinding>() {

    override fun builder(): FragmentToolbar {
        return FragmentToolbar.Builder()
            .withId(FragmentToolbar.NO_TOOLBAR)
            .build()
    }

    override val layoutRes: Int
        get() = R.layout.payment_card_wallet_fragment

    override val viewModel: PaymentCardWalletViewModel by viewModel()

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        runBlocking {
            viewModel.getPaymentCards()
            binding.progressSpinner.visibility = View.VISIBLE
        }

        viewModel.paymentCards.observeNonNull(this) {
            binding.progressSpinner.visibility = View.INVISIBLE
            binding.paymentCardRecycler.apply {
                layoutManager = GridLayoutManager(activity, 1)
                adapter =
                    PaymentCardWalletAdapter(
                        viewModel.paymentCards.value!!,
                        onClickListener = {
                            //                            onCardClicked(it)
                        })
            }

        }

    }

}
