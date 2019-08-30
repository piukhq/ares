package com.bink.wallet.scenes.add_auth

import android.os.Bundle
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.GridLayoutManager
import com.bink.wallet.BaseFragment
import com.bink.wallet.R
import com.bink.wallet.databinding.AddAuthFragmentBinding
import com.bink.wallet.model.request.membership_card.Account
import com.bink.wallet.model.request.membership_card.MembershipCardRequest
import com.bink.wallet.scenes.add_join.AddJoinFragmentArgs
import com.bink.wallet.utils.navigateIfAdded
import kotlinx.android.synthetic.main.add_auth_fragment.*
import org.koin.androidx.viewmodel.ext.android.viewModel

class AddAuthFragment : BaseFragment<AddAuthViewModel, AddAuthFragmentBinding>() {

    override val layoutRes: Int
        get() = R.layout.add_auth_fragment

    private val args: AddJoinFragmentArgs by navArgs()

    override val viewModel: AddAuthViewModel by viewModel()

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val currentMembershipPlan = args.currentMembershipPlan

        binding.item = currentMembershipPlan

        binding.toolbar.setNavigationIcon(R.drawable.ic_back)
        binding.toolbar.setNavigationOnClickListener {
            activity?.onBackPressed()
        }

        binding.close.setOnClickListener {
            findNavController().navigateIfAdded(this, R.id.add_auth_to_home)
        }

        val addAuthFields: MutableList<Any>? = mutableListOf()

        currentMembershipPlan.account?.add_fields?.map { addAuthFields?.add(it) }
        currentMembershipPlan.account?.authorise_fields?.map { addAuthFields?.add(it) }

        val addAuthFieldsRequest = Account(ArrayList(), ArrayList())

        auth_add_fields.apply {
            layoutManager = GridLayoutManager(activity, 1)
            adapter = AddAuthAdapter(
                addAuthFields?.toList()!!,
                addAuthFieldsRequest
            )
        }

        add_card_button.setOnClickListener {
            viewModel.createMembershipCard(
                MembershipCardRequest(
                    addAuthFieldsRequest,
                    currentMembershipPlan.id
                )
            )
        }

        viewModel.membershipCardData.observe(this, Observer {
            findNavController().navigateIfAdded(this, R.id.add_auth_to_home)
        })
    }

}
