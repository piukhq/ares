package com.bink.wallet.scenes.add_join

import android.os.Bundle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bink.wallet.BaseFragment
import com.bink.wallet.R
import com.bink.wallet.databinding.AddJoinFragmentBinding
import com.bink.wallet.utils.navigateIfAdded
import com.bink.wallet.utils.toolbar.FragmentToolbar
import kotlinx.android.synthetic.main.add_join_fragment.*
import org.koin.androidx.viewmodel.ext.android.viewModel

class AddJoinFragment : BaseFragment<AddJoinViewModel, AddJoinFragmentBinding>() {
    override fun builder(): FragmentToolbar {
        return FragmentToolbar.Builder()
            .withId(FragmentToolbar.NO_TOOLBAR)
            .build()
    }

    override val layoutRes: Int
        get() = R.layout.add_join_fragment

    private val args: AddJoinFragmentArgs by navArgs()

    override val viewModel: AddJoinViewModel by viewModel()

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val currentMembershipPlan = args.currentMembershipPlan
        binding.item = currentMembershipPlan

        if (args.currentMembershipPlan.feature_set?.card_type == 0) {
            binding.addJoinViewImage.setImageDrawable(context?.getDrawable(R.drawable.ic_icons_svl_view_inactive))
            binding.addJoinViewDescription.text = getString(R.string.add_join_inactive_view_description)
        }

        if (args.currentMembershipPlan.feature_set?.card_type != 2) {
            binding.addJoinLinkImage.setImageDrawable(context?.getDrawable(R.drawable.ic_icons_svl_link_inactive))
            binding.addJoinLinkDescription.text = getString(R.string.add_join_inactive_link_description)
        }

        binding.addCardButton.setOnClickListener {
            val action = AddJoinFragmentDirections.addJoinToAddAuth(currentMembershipPlan, null)
            findNavController().navigateIfAdded(this, action)
        }
    }

}
