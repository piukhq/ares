package com.bink.espresso_test_matchers

import android.view.View
import android.widget.ImageView
import org.hamcrest.Description
import org.hamcrest.TypeSafeMatcher

class CustomDrawableMatcher : TypeSafeMatcher<View> {
    private var expectedId: Int = 0
    private var resourceName: String? = null
    val EMPTY = -1
    val ANY = -2

    constructor(expectedId: Int) {
        this.expectedId = expectedId
    }

    override fun describeTo(description: Description?) {
        description?.appendText("with drawable from resource id: ")
        description?.appendValue(expectedId)
        if (resourceName != null) {
            description?.appendText("[")
            description?.appendText(resourceName)
            description?.appendText("]")
        }
    }

    override fun matchesSafely(target: View?): Boolean {
        if (target !is ImageView) {
            return false
        }
        if (expectedId == EMPTY) {
            return target.drawable == null
        }
        if (expectedId == ANY) {
            return target.drawable != null
        }
        val resources = target.getContext().resources
        val expectedDrawable = resources.getDrawable(expectedId)
        resourceName = resources.getResourceEntryName(expectedId)

        if (expectedDrawable == null) {
            return false
        }
        return  expectedDrawable == target.drawable
    }
}