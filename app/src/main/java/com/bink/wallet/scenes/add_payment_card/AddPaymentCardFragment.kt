package com.bink.wallet.scenes.add_payment_card

import com.bink.wallet.BaseFragment
import com.bink.wallet.R
import com.bink.wallet.databinding.AddPaymentCardFragmentBinding
import com.bink.wallet.utils.toolbar.FragmentToolbar
import org.koin.androidx.viewmodel.ext.android.viewModel

class AddPaymentCardFragment :
    BaseFragment<AddPaymentCardViewModel, AddPaymentCardFragmentBinding>() {

    override fun builder(): FragmentToolbar {
        return FragmentToolbar.Builder()
            .withId(FragmentToolbar.NO_TOOLBAR)
            .build()
    }

    override val viewModel: AddPaymentCardViewModel by viewModel()

    override val layoutRes: Int
        get() = R.layout.add_payment_card_fragment
}
