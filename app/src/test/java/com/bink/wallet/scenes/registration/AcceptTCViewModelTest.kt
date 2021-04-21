package com.bink.wallet.scenes.registration

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import com.bink.wallet.model.auth.User
import com.bink.wallet.scenes.login.LoginRepository
import com.bink.wallet.scenes.loyalty_wallet.wallet.LoyaltyWalletRepository
import com.bink.wallet.scenes.settings.UserRepository
import com.bink.wallet.testrules.CoroutineTestRule
import com.nhaarman.mockitokotlin2.given
import com.nhaarman.mockitokotlin2.verify
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner
import kotlin.RuntimeException

@RunWith(MockitoJUnitRunner::class)

class AcceptTCViewModelTest {

    @Rule
    @JvmField
    val instanceTaskExecutorRule = InstantTaskExecutorRule()

    @Rule
    @JvmField
    val coroutineTestRule = CoroutineTestRule()

    @Mock
    private lateinit var loginRepository: LoginRepository

    @Mock
    private lateinit var loyaltyWalletRepository: LoyaltyWalletRepository

    @Mock
    private lateinit var userRepository: UserRepository

    @Mock
    private lateinit var userResponseObserver: Observer<User>

    @Mock
    private lateinit var userReturnedObserver: Observer<Boolean>

    private lateinit var acceptTCViewModel: AcceptTCViewModel

    private val user = User("Hello", "World", "123")

    @Before
    fun setup() {
        acceptTCViewModel = AcceptTCViewModel(
            loginRepository,
            loyaltyWalletRepository,
            userRepository
        )

        acceptTCViewModel.getUserResponse.observeForever(userResponseObserver)
        acceptTCViewModel.userReturned.observeForever(userReturnedObserver)
    }

    @Test
    fun `Get user success`() = runBlocking {
        Mockito.`when`(userRepository.getUserDetails()).thenReturn(user)
        acceptTCViewModel.getCurrentUser()
        verify(userResponseObserver).onChanged(user)
    }

    @Test
    fun `Get user failure`() = runBlocking {
        Mockito.`when`(userRepository.getUserDetails()).thenThrow(RuntimeException())
        acceptTCViewModel.getCurrentUser()
        verify(userReturnedObserver).onChanged(false)
    }

}