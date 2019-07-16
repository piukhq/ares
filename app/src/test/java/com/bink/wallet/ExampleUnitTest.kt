package com.bink.wallet

import com.bink.wallet.network.ApiService
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class LoginUnitTest {

    lateinit var apiService: ApiService

    @Before
    fun setUp(){
        apiService = Mockito.mock(ApiService::class.java)
    }

    @Test
    fun registerCustomer(){
        Mockito.`when`(apiService.registerCustomer())
    }

}
