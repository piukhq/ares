package com.bink.wallet

import android.content.Context
import android.graphics.Canvas
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import com.bink.wallet.utils.CardViewStyleKit

/**
 * TODO: document your custom view class.
 */
class CardView : View {
    private var firstColor: Int = 0
    private var secondColor: Int = 0

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
            attrs, R.styleable.CardView, defStyle, 0
        )
        firstColor = a.getInteger(R.styleable.CardView_firstColor, 0)
        secondColor = a.getInteger(R.styleable.CardView_secondColor, 0)

        a.recycle()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        CardViewStyleKit.drawCanvas(
            canvas,
            RectF(0f, 0f, width.toFloat(), height.toFloat()),
            CardViewStyleKit.ResizingBehavior.AspectFit,
            firstColor,
            secondColor
        )
    }

    //Must be called with Color.parseColor(stringColorHex)
    fun setFirstColor(color: Int) {
        firstColor = color
        invalidate()
    }

    //Must be called with Color.parseColor(stringColorHex)
    fun setSecondColor(color: Int) {
        secondColor = color
        invalidate()
    }
}
