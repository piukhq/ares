package com.bink.wallet.scenes.add_auth_enrol

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.GridLayoutManager
import com.bink.wallet.BaseFragment
import com.bink.wallet.R
import com.bink.wallet.databinding.AddAuthFragmentBinding
import com.bink.wallet.model.request.membership_card.Account
import com.bink.wallet.model.request.membership_card.MembershipCardRequest
import com.bink.wallet.model.request.membership_card.PlanFieldsRequest
import com.bink.wallet.model.response.membership_plan.PlanFields
import com.bink.wallet.utils.*
import com.bink.wallet.utils.enums.*
import com.bink.wallet.utils.toolbar.FragmentToolbar
import org.koin.androidx.viewmodel.ext.android.viewModel


class SignUpFragment : BaseFragment<SignUpViewModel, AddAuthFragmentBinding>() {
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

    private val planFieldsList: MutableList<Pair<PlanFields, PlanFieldsRequest>>? =
        mutableListOf()

    private val planBooleanFieldsList: MutableList<Pair<PlanFields, PlanFieldsRequest>>? =
        mutableListOf()

    private fun addFieldToList(planField: PlanFields) {

        val pairPlanField = Pair(
            planField, PlanFieldsRequest(
                planField.column, ""
            )
        )

        if (planField.type == FieldType.BOOLEAN.type) {
            planBooleanFieldsList?.add(
                pairPlanField
            )
        } else {
            if (!planField.column.equals(BARCODE_TEXT))
                planFieldsList?.add(
                    pairPlanField
                )
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel.currentMembershipPlan.value = args.currentMembershipPlan
        viewModel.currentMembershipCard.value = args.membershipCard
        val signUpFormType = args.signUpFormType

        binding.item = viewModel.currentMembershipPlan.value

        if (viewModel.currentMembershipPlan.value != null) {
            binding.descriptionAddAuth.text =
                getString(
                    R.string.enrol_description,
                    viewModel.currentMembershipPlan.value!!.account?.company_name
                )

            if (!viewModel.currentMembershipPlan.value!!.account?.registration_fields?.isNullOrEmpty()!!) {
                with(binding.noAccountText) {
                    visibility = View.VISIBLE
                }
            }
        }
        binding.toolbar.setNavigationOnClickListener {
            windowFullscreenHandler.toNormalScreen()
            activity?.onBackPressed()
        }
        binding.close.setOnClickListener {
            view?.hideKeyboard()
            windowFullscreenHandler.toNormalScreen()
            findNavController().navigateIfAdded(this, R.id.global_to_home)
        }

        when (signUpFormType) {
            SignUpFormType.ADD_AUTH -> {
                if (viewModel.currentMembershipCard.value != null) {
                    if (viewModel.currentMembershipPlan.value!!.feature_set?.has_points != null &&
                        viewModel.currentMembershipPlan.value!!.feature_set?.has_points == true &&
                        viewModel.currentMembershipPlan.value!!.feature_set?.transactions_available != null
                    ) {
                        if (viewModel.currentMembershipPlan.value!!.feature_set?.transactions_available == true) {
                            binding.descriptionAddAuth.text = getString(
                                R.string.log_in_transaction_available,
                                viewModel.currentMembershipPlan.value!!.account?.plan_name_card
                            )
                        } else {
                            binding.descriptionAddAuth.text =
                                getString(
                                    R.string.log_in_transaction_unavailable,
                                    viewModel.currentMembershipPlan.value!!.account?.plan_name_card
                                )
                        }
                    }

                    if (MembershipPlanUtils.getAccountStatus(
                            viewModel.currentMembershipPlan.value!!,
                            viewModel.currentMembershipCard.value!!
                        ) == LoginStatus.STATUS_LOGIN_FAILED
                    ) {
                        binding.descriptionAddAuth.text = getString(
                            R.string.log_in_transaction_available,
                            viewModel.currentMembershipPlan.value!!.account?.plan_name_card
                        )
                    }
                } else {
                    viewModel.currentMembershipPlan.value!!.account?.add_fields?.map {
                        it.typeOfField = TypeOfField.ADD
                        addFieldToList(it)
                    }
                }

                viewModel.currentMembershipPlan.value!!.account?.authorise_fields?.map {
                    it.typeOfField = TypeOfField.AUTH
                    addFieldToList(it)
                }

            }
            SignUpFormType.ENROL -> {
                viewModel.currentMembershipPlan.value!!.account?.enrol_fields?.map {
                    it.typeOfField = TypeOfField.ENROL
                    addFieldToList(it)
                }
            }
            SignUpFormType.GHOST -> {
                viewModel.currentMembershipPlan.value!!.account?.add_fields?.map {
                    it.typeOfField = TypeOfField.ADD
                    addFieldToList(it)
                }

                viewModel.currentMembershipPlan.value!!.account?.registration_fields?.map {
                    it.typeOfField = TypeOfField.REGISTRATION
                    addFieldToList(it)
                }

                with(binding.noAccountText) {
                    visibility = View.INVISIBLE
                }

            }
        }

        binding.noAccountText.setOnClickListener {
            if (viewModel.currentMembershipPlan.value != null) {
                val action = SignUpFragmentDirections.toGhost(
                    SignUpFormType.GHOST,
                    viewModel.currentMembershipPlan.value!!,
                    null
                )
                findNavController().navigateIfAdded(this, action)
            }
        }


        planBooleanFieldsList?.map { planFieldsList?.add(it) }

        val addRegisterFieldsRequest = Account()

        planFieldsList?.map {
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
                planFieldsList?.toList()!!
            )
        }

        binding.addCardButton.setOnClickListener {
            if (viewModel.createCardError.value == null) {
                if (verifyAvailableNetwork(requireActivity())) {

                    when (signUpFormType) {
                        SignUpFormType.ADD_AUTH -> {
                            val currentRequest = MembershipCardRequest(
                                addRegisterFieldsRequest,
                                viewModel.currentMembershipPlan.value!!.id
                            )

                            if (viewModel.currentMembershipCard.value != null &&
                                MembershipPlanUtils.getAccountStatus(
                                    viewModel.currentMembershipPlan.value!!,
                                    viewModel.currentMembershipCard.value!!
                                ) == LoginStatus.STATUS_LOGIN_FAILED
                            ) {
                                viewModel.updateMembershipCard(
                                    viewModel.currentMembershipCard.value!!,
                                    currentRequest
                                )
                            } else {
                                viewModel.createMembershipCard(
                                    currentRequest
                                )
                            }
                        }
                        SignUpFormType.GHOST -> {
                            val currentRequest = MembershipCardRequest(
                                Account(
                                    addRegisterFieldsRequest.add_fields,
                                    null,
                                    null,
                                    null
                                ),
                                viewModel.currentMembershipPlan.value!!.id
                            )

                            viewModel.createMembershipCard(
                                currentRequest
                            )
                        }

                        SignUpFormType.ENROL -> {
                            viewModel.createMembershipCard(
                                MembershipCardRequest(
                                    addRegisterFieldsRequest,
                                    viewModel.currentMembershipPlan.value!!.id
                                )
                            )
                        }
                    }

                } else {
                    showNoInternetConnectionDialog()
                }
                binding.addCardButton.isEnabled = false
                binding.progressSpinner.visibility = View.VISIBLE
            }
        }

        if (viewModel.newMembershipCard.hasActiveObservers())
            viewModel.newMembershipCard.removeObservers(this)
        else
            viewModel
                .newMembershipCard.observeNonNull(this) { membershipCard ->

                if (signUpFormType == SignUpFormType.GHOST) {
                    val currentRequest = MembershipCardRequest(
                        Account(null, null, null, addRegisterFieldsRequest.registration_fields),
                        viewModel.currentMembershipPlan.value!!.id
                    )

                    viewModel.ghostMembershipCard(
                        membershipCard,
                        currentRequest
                    )
                }

                when (viewModel.currentMembershipPlan.value!!.feature_set?.card_type) {

                    CardType.VIEW.type, CardType.STORE.type -> {
                        val directions =
                            SignUpFragmentDirections.signUpToDetails(
                                viewModel.currentMembershipPlan.value!!, membershipCard
                            )
                        findNavController().navigateIfAdded(this, directions)
                    }
                    CardType.PLL.type -> {
                        if (membershipCard.membership_transactions != null && membershipCard.membership_transactions?.isEmpty()!!) {
                            val directions = SignUpFragmentDirections.signUpToPllEmpty(
                                viewModel.currentMembershipPlan.value!!,
                                membershipCard
                            )

                            findNavController().navigateIfAdded(this, directions)
                        }
                    }
                }
                hideLoadingViews()
            }


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
