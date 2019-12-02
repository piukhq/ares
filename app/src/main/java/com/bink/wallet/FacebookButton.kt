package com.bink.wallet

import android.content.Context
import android.util.AttributeSet
import com.bink.wallet.utils.toPixelFromDip
import com.facebook.login.widget.LoginButton

class FacebookButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : LoginButton(context, attrs, R.style.RoundedFacebookButton) {

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        setMeasuredDimension(
            width,
            context.toPixelFromDip(R.integer.facebook_button_height_dp).toInt()
        )
    }

    override fun configureButton(
        context: Context?,
        attrs: AttributeSet?,
        defStyleAttr: Int,
        defStyleRes: Int
    ) {
        super.configureButton(
            context,
            attrs,
            R.style.RoundedFacebookButton,
            R.style.RoundedFacebookButton
        )
        setCompoundDrawablesRelativeWithIntrinsicBounds(null, null, null, null)
    }
}