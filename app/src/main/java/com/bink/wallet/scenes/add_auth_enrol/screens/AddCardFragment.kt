package com.bink.wallet.scenes.add_auth_enrol.screens

import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bink.wallet.R
import com.bink.wallet.modal.generic.GenericModalParameters
import com.bink.wallet.utils.EMPTY_STRING
import com.bink.wallet.utils.FirebaseEvents
import com.bink.wallet.utils.FirebaseEvents.ADD_AUTH_FORM_VIEW
import com.bink.wallet.utils.enums.SignUpFormType
import com.bink.wallet.utils.enums.TypeOfField
import com.bink.wallet.utils.navigateIfAdded

class AddCardFragment : BaseAddAuthFragment() {

    private var isRetryJourney = false
    private var isFromNoReasonCodes = false

    private val addCardArgs: AddCardFragmentArgs by navArgs()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        isRetryJourney = addCardArgs.isRetryJourney
        isFromNoReasonCodes = addCardArgs.isFromNoReasonCodes
        setViewsContent()

        binding.footerComposed.noAccount.setOnClickListener {
            navigateToGhostRegistrationUnavailableScreen()
            logNoAccountClick()
        }

        binding.footerComposed.addAuthCta.setOnClickListener { logCTAClick(it) }
        binding.footerSimple.addAuthCta.setOnClickListener { logCTAClick(it) }

        addItems()
    }

    override fun onResume() {
        super.onResume()
        logScreenView(ADD_AUTH_FORM_VIEW)
    }

    private fun setViewsContent() {
        viewModel.ctaText.set(retrieveCTAText())
        viewModel.titleText.set(retrieveTitleText())
        viewModel.descriptionText.set(retrieveDescriptionText())
        viewModel.isNoAccountFooter.set(!isFooterSimple())
    }

    private fun isFooterSimple() = isRetryJourney && !isFromNoReasonCodes

    private fun retrieveDescriptionText(): String {
        binding.membershipPlan?.let { membershipPlan ->
            if (isRetryJourney) {
                if (membershipPlan.areTransactionsAvailable()) {
                    membershipPlan.account?.plan_name?.let {
                        return getString(
                            R.string.log_in_transaction_available,
                            it
                        )
                    }
                } else {
                    membershipPlan.account?.plan_name_card?.let {
                        return getString(
                            R.string.log_in_transaction_unavailable,
                            it
                        )
                    }
                }
            } else {
                membershipPlan.account?.company_name?.let { companyName ->
                    return getString(
                        R.string.please_enter_credentials,
                        companyName
                    )
                }
            }
        }
        return EMPTY_STRING
    }

    private fun logNoAccountClick() {
        logEvent(
            FirebaseEvents.getFirebaseIdentifier(
                ADD_AUTH_FORM_VIEW,
                binding.footerComposed.noAccount.text.toString()
            )
        )
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

    private fun addItems() {
        with(binding.membershipPlan!!) {
            account?.let {
                account.add_fields?.map { planField ->
                    planField.typeOfField = TypeOfField.ADD
                    addPlanField(planField)
                }
                account.authorise_fields?.map { planField ->
                    planField.typeOfField = TypeOfField.AUTH
                    addPlanField(planField)
                }
                account.plan_documents?.map { planDocument ->
                    planDocument.display?.let { display ->
                        if (display.contains(SignUpFormType.ADD_AUTH.type)) {
                            addPlanDocument(planDocument)
                        }
                    }
                }
            }
        }
        mapItems()
    }

    private fun navigateToGhostRegistrationUnavailableScreen() {
        findNavController().navigateIfAdded(
            this,
            AddCardFragmentDirections.addCardToGhostRegistrationUnavailable(
                GenericModalParameters(
                    R.drawable.ic_close,
                    true,
                    getString(R.string.title_ghost_card_not_available),
                    getString(R.string.description_ghost_card_not_available)
                )
            )
        )
    }
}