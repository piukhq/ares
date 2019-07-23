package com.bink.wallet

import com.bink.wallet.network.ApiService
import com.bink.wallet.scenes.login.LoginBody
import com.bink.wallet.scenes.login.LoginResponse
import com.bink.wallet.scenes.login.LoginWorker
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers
import org.mockito.Mockito

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class LoginUnitTest {

    lateinit var apiService: ApiService

    @Before
    fun setUp() {
        apiService = Mockito.mock(ApiService::class.java)
    }

    @Test
    fun testWorker() {
        Mockito.`when`(apiService.loginOrRegister(ArgumentMatchers.any(LoginBody::class.java))).thenAnswer {
            LoginResponse(
                LoginBody(
                    1517549941,
                    "launchpad@bink.com"
                )
            )
        }

        val worker = LoginWorker(apiService)
    }

}
