package com.bink.wallet.scenes.add_auth_enrol.screens

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bink.wallet.BaseFragment
import com.bink.wallet.R
import com.bink.wallet.databinding.AddAuthFragmentBinding
import com.bink.wallet.model.request.membership_card.Account
import com.bink.wallet.model.request.membership_card.MembershipCardRequest
import com.bink.wallet.model.request.membership_card.PlanFieldsRequest
import com.bink.wallet.model.response.membership_card.MembershipCard
import com.bink.wallet.model.response.membership_plan.PlanDocument
import com.bink.wallet.model.response.membership_plan.PlanField
import com.bink.wallet.model.response.payment_card.PaymentCard
import com.bink.wallet.scenes.add_auth_enrol.AddAuthViewModel
import com.bink.wallet.utils.*
import com.bink.wallet.utils.ApiErrorUtils.Companion.getApiErrorMessage
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
    

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        with(args) {
            viewModel.currentMembershipPlan.value = currentMembershipPlan
            this@AddAuthFragment.membershipCardId = membershipCardId
            this@AddAuthFragment.isRetryJourney = isRetryJourney
            this@AddAuthFragment.isFromNoReasonCodes = isFromNoReasonCodes
        }


        planFieldsList.clear()
        planBooleanFieldsList.clear()

        val signUpFormType = args.signUpFormType

        binding.item = viewModel.currentMembershipPlan.value

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

        viewModel.fetchCardsError.observeErrorNonNull(requireContext(), this, false) {}

        val addRegisterFieldsRequest = Account()

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
                                (field.first as PlanDocument).checkbox?.let { hasCheckbox ->
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
                        if (it.first is PlanField) {
                            if (!UtilFunctions.isValidField(
                                    (it.first as PlanField).validation,
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
}
