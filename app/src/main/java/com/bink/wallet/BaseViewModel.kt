package com.bink.wallet

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bink.wallet.utils.SingleLiveEvent
import com.bink.wallet.utils.convertToBaseException
import kotlinx.coroutines.*
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import kotlin.coroutines.CoroutineContext

abstract class BaseViewModel : ViewModel(), CoroutineScope {
    private val job = SupervisorJob()

    private val noInternetConnectionEvent = SingleLiveEvent<Unit>()
    private val connectTimeoutEvent = SingleLiveEvent<Unit>()

    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        viewModelScope.launch {
            onLoadFail(throwable)
        }
    }

    protected val viewModelScopeExceptionHandler = viewModelScope + exceptionHandler

    override val coroutineContext: CoroutineContext
        get() = job + Dispatchers.Default

    override fun onCleared() {
        super.onCleared()
        coroutineContext.cancel()
    }

    open suspend fun onLoadFail(throwable: Throwable) {
        withContext(Dispatchers.Main) {
            when (throwable) {
                is UnknownHostException -> {
                    noInternetConnectionEvent.call()
                }
                is SocketTimeoutException -> {
                    connectTimeoutEvent.call()
                }
                else -> {
                    convertToBaseException(throwable)
                }
            }
        }
    }
}