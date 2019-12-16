package com.bink.wallet

import androidx.lifecycle.MutableLiveData
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

    val noInternetConnectionEvent = SingleLiveEvent<Unit>()
    val connectTimeoutEvent = SingleLiveEvent<Unit>()

    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        viewModelScope.launch {
            onLoadFail(throwable)
        }
    }

    val errorCode = MutableLiveData<Int>()

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
                    val baseException = convertToBaseException(throwable)

                    errorCode.value = baseException.httpCode

                    //TODO Logic example for error handling, for now just unauthorised needed
//                    when (baseException.httpCode) {
//                        HttpURLConnection.HTTP_UNAUTHORIZED -> {
//                            errorMessage.value = baseException.message
//                        }
//                        HttpURLConnection.HTTP_INTERNAL_ERROR -> {
//                            errorMessage.value = baseException.message
//                        }
//                        else -> {
//                            errorMessage.value = baseException.message
//                        }
//                    }
                }
            }

            //TODO Should be implemented something to disable loading spinner and here it should be dismissed
        }
    }
}