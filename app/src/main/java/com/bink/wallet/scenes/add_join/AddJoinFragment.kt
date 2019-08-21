package com.bink.wallet.scenes.add_join

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.navArgs
import com.bink.wallet.BaseFragment
import com.bink.wallet.R
import com.bink.wallet.databinding.AddJoinFragmentBinding
import kotlinx.android.synthetic.main.add_join_fragment.*
import org.koin.androidx.viewmodel.ext.android.viewModel

class AddJoinFragment : BaseFragment<AddJoinViewModel, AddJoinFragmentBinding>() {
    override val layoutRes: Int
        get() = R.layout.add_join_fragment

    private val args: AddJoinFragmentArgs by navArgs()

    override val viewModel: AddJoinViewModel by viewModel()

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val currentMembershipPlan = args.currentMembershipPlan
        binding.item = currentMembershipPlan

        when (currentMembershipPlan.account?.plan_name_card.isNullOrEmpty()) {
            false -> add_join_reward.text =
                currentMembershipPlan.account?.plan_name_card.plus(" " + getString(R.string.plan_name_card_extra))
            true -> add_join_reward.visibility = View.GONE
        }
        if (args.currentMembershipPlan.feature_set?.linking_support?.filter { it == "REGISTRATION" }?.size!! == 0) {
            add_join_view_image.setImageDrawable(context?.getDrawable(R.drawable.ic_icons_svl_view_inactive))
            add_join_view_description.text = getString(R.string.add_join_inactive_view_description)
        }

        if (args.currentMembershipPlan.feature_set?.linking_support?.filter { it == "ENROL" }?.size!! == 0) {
            add_join_link_image.setImageDrawable(context?.getDrawable(R.drawable.ic_icons_svl_link_inactive))
            add_join_link_description.text = getString(R.string.add_join_inactive_link_description)
        }
    }

}
