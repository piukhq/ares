package com.bink.wallet.scenes.add_auth_enrol.screens

import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.navigation.fragment.navArgs
import com.bink.wallet.R
import com.bink.wallet.data.SharedPreferenceManager
import com.bink.wallet.scenes.add_auth_enrol.view_models.AddCardViewModel
import com.bink.wallet.utils.FirebaseEvents
import com.bink.wallet.utils.FirebaseEvents.ADD_AUTH_FORM_VIEW
import com.bink.wallet.utils.FirebaseEvents.ADD_LOYALTY_CARD_ADD_JOURNEY
import com.bink.wallet.utils.FirebaseEvents.ADD_LOYALTY_CARD_REQUEST
import com.bink.wallet.utils.FirebaseEvents.ADD_LOYALTY_CARD_RESPONSE_FAILURE
import com.bink.wallet.utils.FirebaseEvents.ADD_LOYALTY_CARD_RESPONSE_SUCCESS
import com.bink.wallet.utils.FirebaseEvents.FIREBASE_FALSE
import com.bink.wallet.utils.FirebaseEvents.FIREBASE_TRUE
import com.bink.wallet.utils.observeNonNull
import org.koin.androidx.viewmodel.ext.android.viewModel

class AddCardFragment : BaseAddAuthFragment() {

    private var isFromNoReasonCodes = false

    override val viewModel: AddCardViewModel by viewModel()

    private val addCardArgs: AddCardFragmentArgs by navArgs()

    private var membershipPlanId = "no_plan_id_found"

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        isRetryJourney = addCardArgs.isRetryJourney
        isFromNoReasonCodes = addCardArgs.isFromNoReasonCodes
        currentMembershipPlan = addCardArgs.membershipPlan
        membershipPlanId = addCardArgs.membershipPlan.id

        setViewsContent()

        //todo We've removed this functionality as we currently can't support positive ghost card journeys
        //todo https://hellobink.atlassian.net/browse/AB20-739
//        binding.footerComposed.noAccount.setOnClickListener {
//            navigationHandler?.navigateToGhostRegistrationUnavailableScreen()
//            logNoAccountClick()
//        }

        binding.footerComposed.addAuthCta.setOnClickListener {
            logCTAClick(it)
            handleAuthCtaRequest()
        }

        binding.footerSimple.addAuthCta.setOnClickListener {
            logCTAClick(it)
            handleAuthCtaRequest()
        }

        viewModel.newMembershipCard.observeNonNull(this) {
            handleNavigationAfterCardCreation(it, false)
            val status = it.status?.state ?: "no_status_available"
            //Is it always going to be just one?
            val reasonCode = it.status?.reason_codes?.get(0) ?: "no_reason_code_found"
            val mPlanId = membershipPlanId
            val isAccountNew =
                if (SharedPreferenceManager.addLoyaltyCardSuccessHttpCode == 201) FIREBASE_TRUE else FIREBASE_FALSE
            logEvent(
                ADD_LOYALTY_CARD_RESPONSE_SUCCESS, getAddLoyaltyResponseSuccessMap(
                    ADD_LOYALTY_CARD_ADD_JOURNEY, status, reasonCode, mPlanId, isAccountNew
                )
            )
        }

        viewModel.addLoyaltyCardRequestMade.observeNonNull(this) {
            val isScanned =
                if (SharedPreferenceManager.isScannedCard) FIREBASE_TRUE else FIREBASE_FALSE

            logEvent(
                ADD_LOYALTY_CARD_REQUEST, getAddLoyaltyCardRequestMap(
                    ADD_LOYALTY_CARD_ADD_JOURNEY, membershipPlanId, isScanned
                )
            )
        }

        viewModel.createCardError.observeNonNull(this) {
            logEvent(
                ADD_LOYALTY_CARD_RESPONSE_FAILURE, getAddLoyaltyResponseFailureMap(
                    ADD_LOYALTY_CARD_ADD_JOURNEY, membershipPlanId
                )
            )
        }
    }

    override fun onResume() {
        super.onResume()
        currentMembershipPlan?.let {
            val shouldExcludeBarcode = barcode.isNullOrEmpty()
            viewModel.addItems(it, shouldExcludeBarcode)
        }
        logScreenView(ADD_AUTH_FORM_VIEW)
    }

    private fun setViewsContent() {
        viewModel.ctaText.set(retrieveCTAText())
        viewModel.titleText.set(retrieveTitleText())
        currentMembershipPlan?.let {
            val resourceString = viewModel.retrieveDescriptionText(it, isRetryJourney)?.first
            val stringValue = viewModel.retrieveDescriptionText(it, isRetryJourney)?.second
            resourceString?.let {
                if (stringValue != null) {
                    viewModel.descriptionText.set(getString(resourceString, stringValue))
                } else {
                    viewModel.descriptionText.set(getString(resourceString))
                }
            }
        }
        viewModel.isNoAccountFooter.set(!isFooterSimple())
    }

    private fun isFooterSimple() = isRetryJourney && !isFromNoReasonCodes

    //todo We've removed this functionality as we currently can't support positive ghost card journeys
    //todo https://hellobink.atlassian.net/browse/AB20-739
    private fun logNoAccountClick() {
//        logEvent(
//            FirebaseEvents.getFirebaseIdentifier(
//                ADD_AUTH_FORM_VIEW,
//                binding.footerComposed.noAccount.text.toString()
//            )
//        )
    }

    private fun logCTAClick(button: View) {
        logEvent(
            FirebaseEvents.getFirebaseIdentifier(
                ADD_AUTH_FORM_VIEW,
                (button as Button).text.toString()
            )
        )
    }

    private fun retrieveCTAText() = if (isRetryJourney) {
        getString(R.string.log_in_text)
    } else {
        getString(R.string.add_card)
    }

    private fun retrieveTitleText() = if (isRetryJourney) {
        getString(R.string.log_in_text)
    } else {
        getString(R.string.enter_credentials)
    }

    private fun handleAuthCtaRequest() {
        binding.loadingIndicator.visibility = View.VISIBLE
        membershipCardId?.let {
            currentMembershipPlan?.let { plan ->
                viewModel.handleRequest(isRetryJourney, it, plan)
            }
        }
    }
}