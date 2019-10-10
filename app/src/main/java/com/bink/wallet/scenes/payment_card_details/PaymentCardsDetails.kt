package com.bink.wallet.scenes.payment_card_details

import android.os.Bundle
import androidx.recyclerview.widget.GridLayoutManager
import com.bink.wallet.BaseFragment
import com.bink.wallet.R
import com.bink.wallet.databinding.PaymentCardsDetailsFragmentBinding
import com.bink.wallet.utils.toolbar.FragmentToolbar
import org.koin.androidx.viewmodel.ext.android.viewModel

class PaymentCardsDetails :
    BaseFragment<PaymentCardsDetailsViewModel, PaymentCardsDetailsFragmentBinding>() {

    override fun builder(): FragmentToolbar {
        return FragmentToolbar.Builder()
            .withId(FragmentToolbar.NO_TOOLBAR)
            .build()
    }

    override val viewModel: PaymentCardsDetailsViewModel by viewModel()

    override val layoutRes: Int
        get() = R.layout.payment_cards_details_fragment

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        arguments?.let {
            viewModel.paymentCard.value =
                PaymentCardsDetailsArgs.fromBundle(it).paymentCard
        }

        binding.linkedCardsList.apply {
            layoutManager = GridLayoutManager(context, 1)
            adapter = LinkedCardsAdapter(viewModel.paymentCard.value?.membership_cards!!)
        }

    }

}
