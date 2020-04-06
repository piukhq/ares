package com.bink.wallet.scenes.add_auth_enrol.screens

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.navArgs
import com.bink.wallet.BaseFragment
import com.bink.wallet.R
import com.bink.wallet.databinding.AddAuthFragmentBinding
import com.bink.wallet.model.request.membership_card.Account
import com.bink.wallet.model.response.membership_card.MembershipCard
import com.bink.wallet.model.response.payment_card.PaymentCard
import com.bink.wallet.scenes.add_auth_enrol.view_models.AddAuthViewModel
import com.bink.wallet.utils.ApiErrorUtils
import com.bink.wallet.utils.ApiErrorUtils.Companion.getApiErrorMessage
import com.bink.wallet.utils.ExceptionHandlingUtils
import com.bink.wallet.utils.UtilFunctions.isNetworkAvailable
import com.bink.wallet.utils.displayModalPopup
import com.bink.wallet.utils.enums.HandledException
import com.bink.wallet.utils.enums.SignUpFormType
import com.bink.wallet.utils.observeNonNull
import com.bink.wallet.utils.toolbar.FragmentToolbar
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

    private var isRetryJourney = false
    private var isFromNoReasonCodes = false
    private var membershipCardId: String? = null


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        with(args) {
            this@AddAuthFragment.membershipCardId = membershipCardId
            this@AddAuthFragment.isRetryJourney = isRetryJourney
            this@AddAuthFragment.isFromNoReasonCodes = isFromNoReasonCodes
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
//            if (signUpFormType == SignUpFormType.GHOST) {
//                val currentRequest = MembershipCardRequest(
//                    Account(
//                        null,
//                        null,
//                        null,
//                        addRegisterFieldsRequest.registration_fields,
//                        null
//                    ),
//                    viewModel.currentMembershipPlan.value?.id
//                )
//                if (!isRetryJourney) {
//                    viewModel.ghostMembershipCard(
//                        membershipCard.id,
//                        currentRequest
//                    )
//                }
//            }

//            when (viewModel.currentMembershipPlan.value?.feature_set?.card_type) {
//                CardType.VIEW.type, CardType.STORE.type -> {
//                    viewModel.currentMembershipPlan.value?.let { membershipPlan ->
//                        findNavController().navigateIfAdded(
//                            this,
//                            AddAuthFragmentDirections.signUpToDetails(
//                                membershipPlan,
//                                membershipCard
//                            )
//                        )
//                    }
//                }
//                CardType.PLL.type -> {
//                    if (signUpFormType == SignUpFormType.GHOST) {
//                        handlePllGhost(membershipCard)
//                    } else {
////                        viewModel.paymentCards.value?.let { paymentCards ->
////                            handlePll(membershipCard, paymentCards)
////                        }
//                    }
//                }
//            }
            hideLoadingViews()
        }
    }


    private fun handlePll(membershipCard: MembershipCard, paymentCards: List<PaymentCard>) {
//        viewModel.currentMembershipPlan.value?.let { membershipPlan ->
//            if (paymentCards.isNullOrEmpty()) {
//                findNavController().navigateIfAdded(
//                    this,
//                    AddAuthFragmentDirections.signUpToPllEmpty(
//                        membershipPlan,
//                        membershipCard,
//                        false
//                    )
//                )
//            } else {
//                findNavController().navigateIfAdded(
//                    this,
//                    AddAuthFragmentDirections.signUpToPll(
//                        membershipCard,
//                        membershipPlan,
//                        true
//                    )
//                )
//            }
//        }
    }

    private fun hideLoadingViews() {
        with(binding) {
            progressSpinner.visibility = View.GONE
//            viewModel.createCardError.value = null
            addCardButton.isEnabled = true
        }
    }

}
