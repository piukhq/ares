package com.bink.wallet.scenes.add_auth_enrol.screens

import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.navigation.fragment.navArgs
import com.bink.wallet.R
import com.bink.wallet.scenes.add_auth_enrol.view_models.GhostCardViewModel
import com.bink.wallet.utils.FirebaseEvents
import com.bink.wallet.utils.FirebaseEvents.REGISTRATION_FORM_VIEW
import com.bink.wallet.utils.displayModalPopup
import com.bink.wallet.utils.observeNonNull
import org.koin.androidx.viewmodel.ext.android.viewModel

class GhostCardFragment : BaseAddAuthFragment() {

    override val viewModel: GhostCardViewModel by viewModel()
    private val ghostCardArgs: GhostCardFragmentArgs by navArgs()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        membershipCardId = ghostCardArgs.membershipCardId
        isRetryJourney = ghostCardArgs.isRetryJourney
        currentMembershipPlan = ghostCardArgs.membershipPlan

        setViewsContent()
        currentMembershipPlan?.let {
            viewModel.addItems(it)
        }
        binding.footerSimple.addAuthCta.setOnClickListener {
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
        }
    }

    override fun onResume() {
        super.onResume()
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
        if (viewModel.addRegisterFieldsRequest.value?.add_fields.isNullOrEmpty()) {
            context?.displayModalPopup(
                null,
                getString(R.string.cannot_complete_registration)
            )
        }
        membershipCardId?.let {
            currentMembershipPlan?.let { plan ->
                viewModel.handleRequest(isRetryJourney, it, plan)
            }
        }
    }
}