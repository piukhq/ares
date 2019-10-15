package com.bink.espresso_test_matchers

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.view.View
import android.widget.ImageView
import androidx.annotation.DrawableRes
import org.hamcrest.Description
import org.hamcrest.TypeSafeMatcher

class CustomDrawableMatcher {

    companion object {
        fun toBitmap(drawable: Drawable): Bitmap? {
            val bitmap: Bitmap? =
                if (drawable.intrinsicWidth <= 0 || drawable.intrinsicHeight <= 0) {
                    Bitmap.createBitmap(
                        1,
                        1,
                        Bitmap.Config.ARGB_8888
                    ) // Single color bitmap will be created of 1x1 pixel
                } else {
                    Bitmap.createBitmap(
                        drawable.intrinsicWidth,
                        drawable.intrinsicHeight,
                        Bitmap.Config.ARGB_8888
                    )
                }

            if (drawable is BitmapDrawable) {
                if (drawable.bitmap != null) {
                    return drawable.bitmap
                }
            }
            val canvas = bitmap?.let { Canvas(it) }
            canvas?.width?.let { drawable.setBounds(0, 0, it, canvas.height) }
            canvas?.let { drawable.draw(it) }
            return bitmap
        }

        fun withDrawable(@DrawableRes id: Int) = object : TypeSafeMatcher<View>() {
            override fun describeTo(description: Description) {
                description.appendText("ImageView with drawable same as drawable with id $id")
            }

            override fun matchesSafely(view: View): Boolean {
                val context = view.context
                val expectedBitmap = context.getDrawable(id)?.let {
                    toBitmap(it)
                }
                return view is ImageView && toBitmap(view.drawable)!!.sameAs(expectedBitmap)
            }
        }
    }
}


