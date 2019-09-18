package com.bink.wallet.scenes.add_auth_enrol

import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.GridLayoutManager
import com.bink.wallet.BaseFragment
import com.bink.wallet.R
import com.bink.wallet.databinding.AddAuthFragmentBinding
import com.bink.wallet.model.request.membership_card.Account
import com.bink.wallet.model.request.membership_card.MembershipCardRequest
import com.bink.wallet.utils.*
import com.bink.wallet.utils.toolbar.FragmentToolbar
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
        binding.descriptionAddAuth.text =
            getString(R.string.enrol_description, currentMembershipPlan.account?.company_name)
        activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)

        val enrolFields: MutableList<Any>? = mutableListOf()

        binding.close.setOnClickListener {
            view?.hideKeyboard()

            val imm =
                requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view?.windowToken, 0)
            findNavController().navigateIfAdded(this, R.id.enrol_to_home)
        }

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

        if (viewModel.membershipCardData.hasActiveObservers())
            viewModel.membershipCardData.removeObservers(this)
        else
            viewModel.membershipCardData.observe(this, Observer {
                val directions =
                    EnrolFragmentDirections.enrolToDetails(
                        currentMembershipPlan, it
                    )
                findNavController().navigateIfAdded(this, directions)
                hideLoadingViews()
            })


        viewModel.createCardError.observeNonNull(this) {
            requireContext().displayModalPopup(
                getString(R.string.add_card_error_title),
                getString(R.string.add_card_error_message)
            )
            hideLoadingViews()
        }
    }

    private fun hideLoadingViews() {
        binding.progressSpinner.visibility = View.GONE
        viewModel.createCardError.value = null
        binding.addCardButton.isEnabled = true
    }
}