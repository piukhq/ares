package com.bink.wallet.scenes.add_join

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.bink.wallet.R
import com.bink.wallet.databinding.AddJoinFragmentBinding
import kotlinx.android.synthetic.main.add_join_fragment.*
import org.koin.androidx.viewmodel.ext.android.viewModel

class AddJoinFragment : Fragment() {

    val args: AddJoinFragmentArgs by navArgs()

    private val viewModel: AddJoinViewModel by viewModel()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val addJoinBinding: AddJoinFragmentBinding =
            DataBindingUtil.inflate(inflater, R.layout.add_join_fragment, container, false)
        val currentMembershipPlan = args.currentMembershipPlan
        addJoinBinding.item = currentMembershipPlan


        return addJoinBinding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        if (args.currentMembershipPlan.feature_set?.linking_support?.filter { it == "REGISTRATION" }?.size!! > 0) {
            add_join_view_image.setImageDrawable(context?.getDrawable(R.drawable.ic_icons_svl_view_inactive))
            add_join_view_description.text = getString(R.string.add_join_inactive_view_description)
        }

        if (args.currentMembershipPlan.feature_set?.linking_support?.filter { it == "ENROL" }?.size!! > 0) {
            add_join_link_image.setImageDrawable(context?.getDrawable(R.drawable.ic_icons_svl_link_inactive))
            add_join_link_description.text = getString(R.string.add_join_inactive_link_description)
        }
    }

}
