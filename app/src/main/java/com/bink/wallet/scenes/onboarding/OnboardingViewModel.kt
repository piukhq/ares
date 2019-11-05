package com.bink.wallet.scenes.onboarding

import androidx.lifecycle.MutableLiveData
import com.bink.wallet.modal.generic.BaseModalViewModel

class OnboardingViewModel: BaseModalViewModel() {
    val pageId = MutableLiveData<Int>()
}