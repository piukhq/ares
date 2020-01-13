package com.bink.wallet.scenes.add_auth_enrol

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.GridLayoutManager
import com.bink.wallet.BaseFragment
import com.bink.wallet.R
import com.bink.wallet.data.SharedPreferenceManager
import com.bink.wallet.databinding.AddAuthFragmentBinding
import com.bink.wallet.modal.generic.GenericModalParameters
import com.bink.wallet.model.request.membership_card.Account
import com.bink.wallet.model.request.membership_card.MembershipCardRequest
import com.bink.wallet.model.request.membership_card.PlanFieldsRequest
import com.bink.wallet.model.response.membership_plan.PlanDocuments
import com.bink.wallet.model.response.membership_plan.PlanFields
import com.bink.wallet.utils.*
import com.bink.wallet.utils.enums.*
import com.bink.wallet.utils.toolbar.FragmentToolbar
import kotlinx.coroutines.runBlocking
import org.koin.androidx.viewmodel.ext.android.viewModel


class AddAuthFragment : BaseFragment<AddAuthViewModel, AddAuthFragmentBinding>() {
    override fun builder(): FragmentToolbar {
        return FragmentToolbar.Builder()
            .build()
    }

    companion object {
        const val BARCODE_TEXT = "Barcode"
    }

    override val layoutRes: Int
        get() = R.layout.add_auth_fragment

    private val args: AddAuthFragmentArgs by navArgs()

    override val viewModel: AddAuthViewModel by viewModel()

    override fun onResume() {
        super.onResume()
        windowFullscreenHandler.toFullscreen()
    }

    private var isPaymentWalletEmpty: Boolean? = null

    private val planFieldsList: MutableList<Pair<Any, PlanFieldsRequest>>? =
        mutableListOf()

    private val planBooleanFieldsList: MutableList<Pair<Any, PlanFieldsRequest>>? =
        mutableListOf()

    private fun addFieldToList(planField: PlanFields) {
        viewModel.currentMembershipPlan.value?.has_vouchers?.let {
            if (it) {
                if (planField.common_name == SignUpFieldTypes.EMAIL.common_name) {
                    val email =
                        LocalStoreUtils.getAppSharedPref(LocalStoreUtils.KEY_EMAIL)
                            ?.let {
                                it
                            }
                    with (pairPlanField) {
                        second.disabled = true
                        second.value = email
                    }
                }
            }
        }
        if (planField is PlanFields) {
            val pairPlanField = Pair(
                planField, PlanFieldsRequest(
                    planField.column, ""
                )
            )

            if (planField.type == FieldType.BOOLEAN_OPTIONAL.type) {
                planBooleanFieldsList?.add(
                    pairPlanField
                )
            } else if (!planField.column.equals(BARCODE_TEXT)) {
                planFieldsList?.add(
                    pairPlanField
                )
            }
        }

        if (planField is PlanDocuments) {
            planBooleanFieldsList?.add(
                Pair(
                    planField, PlanFieldsRequest(
                        planField.name, ""
                    )
                )
            )
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        with (viewModel) {
            currentMembershipPlan.value = args.currentMembershipPlan
            currentMembershipCard.value = args.membershipCard
        }
        planFieldsList?.clear()
        planBooleanFieldsList?.clear()
        val signUpFormType = args.signUpFormType
        SharedPreferenceManager.isLoyaltySelected = true

        binding.item = viewModel.currentMembershipPlan.value

        viewModel.currentMembershipPlan.value?.let {
            binding.descriptionAddAuth.text =
                getString(
                    R.string.enrol_description,
                    it.account?.company_name
                )
            binding.noAccountText.visibility = View.VISIBLE
        }

        binding.close.setOnClickListener {
            windowFullscreenHandler.toNormalScreen()
            requireActivity().onBackPressed()
        }
        binding.cancel.setOnClickListener {
            view?.hideKeyboard()
            windowFullscreenHandler.toNormalScreen()
            findNavController().navigateIfAdded(this, R.id.global_to_home)
        }

        binding.close.setOnClickListener {
            view?.hideKeyboard()
            windowFullscreenHandler.toNormalScreen()
            findNavController().popBackStack()
        }

        runBlocking {
            viewModel.getPaymentCards()
        }

        viewModel.paymentCards.observeNonNull(this) { paymentCards ->
            isPaymentWalletEmpty = paymentCards.isNullOrEmpty()
        }

        binding.addJoinReward.setOnClickListener {
            viewModel.currentMembershipPlan.value?.account?.plan_description?.let { planDescription ->
                findNavController().navigateIfAdded(
                    this,
                    AddAuthFragmentDirections.signUpToBrandHeader(
                        GenericModalParameters(
                            R.drawable.ic_close,
                            true,
                            viewModel.currentMembershipPlan.value?.account?.plan_name
                                ?: getString(R.string.plan_description),
                            planDescription
                        )
                    )
                )
            }
        }

        when (signUpFormType) {
            SignUpFormType.ADD_AUTH -> {
                with (binding) {
                    titleAddAuthText.text = getString(R.string.log_in_text)
                    addCardButton.text = getString(R.string.log_in_text)
                }
                with(viewModel) {
                    if (currentMembershipCard.value != null) {
                        if (currentMembershipPlan.value?.feature_set?.has_points != null &&
                            currentMembershipPlan.value?.feature_set?.has_points == true &&
                            currentMembershipPlan.value?.feature_set?.transactions_available != null
                        ) {
                            if (currentMembershipPlan.value?.feature_set?.transactions_available == true) {
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
                                this,
                                viewModel.currentMembershipCard.value!!
                            ) == LoginStatus.STATUS_LOGIN_FAILED
                        ) {
                            binding.descriptionAddAuth.text = getString(
                                R.string.log_in_transaction_available,
                                viewModel.currentMembershipPlan.value!!.account?.plan_name_card
                            )
                        }
                    } else {
                        account?.add_fields?.map {
                            it.typeOfField = TypeOfField.ADD
                            addFieldToList(it)
                        }
                    }

                    account?.let {
                        account.authorise_fields?.map {
                            it.typeOfField = TypeOfField.AUTH
                            addFieldToList(it)
                        }
                        account.plan_documents?.map {
                            if (it.display?.contains(SignUpFormType.ADD_AUTH.type)!!) {
                                addFieldToList(it)
                            }
                        }
                    }

                }
            }
            SignUpFormType.ENROL -> {
                with(binding) {
                    titleAddAuthText.text = getString(R.string.sign_up_enrol)
                    addCardButton.text = getString(R.string.sign_up_text)
                    descriptionAddAuth.text = getString(
                        R.string.enrol_description,
                        viewModel.currentMembershipPlan.value?.account?.company_name
                    )
                    viewModel.currentMembershipPlan.value!!.account?.enrol_fields?.map {
                        it.typeOfField = TypeOfField.ENROL
                        addFieldToList(it)
                    }
                    noAccountText.visibility = View.GONE
                }

                viewModel.currentMembershipPlan.value!!.account?.plan_documents?.map {
                    if (it.display?.contains(SignUpFormType.ENROL.type)!!) {
                        addFieldToList(it)
                    }
                }

            }
            SignUpFormType.GHOST -> {
                with(binding) {
                    titleAddAuthText.text = getString(R.string.register_ghost_card_title)
                    addCardButton.text = getString(R.string.register_ghost_card_button)
                }
                viewModel.currentMembershipPlan.value!!.account?.add_fields?.map {
                    it.typeOfField = TypeOfField.ADD
                    addFieldToList(it)
                }

                viewModel.currentMembershipPlan.value!!.account?.registration_fields?.map {
                    it.typeOfField = TypeOfField.REGISTRATION
                    addFieldToList(it)
                }

                viewModel.currentMembershipPlan.value!!.account?.plan_documents?.map {
                    if (it.display?.contains(SignUpFormType.GHOST.type)!!) {
                        addFieldToList(it)
                    }
                }

                binding.noAccountText.visibility = View.GONE
            }
        }

        binding.noAccountText.setOnClickListener {
            if (viewModel.currentMembershipPlan.value?.feature_set?.linking_support?.contains(
                    TypeOfField.REGISTRATION.name
                )!!
            ) {
                if (viewModel.currentMembershipPlan.value != null) {
                    val action = AddAuthFragmentDirections.toGhost(
                        SignUpFormType.GHOST,
                        viewModel.currentMembershipPlan.value!!,
                        null
                    )
                    findNavController().navigateIfAdded(this, action)
                }
            } else {
                val action = AddAuthFragmentDirections.signUpToGhostRegistrationUnavailable(
                    GenericModalParameters(
                        R.drawable.ic_close,
                        true,
                        getString(R.string.title_ghost_card_not_available),
                        getString(R.string.description_ghost_card_not_available)
                    )
                )
                findNavController().navigateIfAdded(this, action)
            }
        }

        planBooleanFieldsList?.map { planFieldsList?.add(it) }

        val addRegisterFieldsRequest = Account()

        planFieldsList?.map {
            if (it.first is PlanFields) {
                when ((it.first as PlanFields).typeOfField) {
                    TypeOfField.ADD -> addRegisterFieldsRequest.add_fields?.add(it.second)
                    TypeOfField.AUTH -> addRegisterFieldsRequest.authorise_fields?.add(it.second)
                    TypeOfField.ENROL -> addRegisterFieldsRequest.enrol_fields?.add(it.second)
                    else -> addRegisterFieldsRequest.registration_fields?.add(it.second)
                }
            } else
                addRegisterFieldsRequest.plan_documents?.add(it.second)
        }

        binding.authAddFields.apply {
            layoutManager = GridLayoutManager(activity, 1)
            adapter = AddAuthAdapter(
                planFieldsList?.toList()!!,
                buttonRefresh = {
                    addRegisterFieldsRequest.plan_documents?.map {
                        if (it.value != true.toString()) {
                            binding.addCardButton.isEnabled = false
                            return@AddAuthAdapter
                        }
                    }

                    planFieldsList.map {
                        if (it.first is PlanFields) {
                            if (!UtilFunctions.isValidField(
                                    (it.first as PlanFields).validation,
                                    it.second.value
                                )
                            ) {
                                binding.addCardButton.isEnabled = false
                                return@AddAuthAdapter
                            }
                        }
                    }

                    binding.addCardButton.isEnabled = true

                }
            )
        }

        binding.addCardButton.isEnabled = false

        binding.addCardButton.setOnClickListener {
            if (viewModel.createCardError.value == null) {
                if (verifyAvailableNetwork(requireActivity())) {

                    addRegisterFieldsRequest.plan_documents?.map {
                        if (it.value != "true") {
                            requireContext().displayModalPopup(
                                EMPTY_STRING,
                                getString(R.string.required_fields)
                            )
                            return@setOnClickListener
                        }
                    }

                    planFieldsList?.map {
                        if (it.first is PlanFields) {
                            if (!UtilFunctions.isValidField(
                                    (it.first as PlanFields).validation,
                                    it.second.value
                                )
                            ) {
                                context?.displayModalPopup(
                                    null,
                                    getString(R.string.all_fields_must_be_valid)
                                )
                                return@setOnClickListener
                            }
                        }
                    }

                    when (signUpFormType) {
                        SignUpFormType.ADD_AUTH -> {
                            with (viewModel) {
                                val currentRequest = MembershipCardRequest(
                                    addRegisterFieldsRequest,
                                    currentMembershipPlan.value!!.id
                                )

                                if (currentMembershipCard.value != null &&
                                    MembershipPlanUtils.getAccountStatus(
                                        currentMembershipPlan.value!!,
                                        currentMembershipCard.value!!
                                    ) == LoginStatus.STATUS_LOGIN_FAILED
                                ) {
                                    updateMembershipCard(
                                        currentMembershipCard.value!!,
                                        currentRequest
                                    )
                                } else {
                                    createMembershipCard(
                                        currentRequest
                                    )
                                }
                            }
                        }
                        SignUpFormType.GHOST -> {
                            if (addRegisterFieldsRequest.add_fields.isNullOrEmpty()) {
                                requireContext().displayModalPopup(
                                    null,
                                    getString(R.string.cannot_complete_registration)
                                )
                                return@setOnClickListener
                            }

                            val currentRequest = MembershipCardRequest(
                                Account(
                                    addRegisterFieldsRequest.add_fields,
                                    null,
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
                    binding.addCardButton.isEnabled = false
                    binding.progressSpinner.visibility = View.VISIBLE
                } else {
                    showNoInternetConnectionDialog()
                    binding.progressSpinner.visibility = View.GONE
                }
            }
        }

        viewModel.newMembershipCard.observeNonNull(this) { membershipCard ->
            if (viewModel.newMembershipCard.hasActiveObservers())
                viewModel.newMembershipCard.removeObservers(this)
            if (signUpFormType == SignUpFormType.GHOST) {
                val currentRequest = MembershipCardRequest(
                    Account(
                        null,
                        null,
                        null,
                        addRegisterFieldsRequest.registration_fields,
                        null
                    ),
                    viewModel.currentMembershipPlan.value!!.id
                )
                viewModel.ghostMembershipCard(
                    membershipCard,
                    currentRequest
                )
            }
            if (signUpFormType == SignUpFormType.GHOST) {
                val currentRequest = MembershipCardRequest(
                    Account(
                        null,
                        null,
                        null,
                        addRegisterFieldsRequest.registration_fields,
                        null
                    ),
                    viewModel.currentMembershipPlan.value!!.id
                )
                viewModel.ghostMembershipCard(
                    membershipCard,
                    currentRequest
                )
            }
            viewModel.currentMembershipPlan.value?.let {
                when (it.feature_set?.card_type) {
                    CardType.VIEW.type,
                    CardType.STORE.type -> {
                        val directions =
                            AddAuthFragmentDirections.signUpToDetails(
                                viewModel.currentMembershipPlan.value!!,
                                membershipCard
                            )
                        findNavController().navigateIfAdded(this, directions)
                    }
                    CardType.PLL.type -> {
                        if (signUpFormType == SignUpFormType.GHOST) {
                            if (membershipCard.membership_transactions.isNullOrEmpty()) {
                                val directions = AddAuthFragmentDirections.signUpToPllEmpty(
                                    viewModel.currentMembershipPlan.value!!,
                                    membershipCard
                                )
                                findNavController().navigateIfAdded(this, directions)
                            }
                        } else {
                            if (viewModel.currentMembershipPlan.value != null) {
                                val directions =
                                    if (viewModel.paymentCards.value.isNullOrEmpty()) {
                                        AddAuthFragmentDirections.signUpToPllEmpty(
                                            viewModel.currentMembershipPlan.value!!,
                                            membershipCard
                                        )
                                    } else {
                                        AddAuthFragmentDirections.signUpToPll(
                                            membershipCard,
                                            viewModel.currentMembershipPlan.value!!,
                                            true
                                        )
                                    }
                                findNavController().navigateIfAdded(this, directions)
                            }
                        }
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
        with (binding) {
            progressSpinner.visibility = View.GONE
            viewModel.createCardError.value = null
            addCardButton.isEnabled = true
        }
    }
}
