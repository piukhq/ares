package com.bink.wallet.scenes.browse_brands

import android.os.Bundle
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import com.bink.wallet.BaseFragment
import com.bink.wallet.R
import com.bink.wallet.databinding.BrowseBrandsFragmentBinding
import com.bink.wallet.scenes.browse_brands.model.MembershipPlan
import kotlinx.android.synthetic.main.browse_brands_fragment.*
import org.koin.androidx.viewmodel.ext.android.viewModel

class BrowseBrandsFragment : BaseFragment<BrowseBrandsViewModel, BrowseBrandsFragmentBinding>() {
    override val layoutRes: Int
        get() = R.layout.browse_brands_fragment

    override val viewModel: BrowseBrandsViewModel by viewModel()

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        viewModel.fetchMembershipPlans()

        viewModel.membershipPlanData.observe(this, Observer {
            browse_brands_container.apply {
                layoutManager = GridLayoutManager(activity, 1)
                adapter = BrowseBrandsAdapter(it, itemClickListener = { toAddJoinScreen(it) })
            }
        })
    }

    private fun toAddJoinScreen(membershipPlan: MembershipPlan) {
        val action = BrowseBrandsFragmentDirections.browseToAddJoin(membershipPlan)
        findNavController().navigate(action)
    }
}
