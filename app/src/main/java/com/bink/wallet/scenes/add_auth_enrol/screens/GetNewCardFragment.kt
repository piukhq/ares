package com.bink.wallet.scenes.add_auth_enrol.screens

import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.navigation.fragment.navArgs
import com.bink.wallet.R
import com.bink.wallet.data.SharedPreferenceManager
import com.bink.wallet.scenes.add_auth_enrol.view_models.GetNewCardViewModel
import com.bink.wallet.utils.FirebaseEvents
import com.bink.wallet.utils.FirebaseEvents.ADD_LOYALTY_CARD_REGISTER_JOURNEY
import com.bink.wallet.utils.FirebaseEvents.ADD_LOYALTY_CARD_REQUEST
import com.bink.wallet.utils.FirebaseEvents.ADD_LOYALTY_CARD_RESPONSE_FAILURE
import com.bink.wallet.utils.FirebaseEvents.ADD_LOYALTY_CARD_RESPONSE_SUCCESS
import com.bink.wallet.utils.FirebaseEvents.ENROL_FORM_VIEW
import com.bink.wallet.utils.FirebaseEvents.FIREBASE_FALSE
import com.bink.wallet.utils.FirebaseEvents.FIREBASE_TRUE
import com.bink.wallet.utils.observeNonNull
import org.koin.androidx.viewmodel.ext.android.viewModel

class GetNewCardFragment : BaseAddAuthFragment() {

    override val viewModel: GetNewCardViewModel by viewModel()

    private val getCardArgs: GetNewCardFragmentArgs by navArgs()

    private var membershipPlanId = "no_plan_available"

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        isRetryJourney = getCardArgs.isRetryJourney
        membershipCardId = getCardArgs.membershipCardId
        currentMembershipPlan = getCardArgs.membershipPlan
        membershipPlanId = getCardArgs.membershipPlan.id

        setViewsContent()

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
                    ADD_LOYALTY_CARD_REGISTER_JOURNEY, status, reasonCode, mPlanId, isAccountNew
                )
            )

        }
        binding.footerSimple.addAuthCta.setOnClickListener {
            logCTAClick(it)
            handleCtaRequest()
        }

        viewModel.addLoyaltyCardRequestMade.observeNonNull(this) {
            val isScanned =
                if (SharedPreferenceManager.isScannedCard) FIREBASE_TRUE else FIREBASE_FALSE

            logEvent(
                ADD_LOYALTY_CARD_REQUEST, getAddLoyaltyCardRequestMap(
                    ADD_LOYALTY_CARD_REGISTER_JOURNEY, membershipPlanId, isScanned
                )
            )
        }

        viewModel.createCardError.observeNonNull(this) {
            logEvent(
                ADD_LOYALTY_CARD_RESPONSE_FAILURE, getAddLoyaltyResponseFailureMap(
                    ADD_LOYALTY_CARD_REGISTER_JOURNEY, membershipPlanId
                )
            )
        }
    }

    override fun onResume() {
        super.onResume()
        currentMembershipPlan?.let {
            viewModel.addItems(it)
        }
        logScreenView(ENROL_FORM_VIEW)
    }

    private fun setViewsContent() {
        viewModel.titleText.set(getString(R.string.sign_up_enrol))
        viewModel.ctaText.set(getString(R.string.sign_up_text))
        currentMembershipPlan?.let {
            viewModel.descriptionText.set(
                getString(
                    R.string.enrol_description,
                    it.account?.plan_name_card
                )
            )
        }
        viewModel.isNoAccountFooter.set(false)
    }

    private fun logCTAClick(button: View) {
        logEvent(
            FirebaseEvents.getFirebaseIdentifier(
                ENROL_FORM_VIEW,
                (button as Button).text.toString()
            )
        )
    }

    private fun handleCtaRequest() {
        membershipCardId?.let {
            currentMembershipPlan?.let { plan ->
                viewModel.handleRequest(isRetryJourney, it, plan)
            }
        }
    }
}
