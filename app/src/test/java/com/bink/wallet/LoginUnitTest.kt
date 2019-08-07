package com.bink.wallet

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.bink.wallet.di.networkModule
import com.bink.wallet.di.viewModelModules
import com.bink.wallet.scenes.login.LoginBody
import com.bink.wallet.scenes.login.LoginRepository
import com.bink.wallet.scenes.login.LoginResponse
import com.bink.wallet.scenes.login.LoginViewModel
import com.nhaarman.mockitokotlin2.mock
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.newSingleThreadContext
import kotlinx.coroutines.test.resetMain
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.test.KoinTest
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.MockitoAnnotations

class LoginUnitTest : KoinTest {
    @Rule
    @JvmField
    val rule = InstantTaskExecutorRule()

    var loginData: MutableLiveData<LoginBody> = MutableLiveData()
    private val loginRepository: LoginRepository = mock()
    lateinit var viewModel: LoginViewModel
    var apiResponseObserver: Observer<LoginBody?> = mock()

    @ObsoleteCoroutinesApi
    private val mainThreadSurrogate = newSingleThreadContext("UI thread")

    @Before
    fun setup() {
        MockitoAnnotations.initMocks(this)
        viewModel = LoginViewModel(loginRepository)
        viewModel.loginData.observeForever(apiResponseObserver)
    }

    @ExperimentalCoroutinesApi
    @ObsoleteCoroutinesApi
    @After
    fun tearDown() {
        Dispatchers.resetMain()
        mainThreadSurrogate.close()
    }

    @Test
    fun `make a test with Koin`() {
        startKoin {
            androidContext(mock(Context::class.java))
            modules(listOf(viewModelModules, networkModule))
        }
    }

    @Test
    fun testLiveDataUpdate() {
        var loginBody = LoginBody(
            System.currentTimeMillis() / 1000,
            "bink20iteration1@testbink.com",
            0.0,
            12.345
        )

        loginRepository.doAuthenticationWork(LoginResponse(loginBody))

        loginData.value = loginBody
        verify(apiResponseObserver).onChanged(
            LoginBody(
                System.currentTimeMillis() / 1000,
                "bink20iteration1@testbink.com",
                0.0,
                12.345
            )
        )
    }
}