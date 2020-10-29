package com.bink.wallet.scenes.who_we_are

import android.content.res.Resources
import androidx.lifecycle.MutableLiveData
import com.bink.wallet.BaseViewModel
import com.bink.wallet.R


class WhoWeAreViewModel : BaseViewModel() {

    private val _nameList = MutableLiveData<Array<String>>()
    val nameList: MutableLiveData<Array<String>> get() = _nameList

    fun populateNames(res: Resources){
        nameList.value = res.getStringArray(R.array.dev_names)
    }

}