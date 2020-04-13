package com.bink.wallet.scenes.add_auth_enrol.screens

import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.navigation.fragment.navArgs
import com.bink.wallet.R
import com.bink.wallet.scenes.add_auth_enrol.view_models.GetNewCardViewModel
import com.bink.wallet.utils.FirebaseEvents
import com.bink.wallet.utils.FirebaseEvents.ENROL_FORM_VIEW
import com.bink.wallet.utils.observeNonNull
import org.koin.androidx.viewmodel.ext.android.viewModel

class GetNewCardFragment : BaseAddAuthFragment() {

    override val viewModel: GetNewCardViewModel by viewModel()

    private val getCardArgs: GetNewCardFragmentArgs by navArgs()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        isRetryJourney = getCardArgs.isRetryJourney
        membershipCardId = getCardArgs.membershipCardId
        currentMembershipPlan = getCardArgs.membershipPlan

        setViewsContent()

        viewModel.newMembershipCard.observeNonNull(this) {
            handleNavigationAfterCardCreation(it, false)
        }
        binding.footerSimple.addAuthCta.setOnClickListener {
            logCTAClick(it)
            handleCtaRequest()
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
