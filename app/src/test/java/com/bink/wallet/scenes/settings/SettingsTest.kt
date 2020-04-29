package com.bink.wallet.scenes.settings

import android.content.Context
import android.os.Build
import androidx.test.core.app.ApplicationProvider
import com.bink.wallet.model.SettingsItem
import com.bink.wallet.scenes.login.LoginRepository
import com.bink.wallet.scenes.loyalty_wallet.LoyaltyWalletRepository
import com.bink.wallet.scenes.pll.PaymentWalletRepository
import junit.framework.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.test.AutoCloseKoinTest
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.P])
class SettingsTest : AutoCloseKoinTest() {

    val context = ApplicationProvider.getApplicationContext<Context>()
    @Mock
    private lateinit var loginRepository: LoginRepository
    @Mock
    private lateinit var loyaltyWalletRepository: LoyaltyWalletRepository
    @Mock
    private lateinit var paymentWalletRepository: PaymentWalletRepository

    private lateinit var settingsMockViewModel: SettingsViewModel
    private var settingsItemList = mutableListOf<SettingsItem>()

    @Before
    fun setupTest() {
        MockitoAnnotations.initMocks(this)
        settingsMockViewModel =
            SettingsViewModel(loginRepository, loyaltyWalletRepository, paymentWalletRepository)

        for (item in SettingsItemsPopulation.populateItems(context.resources)) {
            settingsItemList.add(item)
        }
    }



    @Test
    fun test_settingsScreenTitle() {
        assertEquals("Settings", context.getString(settingsMockViewModel.getSettingsTitle()))
    }

//    @After
//    fun tearDown() {
//        stopKoin()
//    }



    @Test
    fun test_accountHeaderTitle() {
        assertEquals("Account", settingsItemList[0].title)
    }

    @Test
    fun test_preferencesTitleRow() {
        assertEquals("Preferences", settingsItemList[1].title)
    }

    @Test
    fun test_logOutTitleRow() {
        assertEquals("Log out", settingsItemList[2].title)
    }

    @Test
    fun test_supportAndFeedbackHeaderTitle() {
        assertEquals("Support and feedback", settingsItemList[3].title)
    }

    @Test
    fun test_faqsTitleRow() {
        assertEquals("FAQs", settingsItemList[4].title)
    }

    @Test
    fun test_faqsDescriptionRow() {
        assertEquals("Frequently asked questions", settingsItemList[4].value)
    }

    @Test
    fun test_contactUsTitleRow() {
        assertEquals("Contact us", settingsItemList[5].title)
    }

    @Test
    fun test_contactUsDescriptionRow() {
        assertEquals("Get in touch with Bink", settingsItemList[5].value)
    }

    @Test
    fun test_rateTitleRow() {
        assertEquals("Rate this app", settingsItemList[6].title)
    }
}