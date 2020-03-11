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
import com.bink.wallet.model.response.payment_card.PaymentCard
import com.bink.wallet.utils.*
import com.bink.wallet.utils.ApiErrorUtils.Companion.getApiErrorMessage
import com.bink.wallet.utils.FirebaseEvents.ADD_AUTH_FORM_VIEW
import com.bink.wallet.utils.FirebaseEvents.ENROL_FORM_VIEW
import com.bink.wallet.utils.FirebaseEvents.REGISTRATION_FORM_VIEW
import com.bink.wallet.utils.FirebaseEvents.getFirebaseIdentifier
import com.bink.wallet.utils.UtilFunctions.isNetworkAvailable
import com.bink.wallet.utils.enums.*
import com.bink.wallet.utils.toolbar.FragmentToolbar
import kotlinx.coroutines.runBlocking
import org.koin.androidx.viewmodel.ext.android.viewModel
import retrofit2.HttpException
import java.net.SocketTimeoutException

class AddAuthFragment : BaseFragment<AddAuthViewModel, AddAuthFragmentBinding>() {
    override fun builder(): FragmentToolbar {
        return FragmentToolbar.Builder()
            .build()
    }

    override val layoutRes: Int
        get() = R.layout.add_auth_fragment

    private val args: AddAuthFragmentArgs by navArgs()

    override val viewModel: AddAuthViewModel by viewModel()

    override fun onResume() {
        super.onResume()
        logScreenView(getScreenName(args.signUpFormType))
        windowFullscreenHandler.toFullscreen()
    }

    private var isPaymentWalletEmpty: Boolean? = null

    private var isRetryJourney = false

    private var isFromNoReasonCodes = false

    private var membershipCardId: String? = null

    private val planFieldsList: MutableList<Pair<Any, PlanFieldsRequest>> =
        mutableListOf()

    private val planBooleanFieldsList: MutableList<Pair<Any, PlanFieldsRequest>> =
        mutableListOf()

    private fun addFieldToList(planField: Any) {
        when (planField) {
            is PlanFields -> {
                val pairPlanField = Pair(
                    planField, PlanFieldsRequest(
                        planField.column, EMPTY_STRING
                    )
                )

                viewModel.currentMembershipPlan.value?.has_vouchers?.let {
                    if (it) {
                        if (planField.common_name == SignUpFieldTypes.EMAIL.common_name) {
                            val email =
                                LocalStoreUtils.getAppSharedPref(LocalStoreUtils.KEY_EMAIL)
                                    ?.let {
                                        it
                                    }
                            with(pairPlanField) {
                                second.disabled = true
                                second.value = email
                            }
                        }
                    }
                }

                if (planField.type == FieldType.BOOLEAN_OPTIONAL.type) {
                    planBooleanFieldsList.add(
                        pairPlanField
                    )
                } else if (!planField.column.equals(BARCODE_TEXT)) {
                    planFieldsList.add(
                        pairPlanField
                    )
                }
            }

            is PlanDocuments -> {
                planBooleanFieldsList.add(
                    Pair(
                        planField, PlanFieldsRequest(
                            planField.name, EMPTY_STRING
                        )
                    )
                )
            }
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        with(args) {
            viewModel.currentMembershipPlan.value = currentMembershipPlan
            this@AddAuthFragment.membershipCardId = membershipCardId
            this@AddAuthFragment.isRetryJourney = isRetryJourney
            this@AddAuthFragment.isFromNoReasonCodes = isFromNoReasonCodes
        }

        if (isRetryJourney && !isFromNoReasonCodes) {
            binding.noAccountText.visibility = View.GONE
        }

        planFieldsList.clear()
        planBooleanFieldsList.clear()

        val signUpFormType = args.signUpFormType

        SharedPreferenceManager.isLoyaltySelected = true

        binding.item = viewModel.currentMembershipPlan.value


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
            if (isNetworkAvailable(requireContext(), false)) {
                viewModel.getPaymentCards()
            } else {
                viewModel.getLocalPaymentCards()
            }
        }

        viewModel.paymentCardsMerger.observeNonNull(this) { paymentCards ->
            isPaymentWalletEmpty = paymentCards.isNullOrEmpty()
        }

        binding.addJoinReward.setOnClickListener {
            viewModel.currentMembershipPlan.value?.let {
                if (it.account?.plan_description != null) {
                    findNavController().navigateIfAdded(
                        this,
                        AddAuthFragmentDirections.signUpToBrandHeader(
                            GenericModalParameters(
                                R.drawable.ic_close,
                                true,
                                viewModel.currentMembershipPlan.value?.account?.plan_name
                                    ?: getString(R.string.plan_description),
                                it.account.plan_description
                            )
                        )
                    )
                } else if (it.account?.plan_name_card != null) {
                    it.account.plan_name?.let { planName ->
                        findNavController().navigateIfAdded(
                            this,
                            AddAuthFragmentDirections.signUpToBrandHeader(
                                GenericModalParameters(
                                    R.drawable.ic_close,
                                    true,
                                    planName
                                )
                            )
                        )
                    }
                }
            }
        }

        viewModel.fetchCardsError.observeNonNull(this) {
            requireContext().displayModalPopup(
                getString(R.string.error_title),
                getString(R.string.error_description)
            )
        }

        when (signUpFormType) {
            SignUpFormType.ADD_AUTH -> {
                viewModel.currentMembershipPlan.value?.let {
                    with(it) {
                        if (isRetryJourney) {
                            with(binding) {
                                titleAddAuthText.text = getString(R.string.log_in_text)
                                addCardButton.text = getString(R.string.log_in_text)
                            }
                            if (feature_set?.has_points != null &&
                                feature_set.has_points == true &&
                                feature_set.transactions_available != null &&
                                account?.plan_name_card != null
                            ) {
                                if (feature_set.transactions_available == true) {
                                    binding.descriptionAddAuth.text = getString(
                                        R.string.log_in_transaction_available,
                                        account.plan_name_card
                                    )
                                } else {
                                    binding.descriptionAddAuth.text =
                                        getString(
                                            R.string.log_in_transaction_unavailable,
                                            account.plan_name_card
                                        )
                                }
                            }
                        } else {
                            with(binding) {
                                titleAddAuthText.text = getString(R.string.enter_credentials)
                                addCardButton.text = getString(R.string.add_card)
                                account?.company_name?.let { companyName ->
                                    descriptionAddAuth.text =
                                        getString(
                                            R.string.please_enter_credentials,
                                            companyName
                                        )
                                }
                            }
                        }

                        account?.let {
                            account.add_fields?.map { planFields ->
                                planFields.typeOfField = TypeOfField.ADD
                                addFieldToList(planFields)
                            }
                            account.authorise_fields?.map { planFields ->
                                planFields.typeOfField = TypeOfField.AUTH
                                addFieldToList(planFields)
                            }
                            account.plan_documents?.map { planDocuments ->
                                planDocuments.display?.let { display ->
                                    if (display.contains(SignUpFormType.ADD_AUTH.type)) {
                                        addFieldToList(planDocuments)
                                    }
                                }
                            }
                        }

                    }
                }
            }
            SignUpFormType.ENROL -> {
                with(binding) {
                    noAccountText.visibility = View.GONE
                    titleAddAuthText.text = getString(R.string.sign_up_enrol)
                    addCardButton.text = getString(R.string.sign_up_text)
                    descriptionAddAuth.text = getString(
                        R.string.enrol_description,
                        viewModel.currentMembershipPlan.value?.account?.plan_name_card
                    )
                    viewModel.currentMembershipPlan.value?.account?.enrol_fields?.map {
                        it.typeOfField = TypeOfField.ENROL
                        addFieldToList(it)
                    }
                }

                viewModel.currentMembershipPlan.value?.account?.plan_documents?.map {
                    it.display?.let { display ->
                        if (display.contains(SignUpFormType.ENROL.type)) {
                            addFieldToList(it)
                        }
                    }
                }

            }
            SignUpFormType.GHOST -> {
                binding.noAccountText.visibility = View.GONE
                binding.titleAddAuthText.text = getString(R.string.register_ghost_card_title)
                binding.addCardButton.text = getString(R.string.register_ghost_card_button)
                viewModel.currentMembershipPlan.value?.account?.add_fields?.map {
                    it.typeOfField = TypeOfField.ADD
                    addFieldToList(it)
                }

                viewModel.currentMembershipPlan.value?.account?.registration_fields?.map {
                    it.typeOfField = TypeOfField.REGISTRATION
                    addFieldToList(it)
                }

                viewModel.currentMembershipPlan.value?.account?.plan_documents?.map {
                    it.display?.let { display ->
                        if (display.contains(SignUpFormType.GHOST.type)) {
                            addFieldToList(it)
                        }
                    }
                }
            }
        }

        binding.noAccountText.setOnClickListener {
            //TODO: Replace this with appropriate navigation logic after MVP
            findNavController().navigateIfAdded(
                this,
                AddAuthFragmentDirections.signUpToGhostRegistrationUnavailable(
                    GenericModalParameters(
                        R.drawable.ic_close,
                        true,
                        getString(R.string.title_ghost_card_not_available),
                        getString(R.string.description_ghost_card_not_available)
                    )
                )
            )
//            viewModel.currentMembershipPlan.value?.feature_set?.linking_support?.let { linkingSupport ->
//                if (linkingSupport.contains(TypeOfField.REGISTRATION.name)
//                ) {
//                    viewModel.currentMembershipPlan.value?.let {
//                        findNavController().navigateIfAdded(
//                            this,
//                            AddAuthFragmentDirections.toGhost(
//                                SignUpFormType.GHOST,
//                                it,
//                                isRetryJourney
//                            )
//                        )
//                    }
//                } else {
//                    findNavController().navigateIfAdded(
//                        this,
//                        AddAuthFragmentDirections.signUpToGhostRegistrationUnavailable(
//                            GenericModalParameters(
//                                R.drawable.ic_close,
//                                true,
//                                getString(R.string.title_ghost_card_not_available),
//                                getString(R.string.description_ghost_card_not_available)
//                            )
//                        )
//                    )
//
//                }
//            }

            logEvent(
                getFirebaseIdentifier(
                    getScreenName(args.signUpFormType),
                    binding.noAccountText.text.toString()
                )
            )
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
                        val item = it.first
                        if (item is PlanFields && item.type != FieldType.BOOLEAN_OPTIONAL.type) {
                            if (it.second.value.isNullOrEmpty()) {
                                binding.addCardButton.isEnabled = false
                                return@AddAuthAdapter
                            } else {
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
                    }
                    binding.addCardButton.isEnabled = true
                }
            )
        }

        binding.addCardButton.isEnabled = false

        viewModel.createCardError.observeNonNull(this) { exception ->
            when (ExceptionHandlingUtils.onHttpException(exception)) {
                HandledException.BAD_REQUEST -> {
                    if (exception is HttpException) {
                        requireContext().displayModalPopup(
                            getString(R.string.error),
                            getApiErrorMessage(
                                exception,
                                getString(R.string.error_scheme_already_exists)
                            )
                        )
                    } else {
                        requireContext().displayModalPopup(
                            getString(R.string.error),
                            getString(R.string.error_scheme_already_exists)
                        )
                    }
                }
                else -> {
                    if (((exception is HttpException)
                                && exception.code() >= ApiErrorUtils.SERVER_ERROR)
                        || exception is SocketTimeoutException
                    ) {
                        requireContext().displayModalPopup(
                            requireContext().getString(R.string.error_server_down_title),
                            requireContext().getString(R.string.error_server_down_message)
                        )
                    } else {
                        requireContext().displayModalPopup(
                            getString(R.string.add_card_error_title),
                            getString(R.string.add_card_error_message)
                        )
                    }
                }
            }
            hideLoadingViews()
        }

        binding.addCardButton.setOnClickListener {
            if (viewModel.createCardError.value == null) {
                if (isNetworkAvailable(requireActivity(), true)) {
                    addRegisterFieldsRequest.plan_documents?.map { plan ->
                        var required = true
                        planBooleanFieldsList.map { field ->
                            if (field.second.column == plan.column) {
                                (field.first as PlanDocuments).checkbox?.let { hasCheckbox ->
                                    if (!hasCheckbox) {
                                        required = false
                                    }
                                }
                            }
                        }
                        if (required && plan.value != true.toString()) {
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
                                requireContext().displayModalPopup(
                                    null,
                                    getString(R.string.all_fields_must_be_valid)
                                )
                                return@setOnClickListener
                            }
                        }
                    }
                    viewModel.currentMembershipPlan.value?.let { membershipPlan ->
                        when (signUpFormType) {
                            SignUpFormType.ADD_AUTH -> {
                                val currentRequest = MembershipCardRequest(
                                    addRegisterFieldsRequest,
                                    membershipPlan.id
                                )
                                if (isRetryJourney && !membershipCardId.isNullOrEmpty()) {
                                    membershipCardId?.let {
                                        viewModel.updateMembershipCard(it, currentRequest)
                                    }
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

                                val currentRequest: MembershipCardRequest

                                if (isRetryJourney && !membershipCardId.isNullOrEmpty()) {
                                    currentRequest = MembershipCardRequest(
                                        Account(
                                            null,
                                            null,
                                            null,
                                            addRegisterFieldsRequest.registration_fields,
                                            null
                                        ),
                                        membershipPlan.id
                                    )
                                    membershipCardId?.let {
                                        viewModel.ghostMembershipCard(it, currentRequest)
                                    }
                                } else {
                                    currentRequest = MembershipCardRequest(
                                        Account(
                                            addRegisterFieldsRequest.add_fields,
                                            null,
                                            null,
                                            null,
                                            null
                                        ),
                                        membershipPlan.id
                                    )
                                    viewModel.createMembershipCard(
                                        currentRequest
                                    )
                                }
                            }

                            SignUpFormType.ENROL -> {
                                val currentRequest = MembershipCardRequest(
                                    addRegisterFieldsRequest,
                                    membershipPlan.id
                                )
                                if (isRetryJourney && !membershipCardId.isNullOrEmpty()) {
                                    membershipCardId?.let {
                                        viewModel.updateMembershipCard(it, currentRequest)
                                    }
                                } else {
                                    viewModel.createMembershipCard(
                                        MembershipCardRequest(
                                            addRegisterFieldsRequest,
                                            membershipPlan.id
                                        )
                                    )
                                }
                            }
                        }
                    }

                    binding.addCardButton.isEnabled = false
                    binding.progressSpinner.visibility = View.VISIBLE
                } else {
                    binding.progressSpinner.visibility = View.GONE
                }
            }

            logEvent(
                getFirebaseIdentifier(
                    getScreenName(args.signUpFormType),
                    binding.addCardButton.text.toString()
                )
            )
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
                    viewModel.currentMembershipPlan.value?.id
                )
                if (!isRetryJourney) {
                    viewModel.ghostMembershipCard(
                        membershipCard.id,
                        currentRequest
                    )
                }
            }

            when (viewModel.currentMembershipPlan.value?.feature_set?.card_type) {
                CardType.VIEW.type, CardType.STORE.type -> {
                    viewModel.currentMembershipPlan.value?.let { membershipPlan ->
                        findNavController().navigateIfAdded(
                            this,
                            AddAuthFragmentDirections.signUpToDetails(
                                membershipPlan,
                                membershipCard
                            )
                        )
                    }
                }
                CardType.PLL.type -> {
                    if (signUpFormType == SignUpFormType.GHOST) {
                        handlePllGhost(membershipCard)
                    } else {
                        viewModel.paymentCards.value?.let { paymentCards ->
                            handlePll(membershipCard, paymentCards)
                        }
                    }
                }
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

    private fun handlePll(membershipCard: MembershipCard, paymentCards: List<PaymentCard>) {
        viewModel.currentMembershipPlan.value?.let { membershipPlan ->
            if (paymentCards.isNullOrEmpty()) {
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
        with(binding) {
            progressSpinner.visibility = View.GONE
            viewModel.createCardError.value = null
            addCardButton.isEnabled = true
        }
    }

    companion object {
        private const val BARCODE_TEXT = "Barcode"
    }

    private fun getScreenName(signUpFormType: SignUpFormType): String {
        return when (signUpFormType) {
            SignUpFormType.ADD_AUTH -> {
                ADD_AUTH_FORM_VIEW
            }
            SignUpFormType.ENROL -> {
                ENROL_FORM_VIEW
            }
            SignUpFormType.GHOST -> {
                REGISTRATION_FORM_VIEW
            }
        }
    }
}
