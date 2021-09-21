package com.bink.wallet.scenes.add_auth_enrol.screens

import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.navigation.fragment.navArgs
import com.bink.wallet.R
import com.bink.wallet.data.SharedPreferenceManager
import com.bink.wallet.scenes.add_auth_enrol.view_models.GhostCardViewModel
import com.bink.wallet.utils.FirebaseEvents
import com.bink.wallet.utils.FirebaseEvents.ADD_LOYALTY_CARD_REGISTER_JOURNEY
import com.bink.wallet.utils.FirebaseEvents.ADD_LOYALTY_CARD_REQUEST
import com.bink.wallet.utils.FirebaseEvents.ADD_LOYALTY_CARD_RESPONSE_FAILURE
import com.bink.wallet.utils.FirebaseEvents.ADD_LOYALTY_CARD_RESPONSE_SUCCESS
import com.bink.wallet.utils.FirebaseEvents.FIREBASE_FALSE
import com.bink.wallet.utils.FirebaseEvents.FIREBASE_TRUE
import com.bink.wallet.utils.FirebaseEvents.REGISTRATION_FORM_VIEW
import com.bink.wallet.utils.getErrorBody
import com.bink.wallet.utils.observeNonNull
import org.koin.androidx.viewmodel.ext.android.viewModel
import retrofit2.HttpException

class GhostCardFragment : BaseAddAuthFragment() {

    override val viewModel: GhostCardViewModel by viewModel()
    private val ghostCardArgs: GhostCardFragmentArgs by navArgs()
    private var membershipPlanId: String? = null


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        membershipCardId = ghostCardArgs.membershipCardId
        isRetryJourney = ghostCardArgs.isRetryJourney
        currentMembershipPlan = ghostCardArgs.membershipPlan
        membershipPlanId = ghostCardArgs.membershipPlan.id


        setViewsContent()

        binding.footerComposed.progressBtnContainer.setOnClickListener {
            logCTAClick(it)
            handleCtaRequest()
        }

        viewModel.newMembershipCard.observeNonNull(this) {
            currentMembershipPlan?.let { plan ->
                if (!isRetryJourney) {
                    viewModel.createGhostCardRequest(it.id, plan)
                }
            }
            handleNavigationAfterCardCreation(it, true)
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
                        ADD_LOYALTY_CARD_REGISTER_JOURNEY,it.id,
                        status,
                        reasonCode,
                        mPlanId,
                        isAccountNew
                    )
                )
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
                        ADD_LOYALTY_CARD_REGISTER_JOURNEY, mPlanId, isScanned
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
                        ADD_LOYALTY_CARD_REGISTER_JOURNEY, mPlanId, httpException.code(), httpException.getErrorBody()
                    )
                )
            }

        }

        viewModel.loading.observeNonNull(this){
            binding.footerComposed.progressBtnContainer.setLoading(it)
        }
    }

    override fun onResume() {
        super.onResume()
        currentMembershipPlan?.let {
            viewModel.addItems(it)
        }
        logScreenView(REGISTRATION_FORM_VIEW)
    }

    private fun setViewsContent() {
        viewModel.titleText.set(getString(R.string.register_ghost_card_title))
        viewModel.ctaText.set(getString(R.string.register_ghost_card_button))
        viewModel.isNoAccountFooter.set(false)
    }

    private fun logCTAClick(button: View) {
        logEvent(
            FirebaseEvents.getFirebaseIdentifier(
                REGISTRATION_FORM_VIEW,
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