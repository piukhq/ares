package com.bink.wallet.scenes.add_auth_enrol

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.navArgs
import com.bink.wallet.R

class AddCardFragment : BaseAddAuthFragment() {

    private var isRetryJourney = false

    private val addCardArgs: AddCardFragmentArgs by navArgs()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        isRetryJourney = addCardArgs.isRetryJourney
        setViewsContent(isRetryJourney)
    }


    private fun setViewsContent(isRetryJourney: Boolean) {
        viewModel.ctaText.set(retrieveCTAText(isRetryJourney))
        viewModel.titleText.set(retrieveTitleText(isRetryJourney))
        viewModel.descriptionText.set(retrieveDescriptionText(isRetryJourney))
    }

    private fun retrieveDescriptionText(isRetryJourney: Boolean): String {
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
        return ""
    }

    private fun retrieveCTAText(isRetryJourney: Boolean) = if (isRetryJourney) {
        getString(R.string.log_in_text)
    } else {
        getString(R.string.add_card)
    }

    private fun retrieveTitleText(isRetryJourney: Boolean) = if (isRetryJourney) {
        getString(R.string.log_in_text)
    } else {
        getString(R.string.enter_credentials)
    }
}