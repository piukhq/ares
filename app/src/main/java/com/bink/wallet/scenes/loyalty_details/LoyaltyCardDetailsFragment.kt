package com.bink.wallet.scenes.loyalty_details

import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bink.wallet.BaseFragment
import com.bink.wallet.R
import org.koin.androidx.viewmodel.ext.android.viewModel
import com.bink.wallet.databinding.FragmentLoyaltyCardDetailsBinding

class LoyaltyCardDetailsFragment: BaseFragment<LoyaltyCardDetailsViewModel, FragmentLoyaltyCardDetailsBinding>() {

    override val viewModel: LoyaltyCardDetailsViewModel by viewModel()
    override val layoutRes: Int
        get() = R.layout.fragment_loyalty_card_details

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        binding.toolbar.setNavigationIcon(R.drawable.ic_close)
        binding.toolbar.setNavigationOnClickListener {
            activity?.onBackPressed()
        }

        arguments?.let {
            viewModel.membershipPlan.value = LoyaltyCardDetailsFragmentArgs.fromBundle(it).membershipPlan
        }

        binding.offerTiles.layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        binding.offerTiles.adapter = viewModel.tiles.value?.let { LoyaltyDetailsTilesAdapter(it) }
    }
}