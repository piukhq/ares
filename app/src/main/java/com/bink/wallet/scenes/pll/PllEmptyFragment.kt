package com.bink.wallet.scenes.pll

import android.os.Bundle
import androidx.navigation.fragment.findNavController
import com.bink.wallet.BaseFragment
import com.bink.wallet.R
import com.bink.wallet.databinding.FragmentPllEmptyBinding
import com.bink.wallet.model.response.membership_card.MembershipCard
import com.bink.wallet.model.response.membership_plan.MembershipPlan
import com.bink.wallet.utils.navigateIfAdded
import org.koin.androidx.viewmodel.ext.android.viewModel

class PllEmptyFragment : BaseFragment<PllViewModel, FragmentPllEmptyBinding>() {

    override val layoutRes: Int
        get() = R.layout.fragment_pll_empty

    private lateinit var currentMembershipCard: MembershipCard
    private lateinit var currentMembershipPlan: MembershipPlan

    override val viewModel: PllViewModel by viewModel()

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        arguments.let {

            currentMembershipCard =
                it?.let { it }?.let { it1 -> PllEmptyFragmentArgs.fromBundle(it1).membershipCard }!!

            currentMembershipPlan =
                it.let { it }.let { it1 -> PllEmptyFragmentArgs.fromBundle(it1).membershipPlan }

        }

        binding.toolbar.setNavigationIcon(R.drawable.ic_back)
        binding.toolbar.setNavigationOnClickListener {
            activity?.onBackPressed()
        }

        binding.buttonDone.setOnClickListener {

            val directions =
                PllEmptyFragmentDirections.pllEmptyToDetail(
                    currentMembershipPlan, currentMembershipCard
                )

            findNavController().navigateIfAdded(this, directions)
        }

    }
}
