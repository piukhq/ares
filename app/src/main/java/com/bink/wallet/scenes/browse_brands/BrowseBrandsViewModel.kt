package com.bink.wallet.scenes.browse_brands

import androidx.lifecycle.MutableLiveData
import com.bink.wallet.BaseViewModel

class BrowseBrandsViewModel : BaseViewModel() {
    val searchText = MutableLiveData<String>()
}
