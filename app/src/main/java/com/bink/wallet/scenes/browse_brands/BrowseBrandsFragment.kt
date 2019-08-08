package com.bink.wallet.scenes.browse_brands

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import com.bink.wallet.R
import kotlinx.android.synthetic.main.browse_brands_fragment.*
import org.koin.androidx.viewmodel.ext.android.viewModel

class BrowseBrandsFragment : Fragment() {

    private val viewModel: BrowseBrandsViewModel by viewModel()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.browse_brands_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        viewModel.fetchMembershipPlans()

        viewModel.membershipPlanData.observe(this, Observer {
            browse_brands_container.apply {
                layoutManager = GridLayoutManager(activity, 1)
                adapter = BrowseBrandsAdapter(it)
            }
        })
    }
}
