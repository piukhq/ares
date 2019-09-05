package com.bink.wallet.scenes.add_auth

import android.app.AlertDialog
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
import com.bink.wallet.utils.displayModalPopup
import com.bink.wallet.utils.navigateIfAdded
import com.bink.wallet.utils.observeNonNull
import com.bink.wallet.utils.verifyAvailableNetwork
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

        val addAuthBoolean: MutableList<Any>? = mutableListOf()

        currentMembershipPlan.account?.add_fields?.map {
            if (it.type == 3) addAuthBoolean?.add(it)

        }
        currentMembershipPlan.account?.authorise_fields?.map {
            if (it.type == 3) addAuthBoolean?.add(it)
        }

        currentMembershipPlan.account?.add_fields?.map { if (it.type != 3) addAuthFields?.add(it) }
        currentMembershipPlan.account?.authorise_fields?.map {
            if (it.type != 3) addAuthFields?.add(it)
        }

        addAuthBoolean?.map { addAuthFields?.add(it) }

        val addAuthFieldsRequest = Account(ArrayList(), ArrayList())

        binding.authAddFields.apply {
            layoutManager = GridLayoutManager(activity, 1)
            adapter = AddAuthAdapter(
                addAuthFields?.toList()!!,
                addAuthFieldsRequest
            )
        }

        binding.addCardButton.setOnClickListener {
            if (verifyAvailableNetwork(activity!!)) {
                viewModel.createMembershipCard(
                    MembershipCardRequest(
                        addAuthFieldsRequest,
                        currentMembershipPlan.id
                    )
                )
            } else {
                showNoInternetConnectionDialog()
            }

        if (viewModel.membershipCardData.hasActiveObservers())
            viewModel.membershipCardData.removeObservers(this)
        else
            viewModel.membershipCardData.observe(this, Observer {
                when (currentMembershipPlan.feature_set?.card_type) {
//                    0, 1 -> {
//                        val directions =
//                            AddAuthFragmentDirections.addAuthToDetails(
//                                currentMembershipPlan, it
//                            )
//                        findNavController().navigateIfAdded(this, directions)
//                    }
                    0, 1, 2 -> {
                        if (it.membership_transactions.isEmpty()) {
                            val directions =
                                AddAuthFragmentDirections.addAuthToPllEmpty(
                                    currentMembershipPlan, it
                                )
                            findNavController().navigateIfAdded(this, directions)
                        }
                    }
                }
            })
        viewModel.createCardError.observeNonNull(this){
            requireContext().displayModalPopup(getString(R.string.add_card_error_title), getString(R.string.add_card_error_message))
        }
    }

    private fun showNoInternetConnectionDialog() {
        AlertDialog.Builder(context).setMessage(R.string.no_internet_connection_dialog_message)
            .setNeutralButton(R.string.ok) { _, _ -> }
            .create().show()
    }
}
