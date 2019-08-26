package com.bink.wallet.scenes.add_auth

import android.os.Bundle
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.GridLayoutManager
import com.bink.wallet.BaseFragment
import com.bink.wallet.R
import com.bink.wallet.databinding.AddAuthFragmentBinding
import com.bink.wallet.scenes.add_join.AddJoinFragmentArgs
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

        val addAuthFields : MutableList<Any>? = mutableListOf()

        currentMembershipPlan.account?.add_fields?.map { addAuthFields?.add(it) }
        currentMembershipPlan.account?.authorise_fields?.map { addAuthFields?.add(it) }

        auth_add_fields.apply {
            layoutManager = GridLayoutManager(activity, 1)
            adapter = AddAuthAdapter(addAuthFields?.toList()!!, itemClickListener = {  })
        }
    }

}
