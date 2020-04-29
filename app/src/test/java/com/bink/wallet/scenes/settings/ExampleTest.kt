package com.bink.wallet.scenes.settings

import android.content.Context
import android.os.Build
import androidx.test.core.app.ApplicationProvider
import com.bink.wallet.R
import junit.framework.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

private const val FAKE_STRING = "Settings"

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.P])
class ExampleTest {
    val context = ApplicationProvider.getApplicationContext<Context>()

    @Test fun readStringFromContext_LocalizedString() {
        // Given a Context object retrieved from Robolectric...
//        val myObjectUnderTest = ClassUnderTest(context)

        // ...when the string is returned from the object under test...
        val result: String = context.getString(R.string.settings)

        // ...then the result should be the expected one.
        assertEquals(result, FAKE_STRING)
    }
}