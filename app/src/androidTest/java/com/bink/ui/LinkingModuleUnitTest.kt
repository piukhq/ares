package com.bink.ui

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.filters.LargeTest
import androidx.test.rule.ActivityTestRule
import androidx.test.runner.AndroidJUnit4
import com.bink.espresso_test_matchers.CustomDrawableMatcher
import com.bink.wallet.MainActivity
import com.bink.wallet.R
import com.bink.wallet.scenes.loyalty_details.LoyaltyCardDetailsFragment
import com.bink.wallet.utils.enums.LinkStatus
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
class LinkingModuleUnitTest {
    @get:Rule
    val activityRule = ActivityTestRule(MainActivity::class.java)

    private var mainActivity: MainActivity? = null

    @Before
    fun setUp() {
        mainActivity = activityRule.activity
        activityRule.activity.supportFragmentManager.beginTransaction()
            .add(R.id.swipe_layout_loyalty_details, LoyaltyCardDetailsFragment())
    }

    @Test
    fun configureLinkingStatus() {
        val linkStatus = LinkStatus.STATUS_UNLINKABLE
        when(linkStatus){
            LinkStatus.STATUS_LINKABLE_NO_PAYMENT_CARDS, LinkStatus.STATUS_LINKABLE_NO_PAYMENT_CARDS_LINKED -> {
                checkMatchesViews(
                    R.string.description_no_cards,
                    R.string.link_status_linkable_no_cards,
                    R.drawable.ic_lcd_module_icons_link_error
                )
            }
            LinkStatus.STATUS_LINKABLE_GENERIC_ERROR -> {
                checkMatchesViews(
                    R.string.description_error,
                    R.string.link_status_link_error,
                    R.drawable.ic_lcd_module_icons_link_error
                )
            }
            LinkStatus.STATUS_LINKABLE_REQUIRES_AUTH -> {
                checkMatchesViews(
                    R.string.description_requires_auth,
                    R.string.link_status_requires_auth,
                    R.drawable.ic_lcd_module_icons_points_login
                )
            }
            LinkStatus.STATUS_LINKABLE_REQUIRES_AUTH_PENDING -> {
                checkMatchesViews(
                    R.string.description_requires_auth,
                    R.string.link_status_requires_auth,
                    R.drawable.ic_lcd_module_icons_points_login
                )
            }
            LinkStatus.STATUS_LINKABLE_REQUIRES_AUTH_PENDING_FAILED -> {
                checkMatchesViews(
                    R.string.description_auth_failed,
                    R.string.link_status_auth_failed,
                    R.drawable.ic_lcd_module_icons_points_login
                )
            }
            LinkStatus.STATUS_UNLINKABLE -> {
                checkMatchesViews(
                    R.string.description_unlinkable,
                    R.string.link_status_unlinkable,
                    R.drawable.ic_lcd_module_icons_link_inactive
                )
            }
            else -> {
            }
        }
    }

    private fun checkMatchesViews(descriptionId: Int, statusId: Int, imageId: Int) {
        val linkDescriptionId = R.id.link_description
        val linkStatusTextId = R.id.link_status_text
        val linkImageId = R.id.link_status_img
        onView(withId(linkDescriptionId)).check(matches(withText(descriptionId)))
        onView(withId(linkStatusTextId)).check(matches(withText(statusId)))
        onView(withId(linkImageId)).check(matches(CustomDrawableMatcher.withDrawable(imageId)))
    }
}