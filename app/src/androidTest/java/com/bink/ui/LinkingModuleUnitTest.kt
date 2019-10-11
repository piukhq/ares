package com.bink.ui
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.filters.LargeTest
import androidx.test.rule.ActivityTestRule
import com.bink.wallet.MainActivity
import com.bink.wallet.R
import com.bink.wallet.utils.enums.LinkStatus
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
@LargeTest
class LinkingModuleUnitTest {
    @get:Rule
    val activityRule = ActivityTestRule(MainActivity::class.java)

    private val linkDescriptionId = R.id.link_description
    private val linkStatusTextId = R.id.link_status_text
    private val linkImageId = R.id.link_status_img

    @Test
    fun configureLinkingStatus(linkStatus: LinkStatus){
        when(linkStatus){
            LinkStatus.STATUS_LINKABLE_NO_PAYMENT_CARDS -> {
                onView(withId(linkDescriptionId)).check(matches(withText(R.string.description_no_cards)))
                onView(withId(linkStatusTextId)).check(matches(withText(R.string.link_status_linkable_no_cards)))
                //onView(withId(linkImageId)).check(matches(with))
            }
        }
    }
}