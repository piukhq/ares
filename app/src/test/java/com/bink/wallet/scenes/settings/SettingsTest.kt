package com.bink.wallet.scenes.settings

import com.bink.wallet.R
import com.bink.wallet.scenes.login.LoginRepository
import com.bink.wallet.scenes.loyalty_wallet.LoyaltyWalletRepository
import com.bink.wallet.scenes.pll.PaymentWalletRepository
import junit.framework.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.mockito.Mock
import org.mockito.MockitoAnnotations

@RunWith(JUnit4::class)
class SettingsTest {

    @Mock
    private lateinit var loginRepository: LoginRepository
    @Mock
    private lateinit var loyaltyWalletRepository: LoyaltyWalletRepository
    @Mock
    private lateinit var paymentWalletRepository: PaymentWalletRepository

    private var settingsMockViewModel: SettingsViewModel? = null

    @Before
    fun setupTest() {
        MockitoAnnotations.initMocks(this)
        settingsMockViewModel =
            SettingsViewModel(loginRepository, loyaltyWalletRepository, paymentWalletRepository)
    }

    @Test
    fun test_settingsScreenTitle() {
        val actualResult = R.string.settings
        assertEquals(settingsMockViewModel?.getSettingsTitle(), actualResult)
    }


}