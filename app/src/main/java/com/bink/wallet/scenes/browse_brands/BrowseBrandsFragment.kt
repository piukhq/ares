package com.bink.wallet.scenes.browse_brands

import android.os.Bundle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.bink.wallet.BaseFragment
import com.bink.wallet.R
import com.bink.wallet.databinding.BrowseBrandsFragmentBinding
import com.bink.wallet.model.response.membership_plan.MembershipPlan
import com.bink.wallet.utils.navigateIfAdded
import kotlinx.android.synthetic.main.browse_brands_fragment.*
import org.koin.androidx.viewmodel.ext.android.viewModel

class BrowseBrandsFragment : BaseFragment<BrowseBrandsViewModel, BrowseBrandsFragmentBinding>() {
    override val layoutRes: Int
        get() = R.layout.browse_brands_fragment

    override val viewModel: BrowseBrandsViewModel by viewModel()

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        arguments?.let {
            val plans = BrowseBrandsFragmentArgs.fromBundle(it).membershipPlans
            browse_brands_container.apply {
                layoutManager = GridLayoutManager(activity, 1)
                adapter = BrowseBrandsAdapter(plans.toList(), itemClickListener = { toAddJoinScreen(it) })
            }
        }

        binding.toolbar.setNavigationIcon(R.drawable.ic_back)
        binding.toolbar.setNavigationOnClickListener {
            activity?.onBackPressed()
        }
    }

    private fun toAddJoinScreen(membershipPlan: MembershipPlan) {
        val action = BrowseBrandsFragmentDirections.browseToAddJoin(membershipPlan)
        findNavController().navigateIfAdded(this, action)
    }
}
