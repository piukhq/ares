package com.bink.wallet.utils.LocalPointScraping

import com.bink.wallet.data.WebScrapeDao
import com.bink.wallet.utils.logDebug
import kotlinx.coroutines.*

class WebScrapeRepository(private val webScrapeDao: WebScrapeDao) {

    suspend fun getWebScrapeCredentials(): List<WebScrapeCredentials>? {
        return coroutineScope {
            val webScrapeCredentialsRequest = async { webScrapeDao.getWebScrapeCredentials() }
            webScrapeCredentialsRequest.await()
        }
    }

    fun storeWebScrapeCredentials(webScrapeCredentials: WebScrapeCredentials) {
        CoroutineScope(Dispatchers.IO).launch {
            withContext(Dispatchers.Main) {
                try {
                    webScrapeDao.storeWebScrapeCredentials(webScrapeCredentials)
                } catch (e: Exception) {
                    logDebug(WebScrapeRepository::class.simpleName, e.toString())
                }
            }
        }
    }

    fun deleteWebScrapeCredentials(membershipPlanId: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            withContext(Dispatchers.Main) {
                try {
                    webScrapeDao.deleteWebScrapeCredentials(membershipPlanId)
                } catch (e: Exception) {
                    logDebug(WebScrapeRepository::class.simpleName, e.toString())
                }
            }
        }
    }

}