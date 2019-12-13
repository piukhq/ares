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
            val directions =
                viewModel.currentMembershipPlan.value?.account?.plan_description?.let { message ->
                    GenericModalParameters(
                        R.drawable.ic_close,
                        true,
                        getString(R.string.plan_description),
                        message, getString(R.string.ok)
                    )
                }?.let { params ->
                    AddAuthFragmentDirections.signUpToBrandHeader(params)
                }
            directions?.let { _ ->
                findNavController().navigateIfAdded(this, directions)
            }
        }

        when (signUpFormType) {
            SignUpFormType.ADD_AUTH -> {
                binding.titleAddAuthText.text = getString(R.string.log_in_text)
                binding.addCardButton.text = getString(R.string.log_in_text)
                with(viewModel) {
                    if (currentMembershipCard.value != null) {
                        currentMembershipCard.value?.let { membershipCard ->
                            currentMembershipPlan.value?.let { membershipPlan ->
                                membershipPlan.feature_set?.apply {
                                    has_points?.let { hasPoints ->
                                        if (hasPoints) {
                                            transactions_available?.let { transactionsAvailable ->
                                                binding.descriptionAddAuth.text =
                                                    if (transactionsAvailable) {
                                                        getString(
                                                            R.string.log_in_transaction_available,
                                                            membershipPlan.account?.plan_name_card
                                                        )

                                                    } else {
                                                        getString(
                                                            R.string.log_in_transaction_unavailable,
                                                            membershipPlan.account?.plan_name_card
                                                        )
                                                    }
                                            }
                                        }
                                    }
                                }
                                if (MembershipPlanUtils.getAccountStatus(
                                        membershipPlan,
                                        membershipCard
                                    ) == LoginStatus.STATUS_LOGIN_FAILED
                                ) {
                                    binding.descriptionAddAuth.text = getString(
                                        R.string.log_in_transaction_available,
                                        membershipPlan.account?.plan_name_card
                                    )
                                }
                                membershipPlan.account?.authorise_fields?.map {
                                    it.typeOfField = TypeOfField.AUTH
                                    addFieldToList(it)
                                }
                            }
                        }
                    } else {
                        currentMembershipPlan.value?.account?.add_fields?.map {
                            it.typeOfField = TypeOfField.ADD
                            addFieldToList(it)
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
                    viewModel.currentMembershipPlan.value?.account?.enrol_fields?.map {
                        it.typeOfField = TypeOfField.ENROL
                        addFieldToList(it)
                    }
                    noAccountText.visibility = View.GONE
                }
            }
            SignUpFormType.GHOST -> {
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

                binding.noAccountText.visibility = View.GONE
            }
        }

        binding.noAccountText.setOnClickListener {
            viewModel.currentMembershipPlan.value?.feature_set?.linking_support?.let {
                if (it.contains(TypeOfField.REGISTRATION.name)) {
                    viewModel.currentMembershipPlan.value?.let {
                        val action = AddAuthFragmentDirections.toGhost(
                            SignUpFormType.GHOST,
                            it,
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
            planFieldsList?.let {
                adapter = SignUpAdapter(it.toList())
            }

            binding.addCardButton.setOnClickListener {
                if (viewModel.createCardError.value == null) {
                    if (verifyAvailableNetwork(requireActivity())) {
                        planFieldsList?.map {
                            if (!UtilFunctions.isValidField(
                                    it.first.validation,
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

                        viewModel.currentMembershipCard.value?.let { card ->
                            viewModel.currentMembershipPlan.value?.let { plan ->
                                when (signUpFormType) {
                                    SignUpFormType.ADD_AUTH -> {
                                        val currentRequest = MembershipCardRequest(
                                            addRegisterFieldsRequest,
                                            plan.id
                                        )

                                        if (viewModel.currentMembershipCard.value != null &&
                                            MembershipPlanUtils.getAccountStatus(
                                                plan,
                                                card
                                            ) == LoginStatus.STATUS_LOGIN_FAILED
                                        ) {
                                            viewModel.updateMembershipCard(
                                                card,
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
                                                null
                                            ),
                                            plan.id
                                        )
                                        viewModel.createMembershipCard(
                                            currentRequest
                                        )
                                    }

                                    SignUpFormType.ENROL -> {
                                        viewModel.createMembershipCard(
                                            MembershipCardRequest(
                                                addRegisterFieldsRequest,
                                                plan.id
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                        showNoInternetConnectionDialog()
                    }
                    binding.addCardButton.isEnabled = false
                    binding.progressSpinner.visibility = View.VISIBLE
                }
            }
        }

        viewModel.newMembershipCard.observeNonNull(this) { membershipCard ->
            viewModel.currentMembershipPlan.value?.let { plan ->
                viewModel.currentMembershipCard.value?.let { _ ->
                    if (viewModel.newMembershipCard.hasActiveObservers())
                        viewModel.newMembershipCard.removeObservers(this)
                    if (signUpFormType == SignUpFormType.GHOST) {
                        val currentRequest = MembershipCardRequest(
                            Account(
                                null,
                                null,
                                null,
                                addRegisterFieldsRequest.registration_fields
                            ),
                            plan.id
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
                                addRegisterFieldsRequest.registration_fields
                            ),
                            plan.id
                        )
                        viewModel.ghostMembershipCard(
                            membershipCard,
                            currentRequest
                        )
                    }
                    when (plan.feature_set?.card_type) {
                        CardType.VIEW.type,
                        CardType.STORE.type -> {
                            val directions =
                                AddAuthFragmentDirections.signUpToDetails(
                                    plan,
                                    membershipCard
                                )
                            findNavController().navigateIfAdded(this, directions)
                        }
                        CardType.PLL.type -> {
                            if (signUpFormType == SignUpFormType.GHOST) {
                                if (membershipCard.membership_transactions.isNullOrEmpty()) {
                                    val directions = AddAuthFragmentDirections.signUpToPllEmpty(
                                        plan,
                                        membershipCard
                                    )
                                    findNavController().navigateIfAdded(this, directions)
                                }
                            } else {
                                if (viewModel.currentMembershipPlan.value != null) {
                                    val directions =
                                        if (viewModel.paymentCards.value.isNullOrEmpty()) {
                                            AddAuthFragmentDirections.signUpToPllEmpty(
                                                plan,
                                                membershipCard
                                            )
                                        } else {
                                            AddAuthFragmentDirections.signUpToPll(
                                                membershipCard,
                                                plan,
                                                true
                                            )
                                        }
                                    findNavController().navigateIfAdded(this, directions)
                                }
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
        binding.progressSpinner.visibility = View.GONE
        viewModel.createCardError.value = null
        binding.addCardButton.isEnabled = true
    }
}
