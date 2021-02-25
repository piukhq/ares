package com.bink.wallet.utils.LocalPointScraping

import com.bink.wallet.data.WebScrapeDao
import com.bink.wallet.utils.logDebug
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class WebScrapeRepository() {

    fun getWebScrapeCredentials(callback: (List<WebScrapeCredentials>?) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
//                val webScrapeCredentials = webScrapeDao.getWebScrapeCredentials()
                withContext(Dispatchers.Main) {
                    callback(webScrapeCredentials)
                }
            } catch (e: Exception) {
                callback(null)
            }
        }
    }

    fun storeWebScrapeCredentials(webScrapeCredentials: WebScrapeCredentials) {
        CoroutineScope(Dispatchers.IO).launch {
            withContext(Dispatchers.Main) {
                try {
//                    webScrapeDao.storeWebScrapeCredentials(webScrapeCredentials)
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