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
import com.bink.wallet.utils.displayModalPopup
import com.bink.wallet.utils.navigateIfAdded
import com.bink.wallet.utils.observeNonNull
import com.bink.wallet.utils.toolbar.FragmentToolbar
import com.bink.wallet.utils.verifyAvailableNetwork
import org.koin.androidx.viewmodel.ext.android.viewModel


class AddAuthFragment : BaseFragment<AddAuthViewModel, AddAuthFragmentBinding>() {
    override fun builder(): FragmentToolbar {
        return FragmentToolbar.Builder()
            .with(binding.toolbar).shouldDisplayBack(activity!!)
            .build()
    }

    companion object {
        const val BARCODE_TEXT = "Barcode"
    }

    override val layoutRes: Int
        get() = R.layout.add_auth_fragment

    private val args: AddAuthFragmentArgs by navArgs()

    override val viewModel: AddAuthViewModel by viewModel()

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val currentMembershipPlan = args.currentMembershipPlan
        val currentMembershipCard = args.membershipCard

        binding.item = currentMembershipPlan
        binding.descriptionAddAuth.text =
            getString(R.string.add_auth_description, currentMembershipPlan.account?.company_name)

        activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)

        binding.close.setOnClickListener {
            val imm =
                requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view?.windowToken, 0)
            findNavController().navigateIfAdded(this, R.id.add_auth_to_home)
        }

        val addAuthFields: MutableList<Any>? = mutableListOf()

        val addAuthBoolean: MutableList<Any>? = mutableListOf()

        if (currentMembershipCard != null) {
            if (currentMembershipPlan.feature_set?.has_points != null &&
                currentMembershipPlan.feature_set.has_points == true &&
                currentMembershipPlan.feature_set.transactions_available != null
            ) {
                binding.titleAddAuthText.text = getString(R.string.log_in_text)
                binding.addCardButton.text = getString(R.string.log_in_text)
                if (currentMembershipPlan.feature_set.transactions_available == true) {
                    binding.descriptionAddAuth.text = getString(
                        R.string.log_in_transaction_available,
                        currentMembershipPlan.account?.plan_name_card
                    )
                } else {
                    binding.descriptionAddAuth.text =
                        getString(
                            R.string.log_in_transaction_unavailable,
                            currentMembershipPlan.account?.plan_name_card
                        )
                }
            }
        } else {
            currentMembershipPlan.account?.add_fields?.map {
                if (it.type == 3) {
                    addAuthBoolean?.add(it)
                }
            }
        }

        currentMembershipPlan.account?.authorise_fields?.map {
            if (it.type == 3) {
                addAuthBoolean?.add(it)
            }
        }

        if (currentMembershipCard == null)
            if (currentMembershipPlan.feature_set?.has_points != null &&
                currentMembershipPlan.feature_set.has_points == true &&
                currentMembershipPlan.feature_set.transactions_available != null
            ) {
                currentMembershipPlan.account?.add_fields?.map {
                    if (it.type != 3 &&
                        !it.column.equals(BARCODE_TEXT)
                    ) {
                        addAuthFields?.add(it)
                    }
                }
            }

        currentMembershipPlan.account?.authorise_fields?.map {
            if (it.type != 3 &&
                !it.column.equals(BARCODE_TEXT)
            ) {
                addAuthFields?.add(it)
            }
        }

        addAuthBoolean?.map { addAuthFields?.add(it) }

        val addAuthFieldsRequest = Account(ArrayList(), ArrayList(), null)

        binding.authAddFields.apply {
            layoutManager = GridLayoutManager(activity, 1)
            adapter = AddAuthAdapter(
                addAuthFields?.toList()!!,
                addAuthFieldsRequest
            )
        }

        binding.addCardButton.setOnClickListener {
            if (viewModel.createCardError.value == null) {
                if (verifyAvailableNetwork(requireActivity())) {
                    viewModel.createMembershipCard(
                        MembershipCardRequest(
                            addAuthFieldsRequest,
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
                when (currentMembershipPlan.feature_set?.card_type) {
                    //TODO The condition is temporary removed for testing regarding to AB20-35(comment section)
//                    0, 1 -> {
//                        val directions =
//                            AddAuthFragmentDirections.addAuthToDetails(
//                                currentMembershipPlan, it
//                            )
//                        findNavController().navigateIfAdded(this, directions)
//                    }
                    0, 1, 2 -> {
                        if (it.membership_transactions != null && it.membership_transactions?.isEmpty()!!) {
                            val directions =
                                AddAuthFragmentDirections.addAuthToPllEmpty(
                                    currentMembershipPlan, it
                                )
                            findNavController().navigateIfAdded(this, directions)
                        }
                    }
                }
                binding.progressSpinner.visibility = View.GONE
                viewModel.createCardError.value = null
                binding.addCardButton.isEnabled = true
            })


        viewModel.createCardError.observeNonNull(this) {
            requireContext().displayModalPopup(
                getString(R.string.add_card_error_title),
                getString(R.string.add_card_error_message)
            )
            binding.progressSpinner.visibility = View.GONE
            viewModel.createCardError.value = null
            binding.addCardButton.isEnabled = true
        }
    }
}
