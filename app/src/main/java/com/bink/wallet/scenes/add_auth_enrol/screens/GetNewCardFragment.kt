package com.bink.wallet.scenes.add_auth_enrol.screens

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.navArgs
import com.bink.wallet.R
import com.bink.wallet.data.SharedPreferenceManager
import com.bink.wallet.scenes.add_auth_enrol.view_models.GetNewCardViewModel
import com.bink.wallet.utils.*
import com.bink.wallet.utils.FirebaseEvents.ADD_LOYALTY_CARD_ENROL_JOURNEY
import com.bink.wallet.utils.FirebaseEvents.ADD_LOYALTY_CARD_REQUEST
import com.bink.wallet.utils.FirebaseEvents.ADD_LOYALTY_CARD_RESPONSE_FAILURE
import com.bink.wallet.utils.FirebaseEvents.ADD_LOYALTY_CARD_RESPONSE_SUCCESS
import com.bink.wallet.utils.FirebaseEvents.ENROL_FORM_VIEW
import com.bink.wallet.utils.FirebaseEvents.FIREBASE_FALSE
import com.bink.wallet.utils.FirebaseEvents.FIREBASE_TRUE
import org.json.JSONObject
import org.koin.androidx.viewmodel.ext.android.viewModel
import retrofit2.HttpException

class GetNewCardFragment : BaseAddAuthFragment() {

    override val viewModel: GetNewCardViewModel by viewModel()

    private val getCardArgs: GetNewCardFragmentArgs by navArgs()

    private var membershipPlanId: String? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        isRetryJourney = getCardArgs.isRetryJourney
        membershipCardId = getCardArgs.membershipCardId
        currentMembershipPlan = getCardArgs.membershipPlan
        membershipPlanId = getCardArgs.membershipPlan.id

        setViewsContent()

        viewModel.newMembershipCard.observeNonNull(this) {
            handleNavigationAfterCardCreation(it)
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
                        ADD_LOYALTY_CARD_ENROL_JOURNEY, it.id, status, reasonCode, mPlanId, isAccountNew
                    )
                )
            }


        }
        binding.footerComposed.progressBtnContainer.setOnClickListener {
            logCTAClick(it)
            handleCtaRequest()
        }

        viewModel.addLoyaltyCardRequestMade.observeNonNull(this) {
            logMixpanelEvent(MixpanelEvents.LOYALTY_CARD_ADD, JSONObject().put(MixpanelEvents.BRAND_NAME, currentMembershipPlan?.account?.company_name ?: MixpanelEvents.VALUE_UNKNOWN))
            val mPlanId = membershipPlanId

            if (mPlanId == null) {
                failedEvent(ADD_LOYALTY_CARD_REQUEST)
            } else {
                val isScanned =
                    if (SharedPreferenceManager.isScannedCard) FIREBASE_TRUE else FIREBASE_FALSE

                logEvent(
                    ADD_LOYALTY_CARD_REQUEST, getAddLoyaltyCardRequestMap(
                        ADD_LOYALTY_CARD_ENROL_JOURNEY, mPlanId, isScanned
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

        viewModel.loading.observeNonNull(this) {
            binding.footerComposed.progressBtnContainer.setLoading(it)
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
        currentMembershipPlan?.let {
            val titleText = it.account?.plan_name ?: getString(R.string.sign_up_new_card_text)
            viewModel.titleText.set(getString(R.string.sign_up_enrol, titleText))
            viewModel.ctaText.set(getString(R.string.sign_up_text))
            viewModel.descriptionText.set(
                it.account?.plan_summary
            )
        }
        viewModel.isNoAccountFooter.set(false)
    }

    private fun logCTAClick(button: View) {
        logEvent(
            FirebaseEvents.getFirebaseIdentifier(
                ENROL_FORM_VIEW,
                (button as ProgressButton).getText().toString()
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
