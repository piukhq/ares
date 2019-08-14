package com.bink.wallet.scenes.browse_brands

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.bink.wallet.data.MembershipPlanDao
import com.bink.wallet.network.ApiService
import com.bink.wallet.scenes.browse_brands.model.MembershipPlan
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class BrowseBrandsRepository(private val apiService: ApiService, private val plansDao: MembershipPlanDao) {

    private var mutableLiveData = MutableLiveData<List<MembershipPlan>>()
    private var localMutableLiveData = MutableLiveData<List<MembershipPlan>>()

    fun fetchMembershipPlans(): MutableLiveData<List<MembershipPlan>> {
        CoroutineScope(Dispatchers.IO).launch {
            val request = apiService.getMembershipPlansAsync()
            withContext(Dispatchers.Main) {
                try {
                    val response = request.await()
                    mutableLiveData.value = response.toMutableList()
                    plansDao.storeAll(response)
                } catch (e: Throwable) {
                    Log.e(BrowseBrandsRepository::class.simpleName, e.toString())
                }
            }
        }
        return mutableLiveData
    }

    fun getStoredMembershipPlans(): MutableLiveData<List<MembershipPlan>> {
        CoroutineScope(Dispatchers.IO).launch {
            withContext(Dispatchers.Main) {
                try {
                    val response = plansDao.getAllAsync()
                    localMutableLiveData.value = response.toMutableList()
                } catch (e: Throwable) {
                    Log.e(BrowseBrandsRepository::class.simpleName, e.toString())
                }
            }
        }
        return localMutableLiveData
    }
}