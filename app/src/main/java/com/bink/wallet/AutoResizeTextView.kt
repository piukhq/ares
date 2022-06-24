package com.bink.wallet

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.content.Context
import android.content.res.Resources
import android.graphics.RectF
import android.os.Build
import android.text.Layout.Alignment
import android.text.StaticLayout
import android.text.TextPaint
import android.util.AttributeSet
import android.util.SparseIntArray
import android.util.TypedValue
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat


@SuppressLint("AppCompatCustomView")
class AutoResizeTextView : TextView {

    private val textRect = RectF()
    private var availableSpaceRect: RectF? = null
    private var textCachedSizes: SparseIntArray? = null
    private var textPaint: TextPaint? = null
    private var maxTextSize: Float = 0.toFloat()
    private var spacingMult = 1.0f
    private var spacingAdd = 0.0f
    private var minTextSize = 20f
    private var widthLimit: Int = 0
    private var maxLines: Int = 0
    private var enableSizeCache = true
    private var initialised: Boolean = false

    private val mSizeTester = object : SizeTester {
        @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
        override fun onTestSize(suggestedSize: Int, availableSpace: RectF): Int {
            textPaint?.textSize = suggestedSize.toFloat()
            val text = text.toString()
            val singleLine = maxLines == 1
            val maxTextSize = 1
            val minTextSize = -1

            if (singleLine) {
                textPaint?.let {
                    textRect.bottom = it.fontSpacing
                    textRect.right = it.measureText(text)
                }
            } else {
                val layout = StaticLayout(
                    text, textPaint,
                    widthLimit, Alignment.ALIGN_NORMAL, spacingMult,
                    spacingAdd, true
                )
                // return early if we have more lines
                if (maxLines != NO_LINE_LIMIT && layout.lineCount > maxLines) {
                    return 1
                }
                textRect.bottom = layout.height.toFloat()
                var maxWidth = -1
                for (i in 0 until layout.lineCount) {
                    if (maxWidth < layout.getLineWidth(i)) {
                        maxWidth = layout.getLineWidth(i).toInt()
                    }
                }
                textRect.right = maxWidth.toFloat()
            }

            textRect.offsetTo(0f, 0f)
            return if (availableSpace.contains(textRect)) {
                minTextSize
            } else {
                maxTextSize
            }
        }
    }

    private interface SizeTester {
        /**
         *
         * @param suggestedSize
         * Size of text to be tested
         * @param availableSpace
         * available space in which text must fit
         * @return an integer < 0 if after applying `suggestedSize` to
         * text, it takes less space than `availableSpace`, > 0
         * otherwise
         */
        fun onTestSize(suggestedSize: Int, availableSpace: RectF): Int
    }

    constructor(context: Context) : super(context) {
        initialize()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        initialize()
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    ) {
        initialize()
    }

    private fun initialize() {
        textPaint = TextPaint(paint)
        maxTextSize = textSize
        availableSpaceRect = RectF()
        typeface = ResourcesCompat.getFont(context, R.font.nunito_sans_light)
        textCachedSizes = SparseIntArray()
        if (maxLines == 0) {
            // no value was assigned during construction
            maxLines = NO_LINE_LIMIT
        }
        initialised = true
    }

    override fun setText(text: CharSequence, type: BufferType) {
        super.setText(text, type)
        adjustTextSize()
    }

    override fun setTextSize(size: Float) {
        maxTextSize = size
        textCachedSizes?.clear()
        adjustTextSize()
    }

    override fun setMaxLines(maxlines: Int) {
        super.setMaxLines(maxlines)
        maxLines = maxlines
        reAdjust()
    }

    override fun getMaxLines(): Int {
        return maxLines
    }

    override fun setSingleLine() {
        super.setSingleLine()
        maxLines = 1
        reAdjust()
    }

    override fun setSingleLine(singleLine: Boolean) {
        super.setSingleLine(singleLine)
        maxLines = if (singleLine) {
            1
        } else {
            NO_LINE_LIMIT
        }
        reAdjust()
    }

    override fun setLines(lines: Int) {
        super.setLines(lines)
        maxLines = lines
        reAdjust()
    }

    override fun setTextSize(unit: Int, size: Float) {
        val c = context

        val r: Resources = if (c == null) {
            Resources.getSystem()
        } else {
            c.resources
        }
        maxTextSize = TypedValue.applyDimension(
            unit, size,
            r.displayMetrics
        )
        textCachedSizes?.clear()
        adjustTextSize()
    }

    override fun setLineSpacing(add: Float, mult: Float) {
        super.setLineSpacing(add, mult)
        spacingMult = mult
        spacingAdd = add
    }

    private fun reAdjust() {
        adjustTextSize()
    }

    private fun adjustTextSize() {
        if (!initialised) {
            return
        }
        val startSize = minTextSize.toInt()
        val heightLimit = (measuredHeight - compoundPaddingBottom
                - compoundPaddingTop)
        widthLimit = (measuredWidth - compoundPaddingLeft
                - compoundPaddingRight)
        availableSpaceRect?.let {
            it.right = widthLimit.toFloat()
            it.bottom = heightLimit.toFloat()
            super.setTextSize(
                TypedValue.COMPLEX_UNIT_PX,
                efficientTextSizeSearch(
                    startSize, maxTextSize.toInt(),
                    mSizeTester, it
                ).toFloat()
            )
        }
    }

    private fun efficientTextSizeSearch(
        start: Int, end: Int,
        sizeTester: SizeTester, availableSpace: RectF
    ): Int {
        if (!enableSizeCache) {
            return binarySearch(start, end, sizeTester, availableSpace)
        }
        val text = text.toString()
        val key = text.length
        var size = textCachedSizes?.get(key)
        if (size != null && size != 0) {
            return size
        }
        size = binarySearch(start, end, sizeTester, availableSpace)
        textCachedSizes?.put(key, size)
        return size
    }

    override fun onTextChanged(
        text: CharSequence, start: Int,
        before: Int, after: Int
    ) {
        super.onTextChanged(text, start, before, after)
        reAdjust()
    }

    override fun onSizeChanged(
        width: Int, height: Int, oldwidth: Int,
        oldheight: Int
    ) {
        textCachedSizes?.clear()
        super.onSizeChanged(width, height, oldwidth, oldheight)
        if (width != oldwidth || height != oldheight) {
            reAdjust()
        }
    }

    companion object {

        private const val NO_LINE_LIMIT = -1

        private fun binarySearch(
            start: Int, end: Int, sizeTester: SizeTester,
            availableSpace: RectF
        ): Int {
            var lastBest = start
            var lo = start
            var hi = end - 1
            var mid: Int
            while (lo <= hi) {
                mid = (lo + hi).ushr(1)
                val midValCmp = sizeTester.onTestSize(mid, availableSpace)
                when {
                    midValCmp < 0 -> {
                        lastBest = lo
                        lo = mid + 1
                    }
                    midValCmp > 0 -> {
                        hi = mid - 1
                        lastBest = hi
                    }
                    else -> return mid
                }
            }
            // make sure to return last best
            // this is what should always be returned
            return lastBest
        }
    }
}