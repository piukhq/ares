package com.bink.wallet.scenes.browse_brands

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.bink.wallet.network.ApiService
import com.bink.wallet.scenes.browse_brands.model.MembershipPlan
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class BrowseBrandsRepository(val apiService: ApiService) {

    private var mutableLiveData = MutableLiveData<List<MembershipPlan>>()

    fun fetchMembershipPlans(): MutableLiveData<List<MembershipPlan>> {
        CoroutineScope(Dispatchers.IO).launch {
            val request = apiService.getMembershipPlans()
            withContext(Dispatchers.Main) {
                try {
                    val response = request.await()
                    mutableLiveData.value = response.toMutableList()
                } catch (e: Throwable) {
                    Log.e(BrowseBrandsRepository::class.simpleName, e.toString())
                }
            }
        }
        return mutableLiveData
    }
}