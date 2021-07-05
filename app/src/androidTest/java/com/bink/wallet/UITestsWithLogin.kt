package com.bink.wallet


import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.*
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.filters.LargeTest
import androidx.test.rule.ActivityTestRule
import androidx.test.runner.AndroidJUnit4
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@LargeTest
@RunWith(AndroidJUnit4::class)
class UITestsWithLogin {

    @Rule
    @JvmField
    var mActivityTestRule = ActivityTestRule(MainActivity::class.java)

    @Before
    fun before() {
        logInTest()
    }

    fun logInTest() {
        val logInBtn = onView(withId(R.id.log_in_email))
        logInBtn.perform(click())

        val emailEt = onView(withId(R.id.email_field))
        emailEt.perform(replaceText("jbest@bink.com"), closeSoftKeyboard())
        emailEt.perform(pressImeActionButton())

        val passwordEt = onView(withId(R.id.password_field))
        passwordEt.perform(replaceText("Password01"), closeSoftKeyboard())
        passwordEt.perform(pressImeActionButton())

        val continueBtn = onView(withId(R.id.log_in_button))
        continueBtn.perform(click())
    }

    @Test
    fun showBarcode() {
        val cardView = onView(withId(R.id.card_item))
        cardView.perform(click())

        val loyaltyCardHeader = onView(withId(R.id.card_header))
        loyaltyCardHeader.perform(click())
    }

}
