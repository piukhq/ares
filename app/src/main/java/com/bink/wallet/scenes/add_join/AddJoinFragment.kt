package com.bink.wallet.scenes.add_join

import android.os.Bundle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bink.wallet.BaseFragment
import com.bink.wallet.R
import com.bink.wallet.databinding.AddJoinFragmentBinding
import com.bink.wallet.utils.navigateIfAdded
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

        if (args.currentMembershipPlan.feature_set?.linking_support?.filter { it == "REGISTRATION" }?.size!! == 0) {
            add_join_view_image.setImageDrawable(context?.getDrawable(R.drawable.ic_icons_svl_view_inactive))
            add_join_view_description.text = getString(R.string.add_join_inactive_view_description)
        }

        if (args.currentMembershipPlan.feature_set?.linking_support?.filter { it == "ENROL" }?.size!! == 0) {
            add_join_link_image.setImageDrawable(context?.getDrawable(R.drawable.ic_icons_svl_link_inactive))
            add_join_link_description.text = getString(R.string.add_join_inactive_link_description)
        }

        binding.toolbar.setNavigationIcon(R.drawable.ic_back)
        binding.toolbar.setNavigationOnClickListener {
            activity?.onBackPressed()
        }

        binding.close.setOnClickListener {
            findNavController().navigateIfAdded(this, R.id.add_join_to_home)
        }

        binding.addCardButton.setOnClickListener {
            val action = AddJoinFragmentDirections.addJoinToAddAuth(currentMembershipPlan)
            findNavController().navigateIfAdded(this, action)
        }
    }

}
