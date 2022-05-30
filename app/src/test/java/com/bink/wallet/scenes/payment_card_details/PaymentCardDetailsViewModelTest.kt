package com.bink.wallet.scenes.payment_card_details

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.bink.wallet.model.auth.User
import com.bink.wallet.scenes.loyalty_wallet.wallet.LoyaltyWalletRepository
import com.bink.wallet.scenes.pll.PaymentWalletRepository
import com.bink.wallet.scenes.settings.UserRepository
import com.bink.wallet.testrules.CoroutineTestRule
import kotlinx.coroutines.runBlocking
import androidx.lifecycle.Observer
import com.nhaarman.mockitokotlin2.verify
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)

class PaymentCardDetailsViewModelTest {
    @Rule
    @JvmField
    val instanceTaskExecutorRule = InstantTaskExecutorRule()

    @Rule
    @JvmField
    val coroutineTestRule = CoroutineTestRule()

    @Mock
    private lateinit var paymentWalletRepository: PaymentWalletRepository

    @Mock
    private lateinit var loyaltyWalletRepository: LoyaltyWalletRepository

    @Mock
    private lateinit var zendeskRepository: ZendeskRepository

    @Mock
    private lateinit var userRepository: UserRepository

    @Mock
    private lateinit var userResponseObserver: Observer<User>

    private lateinit var paymentCardsDetailsViewModel: PaymentCardsDetailsViewModel

    private val user = User("Hello", "World", "123")

    @Before
    fun setup() {
        paymentCardsDetailsViewModel = PaymentCardsDetailsViewModel(
            paymentWalletRepository,
            loyaltyWalletRepository,
            zendeskRepository,
            userRepository
        )

        paymentCardsDetailsViewModel.userResponse.observeForever(userResponseObserver)
    }

    @Test
    fun `Put User Details Success`() = runBlocking {
        Mockito.`when`(userRepository.putUserDetails(user)).thenReturn(user)
        paymentCardsDetailsViewModel.putUserDetails(user)
        verify(userResponseObserver).onChanged(user)
    }


}