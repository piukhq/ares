package com.bink.wallet

import android.content.Context
import android.util.AttributeSet
import android.widget.ImageView
import com.bink.wallet.utils.ScaleSize
import android.view.WindowManager

class ScaleImageView: ImageView {
    var scale = 0f

    constructor(context: Context) : super(context) {
        init(null, 0)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(attrs, 0)
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    ) {
        init(attrs, defStyle)
    }

    private fun init(attrs: AttributeSet?, defStyle: Int) {
        // Load attributes
        val a = context.obtainStyledAttributes(
            attrs, R.styleable.Scale, defStyle, 0
        )
        scale = a.getFloat(R.styleable.Scale_size, 0f);
        a.recycle()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        if (scale != 0f) {
            val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            setMeasuredDimension(width, ScaleSize.ScaleSize(windowManager, scale))
        } else {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        }
    }
}