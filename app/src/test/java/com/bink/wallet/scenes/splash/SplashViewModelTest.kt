package com.bink.wallet.scenes.splash

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import com.bink.wallet.model.auth.User
import com.bink.wallet.scenes.login.LoginRepository
import com.bink.wallet.scenes.login.LoginViewModel
import com.bink.wallet.scenes.loyalty_wallet.wallet.LoyaltyWalletRepository
import com.bink.wallet.scenes.settings.UserRepository
import com.bink.wallet.testrules.CoroutineTestRule
import com.nhaarman.mockitokotlin2.verify
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)

class SplashViewModelTest {

    @Rule
    @JvmField
    val instanceTaskExecutorRule = InstantTaskExecutorRule()

    @Rule
    @JvmField
    val coroutineTestRule = CoroutineTestRule()

    @Mock
    private lateinit var loginRepository: LoginRepository
    @Mock
    private lateinit var userRepository: UserRepository

    @Mock
    private lateinit var userResponseObserver: Observer<User>

    private lateinit var splashViewModel: SplashViewModel

    private val user = User("Hello", "World", "123")

    @Before
    fun setup() {
        splashViewModel = SplashViewModel(
            loginRepository,
            userRepository
        )

        splashViewModel.getUserResponse.observeForever(userResponseObserver)
    }

    @Test
    fun `Get user success`() = runBlocking {
        Mockito.`when`(userRepository.getUserDetails()).thenReturn(user)
        splashViewModel.getCurrentUser()
        verify(userResponseObserver).onChanged(user)
    }

}