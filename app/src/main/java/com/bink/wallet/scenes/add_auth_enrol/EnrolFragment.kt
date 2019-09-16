package com.bink.wallet.scenes.add_auth_enrol

import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.GridLayoutManager
import com.bink.wallet.BaseFragment
import com.bink.wallet.R
import com.bink.wallet.databinding.AddAuthFragmentBinding
import com.bink.wallet.model.request.membership_card.Account
import com.bink.wallet.model.request.membership_card.MembershipCardRequest
import com.bink.wallet.utils.toolbar.FragmentToolbar
import com.bink.wallet.utils.verifyAvailableNetwork
import org.koin.androidx.viewmodel.ext.android.viewModel

class EnrolFragment : BaseFragment<AddAuthViewModel, AddAuthFragmentBinding>() {

    override fun builder(): FragmentToolbar {
        return FragmentToolbar.Builder()
            .with(binding.toolbar).shouldDisplayBack(activity!!)
            .build()
    }

    private val args: EnrolFragmentArgs by navArgs()

    override val viewModel: AddAuthViewModel by viewModel()

    override val layoutRes: Int
        get() = R.layout.add_auth_fragment

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val currentMembershipPlan = args.currentMembershipPlan

        binding.item = currentMembershipPlan
        binding.titleAddAuthText.text = getString(R.string.sign_up_enrol)
        binding.descriptionAddAuth.text = getString(R.string.enrol_description)
        activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)

        val enrolFields: MutableList<Any>? = mutableListOf()

        currentMembershipPlan.account?.enrol_fields?.map {
            if (it.type != 3 &&
                !it.column.equals(AddAuthFragment.BARCODE_TEXT)
            ) {
                enrolFields?.add(it)
            }
        }

        currentMembershipPlan.account?.enrol_fields?.map {
            if (it.type == 3 &&
                !it.column.equals(AddAuthFragment.BARCODE_TEXT)
            ) {
                enrolFields?.add(it)
            }
        }

        val enrolFieldsRequest = Account(null, null, ArrayList())

        binding.authAddFields.apply {
            layoutManager = GridLayoutManager(activity, 1)
            adapter = AddAuthAdapter(
                enrolFields?.toList()!!,
                enrolFieldsRequest
            )
        }

        binding.addCardButton.setOnClickListener {
            if (viewModel.createCardError.value == null) {
                if (verifyAvailableNetwork(requireActivity())) {
                    viewModel.createMembershipCard(
                        MembershipCardRequest(
                            enrolFieldsRequest,
                            currentMembershipPlan.id
                        )
                    )
                } else {
                    showNoInternetConnectionDialog()
                }
                binding.addCardButton.isEnabled = false
                binding.progressSpinner.visibility = View.VISIBLE
            }
        }
    }
}