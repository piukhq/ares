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
import com.bink.wallet.model.response.membership_card.MembershipCard
import com.bink.wallet.model.response.membership_plan.PlanDocuments
import com.bink.wallet.model.response.membership_plan.PlanFields
import com.bink.wallet.utils.*
import com.bink.wallet.utils.UtilFunctions.isNetworkAvailable
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

    private val planFieldsList: MutableList<Pair<Any, PlanFieldsRequest>> =
        mutableListOf()

    private val planBooleanFieldsList: MutableList<Pair<Any, PlanFieldsRequest>> =
        mutableListOf()

    private fun addFieldToList(planField: Any) {
        if (planField is PlanFields) {
            val pairPlanField = Pair(
                planField, PlanFieldsRequest(
                    planField.column, EMPTY_STRING
                )
            )
            if (planField.type == FieldType.BOOLEAN_OPTIONAL.type) {
                planBooleanFieldsList.add(
                    pairPlanField
                )
            } else if (!planField.column.equals(BARCODE_TEXT))
                planFieldsList.add(
                    pairPlanField
                )
        }
        if (planField is PlanDocuments) {
            planBooleanFieldsList.add(
                Pair(
                    planField, PlanFieldsRequest(
                        planField.name, EMPTY_STRING
                    )
                )
            )
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel.currentMembershipPlan.value = args.currentMembershipPlan
        viewModel.currentMembershipCard.value = args.membershipCard
        planFieldsList.clear()
        planBooleanFieldsList.clear()
        val signUpFormType = args.signUpFormType
        SharedPreferenceManager.isLoyaltySelected = true

        binding.item = viewModel.currentMembershipPlan.value

        if (viewModel.currentMembershipPlan.value != null) {
            binding.descriptionAddAuth.text =
                getString(
                    R.string.login_description,
                    viewModel.currentMembershipPlan.value!!.account?.company_name,
                    viewModel.currentMembershipPlan.value?.account?.plan_name
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
                binding.titleAddAuthText.text = getString(R.string.log_in_text)
                binding.addCardButton.text = getString(R.string.log_in_text)
                with(viewModel.currentMembershipPlan.value!!) {
                    if (viewModel.currentMembershipCard.value != null) {
                        if (feature_set?.has_points != null &&
                            feature_set.has_points == true &&
                            feature_set.transactions_available != null
                        ) {
                            if (feature_set.transactions_available == true) {
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
                        viewModel.currentMembershipPlan.value?.account?.plan_name_card
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
                binding.titleAddAuthText.text = getString(R.string.register_ghost_card_title)
                binding.addCardButton.text = getString(R.string.register_ghost_card_button)
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

        planBooleanFieldsList.map { planFieldsList.add(it) }

        val addRegisterFieldsRequest = Account()

        planFieldsList.map {
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
                planFieldsList.toList(),
                buttonRefresh = {
                    addRegisterFieldsRequest.plan_documents?.map { plan ->
                        var required = true
                        planBooleanFieldsList.map { field ->
                            if (field.second.column == plan.column) {
                                (field.first as PlanDocuments).checkbox?.let { bool ->
                                    if (!bool) {
                                        required = false
                                    }
                                }
                            }
                        }
                        if (required && plan.value != true.toString()) {
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
                if (isNetworkAvailable(requireActivity(), true)) {
                    addRegisterFieldsRequest.plan_documents?.map {
                        if (it.value != "true") {
                            requireContext().displayModalPopup(
                                EMPTY_STRING,
                                getString(R.string.required_fields)
                            )
                            return@setOnClickListener
                        }
                    }

                    planFieldsList.map {
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
                            if (addRegisterFieldsRequest.add_fields.isNullOrEmpty()) {
                                context?.displayModalPopup(
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

            when (viewModel.currentMembershipPlan.value?.feature_set?.card_type) {
                CardType.VIEW.type, CardType.STORE.type -> {
                    if (signUpFormType == SignUpFormType.GHOST) {
                        viewModel.currentMembershipPlan.value?.let {
                            findNavController().navigateIfAdded(
                                this,
                                AddAuthFragmentDirections.signUpToDetails(
                                    viewModel.currentMembershipPlan.value!!,
                                    membershipCard
                                )
                            )
                        }
                    }
                }
                CardType.PLL.type -> {
                    if (signUpFormType == SignUpFormType.GHOST) {
                        handlePllGhost(membershipCard)
                    } else {
                        handlePll(membershipCard)
                    }
                }
            }

            hideLoadingViews()
        }

        viewModel.createCardError.observeNonNull(this) {
            if (!UtilFunctions.hasCertificatePinningFailed(it, requireContext())) {
                requireContext().displayModalPopup(
                    getString(R.string.add_card_error_title),
                    getString(R.string.add_card_error_message)
                )
            }
            hideLoadingViews()
        }
    }

    private fun handlePllGhost(membershipCard: MembershipCard) {
        if (membershipCard.membership_transactions.isNullOrEmpty()) {
            viewModel.currentMembershipPlan.value?.let { membershipPlan ->
                findNavController().navigateIfAdded(
                    this,
                    AddAuthFragmentDirections.signUpToPllEmpty(
                        membershipPlan,
                        membershipCard,
                        false
                    )
                )
            }
        }
    }

    private fun handlePll(membershipCard: MembershipCard) {
        viewModel.currentMembershipPlan.value?.let { membershipPlan ->
            if (membershipCard.payment_cards.isNullOrEmpty()) {
                findNavController().navigateIfAdded(
                    this,
                    AddAuthFragmentDirections.signUpToPllEmpty(
                        membershipPlan,
                        membershipCard,
                        false
                    )
                )
            } else {
                findNavController().navigateIfAdded(
                    this,
                    AddAuthFragmentDirections.signUpToPll(
                        membershipCard,
                        membershipPlan,
                        true
                    )
                )
            }
        }
    }

    private fun hideLoadingViews() {
        binding.progressSpinner.visibility = View.GONE
        viewModel.createCardError.value = null
        binding.addCardButton.isEnabled = true
    }
}
