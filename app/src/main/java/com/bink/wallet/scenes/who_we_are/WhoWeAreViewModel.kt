package com.bink.wallet.scenes.who_we_are

import android.content.res.Resources
import androidx.lifecycle.MutableLiveData
import com.bink.wallet.BaseViewModel
import com.bink.wallet.R

class WhoWeAreViewModel : BaseViewModel() {

    private val _nameList = MutableLiveData<ArrayList<String>>()
    val nameList: MutableLiveData<ArrayList<String>> get() = _nameList

    fun populateNames(res: Resources){
        nameList.value = ArrayList<String>().also {
            it.add(res.getString(R.string.name_paul))
            it.add(res.getString(R.string.name_enoch))
            it.add(res.getString(R.string.name_josh))
            it.add(res.getString(R.string.name_susanne))
            it.add(res.getString(R.string.name_srikalyani))
            it.add(res.getString(R.string.name_marius))
            it.add(res.getString(R.string.name_connor))
            it.add(res.getString(R.string.name_carmen))
            it.add(res.getString(R.string.name_tedodora))
            it.add(res.getString(R.string.name_mara))
            it.add(res.getString(R.string.name_max))
        }
    }

}