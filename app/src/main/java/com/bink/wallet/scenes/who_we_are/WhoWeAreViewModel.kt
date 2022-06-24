package com.bink.wallet.scenes.who_we_are

import android.content.res.Resources
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.bink.wallet.BaseViewModel
import com.bink.wallet.R


class WhoWeAreViewModel : BaseViewModel() {

    private val _nameList = MutableLiveData<Array<String>>()
    val nameList: LiveData<Array<String>> get() = _nameList

    fun populateNames(res: Resources) {
        _nameList.value = res.getStringArray(R.array.dev_names)
    }

}