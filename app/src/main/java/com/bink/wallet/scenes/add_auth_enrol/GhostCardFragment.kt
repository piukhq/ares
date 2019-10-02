package com.bink.wallet.scenes.add_auth_enrol

import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.GridLayoutManager
import com.bink.wallet.BaseFragment
import com.bink.wallet.R
import com.bink.wallet.databinding.AddAuthFragmentBinding
import com.bink.wallet.model.request.membership_card.Account
import com.bink.wallet.model.request.membership_card.MembershipCardRequest
import com.bink.wallet.model.response.membership_plan.PlanFields
import com.bink.wallet.utils.*
import com.bink.wallet.utils.enums.LoginStatus
import com.bink.wallet.utils.enums.TypeOfField
import com.bink.wallet.utils.toolbar.FragmentToolbar
import org.koin.androidx.viewmodel.ext.android.viewModel


class GhostCardFragment : BaseFragment<SignUpViewModel, AddAuthFragmentBinding>() {
    override fun builder(): FragmentToolbar {
        return FragmentToolbar.Builder()
            .with(binding.toolbar).shouldDisplayBack(requireActivity())
            .build()
    }

    companion object {
        const val BARCODE_TEXT = "Barcode"
    }

    override val layoutRes: Int
        get() = R.layout.add_auth_fragment

    private val args: SignUpFragmentArgs by navArgs()

    override val viewModel: SignUpViewModel by viewModel()

    override fun onResume() {
        super.onResume()
        windowFullscreenHandler.toFullscreen()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val currentMembershipPlan = args.currentMembershipPlan
        val currentMembershipCard = args.membershipCard

        binding.item = currentMembershipPlan
        binding.descriptionAddAuth.text =
            getString(R.string.add_auth_description, currentMembershipPlan.account?.company_name)

        binding.toolbar.setNavigationOnClickListener {
            windowFullscreenHandler.toNormalScreen()
            activity?.onBackPressed()
        }
        binding.close.setOnClickListener {
            view?.hideKeyboard()
            windowFullscreenHandler.toNormalScreen()
            findNavController().navigateIfAdded(this, R.id.global_to_home)
        }

        val addAuthFields: MutableList<Pair<PlanFields, com.bink.wallet.model.request.membership_card.PlanFields>>? =
            mutableListOf()

        val addAuthBoolean: MutableList<Pair<PlanFields, com.bink.wallet.model.request.membership_card.PlanFields>>? =
            mutableListOf()

        if (currentMembershipCard != null) {
            if (currentMembershipPlan.feature_set?.has_points != null &&
                currentMembershipPlan.feature_set.has_points == true &&
                currentMembershipPlan.feature_set.transactions_available != null
            ) {
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

            if (MembershipPlanUtils.getAccountStatus(
                    currentMembershipPlan,
                    currentMembershipCard
                ) == LoginStatus.STATUS_LOGIN_FAILED
            ) {
                binding.descriptionAddAuth.text = getString(
                    R.string.log_in_transaction_available,
                    currentMembershipPlan.account?.plan_name_card
                )
            }
        } else {
            currentMembershipPlan.account?.add_fields?.map {
                it.typeOfField = TypeOfField.ADD
                if (it.type == 3) {
                    addAuthBoolean?.add(
                        Pair(
                            it, com.bink.wallet.model.request.membership_card.PlanFields(
                                it.column, ""
                            )
                        )
                    )
                }
            }
        }

        currentMembershipPlan.account?.registration_fields?.map {
            it.typeOfField = TypeOfField.REGISTRATION
            if (it.type == 3) {
                addAuthBoolean?.add(
                    Pair(
                        it, com.bink.wallet.model.request.membership_card.PlanFields(
                            it.column, ""
                        )
                    )
                )
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
                        addAuthFields?.add(
                            Pair(
                                it, com.bink.wallet.model.request.membership_card.PlanFields(
                                    it.column, ""
                                )
                            )
                        )
                    }
                }
            }

        currentMembershipPlan.account?.registration_fields?.map {
            if (it.type != 3 &&
                !it.column.equals(BARCODE_TEXT)
            ) {
                addAuthFields?.add(
                    Pair(
                        it, com.bink.wallet.model.request.membership_card.PlanFields(
                            it.column, ""
                        )
                    )
                )
            }
        }

        addAuthBoolean?.map { addAuthFields?.add(it) }

        val addRegisterFieldsRequest = Account()

        addAuthBoolean?.map {

            when (it.first.typeOfField) {
                TypeOfField.ADD -> addRegisterFieldsRequest.add_fields?.add(it.second)
                TypeOfField.AUTH -> addRegisterFieldsRequest.authorise_fields?.add(it.second)
                TypeOfField.ENROL -> addRegisterFieldsRequest.enrol_fields?.add(it.second)
                else -> addRegisterFieldsRequest.registration_fields?.add(it.second)
            }

        }

        binding.authAddFields.apply {
            layoutManager = GridLayoutManager(activity, 1)
            adapter = SignUpAdapter(
                addAuthFields?.toList()!!
            )
        }

        binding.addCardButton.setOnClickListener {
            if (viewModel.createCardError.value == null) {
                if (verifyAvailableNetwork(requireActivity())) {

                    val currentRequest = MembershipCardRequest(
                        addRegisterFieldsRequest,
                        currentMembershipPlan.id
                    )

                    if (currentMembershipCard != null &&
                        MembershipPlanUtils.getAccountStatus(
                            currentMembershipPlan,
                            currentMembershipCard
                        ) == LoginStatus.STATUS_LOGIN_FAILED
                    ) {
                        viewModel.createMembershipCard(
                            currentRequest
                        )
                    } else {
                        viewModel.createMembershipCard(
                            currentRequest
                        )
                    }
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

                val currentRequest = MembershipCardRequest(
                    Account(
                        null,
                        null,
                        null,
                        addRegisterFieldsRequest.registration_fields
                    ),
                    currentMembershipPlan.id
                )

                viewModel.ghostMembershipCard(
                    it,
                    currentRequest
                )

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
                                SignUpFragmentDirections.addAuthToPllEmpty(
                                    currentMembershipPlan, it
                                )
                            findNavController().navigateIfAdded(this, directions)
                        }
                    }
                }
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
