package com.bink.wallet


import androidx.test.espresso.DataInteraction
import androidx.test.espresso.ViewInteraction
import androidx.test.filters.LargeTest
import androidx.test.rule.ActivityTestRule
import androidx.test.runner.AndroidJUnit4
import android.view.View
import android.view.ViewGroup
import android.view.ViewParent

import androidx.test.InstrumentationRegistry.getInstrumentation
import androidx.test.espresso.Espresso.onData
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.*
import androidx.test.espresso.matcher.ViewMatchers.*

import com.bink.wallet.R

import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.TypeSafeMatcher
import org.hamcrest.core.IsInstanceOf
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.anything
import org.hamcrest.Matchers.`is`

@LargeTest
@RunWith(AndroidJUnit4::class)
class LogInTest {

    @Rule
    @JvmField
    var mActivityTestRule = ActivityTestRule(MainActivity::class.java)

    @Test
    fun logInTest() {
        val logInButton = onView(
            allOf(
                withId(R.id.log_in_email), withText("Log in with email"),
                childAtPosition(
                    allOf(
                        withId(R.id.root),
                        childAtPosition(
                            withId(R.id.main_fragment),
                            0
                        )
                    ),
                    4
                ),
                isDisplayed()
            )
        )
        logInButton.perform(click())

        val emailEditText = onView(
            allOf(
                withId(R.id.email_field),
                childAtPosition(
                    allOf(
                        withId(R.id.container),
                        childAtPosition(
                            withId(R.id.main_fragment),
                            0
                        )
                    ),
                    4
                ),
                isDisplayed()
            )
        )
        emailEditText.perform(replaceText("jbest@bink.com"), closeSoftKeyboard())
        emailEditText.perform(pressImeActionButton())

        val passwordEditText = onView(
            allOf(
                withId(R.id.password_field),
                childAtPosition(
                    allOf(
                        withId(R.id.container),
                        childAtPosition(
                            withId(R.id.main_fragment),
                            0
                        )
                    ),
                    6
                ),
                isDisplayed()
            )
        )
        passwordEditText.perform(replaceText("Password01"), closeSoftKeyboard())
        passwordEditText.perform(pressImeActionButton())

        val continueButton = onView(
            allOf(
                withId(R.id.log_in_button), withText("Continue"),
                childAtPosition(
                    allOf(
                        withId(R.id.container),
                        childAtPosition(
                            withId(R.id.main_fragment),
                            0
                        )
                    ),
                    8
                ),
                isDisplayed()
            )
        )
        continueButton.perform(click())
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
