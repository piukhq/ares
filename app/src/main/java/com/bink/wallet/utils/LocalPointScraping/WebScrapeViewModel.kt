package com.bink.wallet.utils.LocalPointScraping

import com.bink.wallet.BaseViewModel

class WebScrapeViewModel(private val repository: WebScrapeRepository) : BaseViewModel() {

    fun storeWebScrapeCredentials(webScrapeCredentials: WebScrapeCredentials) {
        repository.storeWebScrapeCredentials(webScrapeCredentials)
    }

    fun getWebScrapeCredentials(callback: (List<WebScrapeCredentials>?) -> Unit) {
        repository.getWebScrapeCredentials(callback)
    }

}