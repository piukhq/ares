package com.bink.wallet

import com.bink.wallet.network.ApiService
import com.bink.wallet.scenes.login.LoginBody
import com.bink.wallet.scenes.login.LoginInteractor
import com.bink.wallet.scenes.login.LoginResponse
import com.bink.wallet.scenes.login.LoginWorker
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.newSingleThreadContext
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.*
import retrofit2.Response

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class LoginUnitTest {

    lateinit var apiService: ApiService
    lateinit var loginInteractor: LoginInteractor
    private val mainThreadSurrogate = newSingleThreadContext("UI thread")

    @Before
    fun setUp() {
        apiService = mock(ApiService::class.java)
        loginInteractor = mock(LoginInteractor::class.java)
        Dispatchers.setMain(mainThreadSurrogate)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        mainThreadSurrogate.close()
    }

    @Test
    fun testWorker_withValidAnswer() {
        val loginBody = LoginBody(
            1517549941,
            "launchpad@bink.com"
        )
        val loginResponse = LoginResponse(
            loginBody
        )
        val response = Response.success(loginResponse)
        `when`(apiService.loginOrRegister(loginBody)).thenReturn(CompletableDeferred(response))

        val worker = LoginWorker(apiService, loginInteractor)
        worker.doAuthenticationWork()

        verify(loginInteractor, never()).showErrorMessage(anyString())
        verify(loginInteractor, times(1)).successfulResponse(com.nhaarman.mockitokotlin2.any())
    }
}
