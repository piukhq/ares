package com.bink.wallet.scenes.add_join

import android.os.Bundle
import androidx.navigation.fragment.navArgs
import com.bink.wallet.BaseFragment
import com.bink.wallet.R
import com.bink.wallet.databinding.AddJoinRequestPaymentCardBinding
import com.bink.wallet.utils.toolbar.FragmentToolbar
import org.koin.androidx.viewmodel.ext.android.viewModel

class AddJoinRequestPaymentCardFragment : BaseFragment<AddJoinViewModel, AddJoinRequestPaymentCardBinding>() {
    override fun builder(): FragmentToolbar {
        return FragmentToolbar.Builder()
            .with(binding.toolbar)
            .shouldDisplayBack(requireActivity())
            .build()
    }

    override val layoutRes: Int
        get() = R.layout.add_join_request_payment_card

    override val viewModel: AddJoinViewModel by viewModel()

    private val args: AddJoinFragmentArgs by navArgs()

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        viewModel.membershipPlan.value = args.currentMembershipPlan


    }
}