package com.bink.wallet.utils

import android.graphics.*
import android.graphics.drawable.Drawable

class TextDrawable(private var merchantLetter: String, private var colour: String) : Drawable() {


    private val rec = Rect()

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        textAlign = Paint.Align.CENTER
        textSize = 110.0f
        typeface = Typeface.create("", Typeface.BOLD)
        color = Color.WHITE
        textAlign = Paint.Align.CENTER
        getTextBounds(merchantLetter,0,merchantLetter.length,rec)
    }

    override fun draw(canvas: Canvas) {
        canvas.drawColor(Color.parseColor(colour))
        canvas.drawText(merchantLetter.first().toString(), (bounds.width()/2).toFloat(),
            (bounds.height()/1.3).toFloat(), paint)
    }

    override fun setAlpha(alpha: Int) {
        paint.alpha = alpha
    }

    override fun setColorFilter(colorFilter: ColorFilter?) {
        paint.colorFilter = colorFilter
    }

    override fun getOpacity(): Int {
       return paint.alpha
    }


}