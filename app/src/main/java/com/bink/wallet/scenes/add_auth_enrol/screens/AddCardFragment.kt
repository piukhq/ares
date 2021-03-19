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
import com.bink.wallet.utils.local_point_scraping.WebScrapableManager
import com.bink.wallet.utils.getErrorBody
import com.bink.wallet.utils.observeNonNull
import org.koin.androidx.viewmodel.ext.android.viewModel
import retrofit2.HttpException

class AddCardFragment : BaseAddAuthFragment() {

    private var isFromNoReasonCodes = false

    override val viewModel: AddCardViewModel by viewModel()

    private val addCardArgs: AddCardFragmentArgs by navArgs()

    private var membershipPlanId: String? = null

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
            val status = it.status?.state
            //Is it always going to be just one?
            val reasonCode = it.status?.reason_codes?.get(0)
            val mPlanId = membershipPlanId
            if (status == null || reasonCode == null || mPlanId == null) {
                failedEvent(ADD_LOYALTY_CARD_RESPONSE_SUCCESS)
            } else {
                val isAccountNew =
                    if (SharedPreferenceManager.addLoyaltyCardSuccessHttpCode == 201) FIREBASE_TRUE else FIREBASE_FALSE
                logEvent(
                    ADD_LOYALTY_CARD_RESPONSE_SUCCESS, getAddLoyaltyResponseSuccessMap(
                        ADD_LOYALTY_CARD_ADD_JOURNEY, it.id, status, reasonCode, mPlanId, isAccountNew
                    )
                )
            }

            WebScrapableManager.storeCredentialsFromRequest(it.id)

            WebScrapableManager.tryScrapeCards(0, arrayListOf(it), context, true) { cards ->
                if (cards != null) {
                    viewModel.updateScrapedCards(cards)
                }
            }

        }

        viewModel.addLoyaltyCardRequestMade.observeNonNull(this) {
            val mPlanId = membershipPlanId

            if (mPlanId == null) {
                failedEvent(ADD_LOYALTY_CARD_REQUEST)
            } else {
                val isScanned =
                    if (SharedPreferenceManager.isScannedCard) FIREBASE_TRUE else FIREBASE_FALSE

                logEvent(
                    ADD_LOYALTY_CARD_REQUEST, getAddLoyaltyCardRequestMap(
                        ADD_LOYALTY_CARD_ADD_JOURNEY, mPlanId, isScanned
                    )
                )
            }

        }

        viewModel.createCardError.observeNonNull(this) {
            val mPlanId = membershipPlanId

            if (mPlanId == null) {
                failedEvent(ADD_LOYALTY_CARD_RESPONSE_FAILURE)
            } else {
                val httpException = it as HttpException
                logEvent(
                    ADD_LOYALTY_CARD_RESPONSE_FAILURE, getAddLoyaltyResponseFailureMap(
                        FirebaseEvents.ADD_LOYALTY_CARD_REGISTER_JOURNEY, mPlanId, httpException.code(), httpException.getErrorBody()
                    )
                )
            }

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