package com.bink.wallet

import android.os.SystemClock
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.NoMatchingViewException
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.filters.LargeTest
import androidx.test.rule.ActivityTestRule
import androidx.test.runner.AndroidJUnit4
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.Matchers
import org.hamcrest.TypeSafeMatcher
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
        try {
            val logInBtn = onView(withId(R.id.log_in_email))
            logInBtn.perform(click())

            val emailEt = onView(withId(R.id.email_field))
            emailEt.perform(replaceText("joshuitest@bink.com"), closeSoftKeyboard())
            emailEt.perform(pressImeActionButton())

            val passwordEt = onView(withId(R.id.password_field))
            passwordEt.perform(replaceText("Password01"), closeSoftKeyboard())
            passwordEt.perform(pressImeActionButton())

            val continueBtn = onView(withId(R.id.log_in_button))
            continueBtn.perform(click())
        } catch (e: NoMatchingViewException) {
        }
    }

    @Test
    fun showBarcode() {
        val cardView = onView(withId(R.id.card_item))
        cardView.perform(click())

        val loyaltyCardHeader = onView(withId(R.id.card_header))
        loyaltyCardHeader.perform(click())
    }

    @Test
    fun zendeskFaqs() {
        val settingsButton = onView(withId(R.id.settings_button))
        settingsButton.perform(click())

        val settingsList = onView(withId(R.id.settings_container))
        settingsList.perform(RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(4, click()))
    }

    @Test
    fun addCard() {
        val addMenuBtn = onView(withId(R.id.add_menu_item))
        addMenuBtn.perform(click())

        val browseBrandsBtn = onView(withId(R.id.browse_brands_container))
        browseBrandsBtn.perform(scrollTo(), click())

        val searchEt = onView(withId(R.id.input_search))
        searchEt.perform(replaceText("harvey"), closeSoftKeyboard())

        //Allows animation to finish
        SystemClock.sleep(500)

        val brandsRecyclerView = onView(withId(R.id.brands_recycler_view))
        brandsRecyclerView.perform(RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(2, click()))

        val addCardBtn = onView(withId(R.id.add_card_button))
        addCardBtn.perform(scrollTo(), click())

//        val textInputEditText = onView(
//            Matchers.allOf(
//                withId(R.id.content_add_auth_text),
//                childAtPosition(
//                    childAtPosition(
//                        withId(R.id.auth_fields),
//                        1
//                    ),
//                    1
//                ),
//                ViewMatchers.isDisplayed()
//            )
//        )
//        textInputEditText.perform(replaceText("greppyuser1@testbink.com"), closeSoftKeyboard())
//
//        val textInputEditText2 = onView(
//            Matchers.allOf(
//                withId(R.id.content_add_auth_text),
//                childAtPosition(
//                    childAtPosition(
//                        withId(R.id.auth_fields),
//                        1
//                    ),
//                    1
//                ),
//                ViewMatchers.isDisplayed()
//            )
//        )
//        textInputEditText2.perform(replaceText("GreppyPass1"), closeSoftKeyboard())

//        val emailEt = onView(withI
//        d(R.id.auth_fields)).check(matches(hasDescendant(withHint("example@mydomain.com"))))
//        emailEt.perform(replaceText("greppyuser1@testbink.com"))
//
//        val passwordEt = onView(withId(R.id.content_add_auth_text)).check(matches(withText("Password (min 6 characters)")))
//        passwordEt.perform(replaceText("GreppyPass1"), closeSoftKeyboard())

//        val emailEt = onView(withId(R.id.auth_fields)).perform(RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(2, replaceText("example@mydomain.com")))
//        emailEt.perform(replaceText("greppyuser1@testbink.com"), closeSoftKeyboard())

        onView(withId(R.id.auth_fields)).check(matches(isDisplayed()))

//        onView(withId(R.id.auth_fields)).perform(RecyclerViewActions.actionOnItem<RecyclerView.ViewHolder>(hasDescendant(withHint("example@mydomain.com")), replaceText("greppyuser1@testbink.com")))
//
//        val passwordEt = onView(withId(R.id.auth_fields)).perform(RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(3, replaceText("greppyuser1@testbink.com")))
//        passwordEt.perform(replaceText("GreppyPass1"), closeSoftKeyboard())
//
//        val authCardBtn = onView(withId(R.id.add_auth_cta))
//        authCardBtn.perform(click())
//
//        val doneBtn = onView(withId(R.id.button_done))
//        doneBtn.perform(click())
    }

    @Test
    fun swipeRefresh(){
        onView(withId(R.id.swipe_layout)).perform(swipeDown())
    }

    private fun childAtPosition(
        parentMatcher: Matcher<View>, position: Int
    ): Matcher<View> {

        return object : TypeSafeMatcher<View>() {
            override fun describeTo(description: Description) {
                description.appendText("Child at position $position in parent ")
                parentMatcher.describeTo(description)
            }

            public override fun matchesSafely(view: View): Boolean {
                val parent = view.parent
                return parent is ViewGroup && parentMatcher.matches(parent)
                        && view == parent.getChildAt(position)
            }
        }
    }

}
