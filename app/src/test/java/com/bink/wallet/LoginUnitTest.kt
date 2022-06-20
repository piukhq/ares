package com.bink.wallet

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import com.bink.wallet.di.networkModule
import com.bink.wallet.di.viewModelModules
import com.bink.wallet.scenes.login.LoginBody
import com.bink.wallet.scenes.login.LoginRepository
import com.bink.wallet.scenes.login.LoginResponse
import com.bink.wallet.scenes.login.LoginViewModel
import com.bink.wallet.scenes.loyalty_wallet.wallet.LoyaltyWalletRepository
import com.bink.wallet.scenes.settings.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.newSingleThreadContext
import kotlinx.coroutines.test.resetMain
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.test.KoinTest
import org.mockito.Mockito.mock
import org.mockito.MockitoAnnotations

class LoginUnitTest : KoinTest {
    @Rule
    @JvmField
    val rule = InstantTaskExecutorRule()

    var loginData = MutableLiveData<LoginBody>()
    val authError = MutableLiveData<Exception>()
    private val loginRepository: LoginRepository = mock()
    private val loyaltyWalletRepository: LoyaltyWalletRepository = mock()
    private val userRepository: UserRepository = mock()
    var viewModel: LoginViewModel = mock()

    @ObsoleteCoroutinesApi
    private val mainThreadSurrogate = newSingleThreadContext("UI thread")

    @Before
    fun setup() {
        MockitoAnnotations.initMocks(this)
        viewModel = LoginViewModel(loginRepository, loyaltyWalletRepository, userRepository)
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
        val loginBody = LoginBody(
            System.currentTimeMillis() / 1000,
            "bink20iteration1@testbink.com",
            0.0,
            12.345
        )

        loginData
            .observeForever {
                Assert.assertEquals(loginBody, it)
            }

        loginRepository.doAuthenticationWork(LoginResponse(loginBody), loginData, authError)

    }
}