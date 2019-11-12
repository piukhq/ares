package com.bink.wallet

import android.content.Context
import android.util.AttributeSet
import com.bink.wallet.utils.toPixelFromDip
import com.facebook.login.widget.LoginButton

class FacebookButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null, defStyle: Int = 0
) : LoginButton(context, attrs, R.style.RoundedFacebookButton) {

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        setMeasuredDimension(width, context.toPixelFromDip(48.toFloat()).toInt())
    }

    override fun configureButton(
        context: Context?,
        attrs: AttributeSet?,
        defStyleAttr: Int,
        defStyleRes: Int
    ) {
        super.configureButton(context, attrs, defStyleAttr, defStyleRes)
        setCompoundDrawablesRelativeWithIntrinsicBounds(null, null, null, null)
    }
}