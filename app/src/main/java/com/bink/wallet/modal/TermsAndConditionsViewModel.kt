package com.bink.wallet.modal

import com.bink.wallet.R
import com.bink.wallet.modal.generic.BaseModalViewModel

class TermsAndConditionsViewModel(private val repository: TermsAndConditionsRepository) :
    BaseModalViewModel() {
    override fun onFirstButtonClicked() {
        //TODO: Create the payment card when structure and API calls are ready.
    }

    override fun onSecondButtonClicked() {
        destinationLiveData.value = R.id.global_to_home
    }
}