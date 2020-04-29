package com.bink.wallet.scenes.settings

import android.content.Context
import android.os.Build
import androidx.test.core.app.ApplicationProvider
import com.bink.wallet.BuildConfig
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

    private val context = ApplicationProvider.getApplicationContext<Context>()
    
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

    @Test
    fun test_sizeOfSettingsItems() {
        assertEquals(15, settingsItemList.size)
    }

    // Account Section Tests

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

    // Support and Feedback Section Tests

    @Test
    fun test_supportAndFeedbackHeaderTitle() {
        assertEquals("Support and feedback", settingsItemList[3].title)
    }

    @Test
    fun test_faqsTitleRow() {
        assertEquals("FAQs", settingsItemList[4].title)
    }

    @Test
    fun test_binkFAQUrl() {
        assertEquals(
            "https://help.bink.com",
            settingsItemList[4].url
        )
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

    // About Section Tests

    @Test
    fun test_aboutHeaderTitle() {
        assertEquals("About", settingsItemList[7].title)
    }

    @Test
    fun test_securityAndPrivacyTitleRow() {
        assertEquals("Security and privacy", settingsItemList[8].title)
    }

    @Test
    fun test_securityAndPrivacyDescriptionRow() {
        assertEquals("How we protect your data", settingsItemList[8].value)
    }

    @Test
    fun test_howItWorksTitleRow() {
        assertEquals("How it works", settingsItemList[9].title)
    }

    @Test
    fun test_howItWorksDescriptionRow() {
        assertEquals("Find out more about Bink", settingsItemList[9].value)
    }

    // Legal Section Tests

    @Test
    fun test_legalHeaderTitle() {
        assertEquals("Legal", settingsItemList[10].title)
    }

    @Test
    fun test_privacyPolicyTitleRow() {
        assertEquals("Privacy policy", settingsItemList[11].title)
    }

    @Test
    fun test_binkPrivacyPolicyUrl() {
        assertEquals(
            "https://bink.com/privacy-policy/",
            settingsItemList[11].url
        )
    }

    @Test
    fun test_termsAndConditionsTitleRow() {
        assertEquals("Terms and conditions", settingsItemList[12].title)
    }

    @Test
    fun test_binkTermsAndConditionsUrl() {
        assertEquals(
            "https://bink.com/terms-and-conditions/",
            settingsItemList[12].url
        )
    }

    // App Url Tests

    @Test
    fun test_playStoreAppUrl() {
        assertEquals(
            "market://details?id=" + BuildConfig.APPLICATION_ID,
            context.getString(
                settingsMockViewModel.getPlayStoreAppUrl(),
                BuildConfig.APPLICATION_ID
            )
        )
    }

    @Test
    fun test_playStoreBrowserUrl() {
        assertEquals(
            "https://play.google.com/store/apps/details?id=" + BuildConfig.APPLICATION_ID,
            context.getString(
                settingsMockViewModel.getPlayStoreBrowserUrl(),
                BuildConfig.APPLICATION_ID
            )
        )
    }
}