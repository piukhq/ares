package com.bink.wallet.stampsprogressindicator

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.databinding.DataBindingUtil
import com.bink.wallet.R
import com.bink.wallet.databinding.StampsProgressIndicatorBinding

class StampsProgressIndicator @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : LinearLayout(context, attrs, defStyle) {
    private val binding: StampsProgressIndicatorBinding = DataBindingUtil.inflate(
        LayoutInflater.from(context),
        R.layout.view_stamps_progress_indicator,
        this,
        true
    )
    private val attributes =
        context.theme.obtainStyledAttributes(attrs, R.styleable.StampsProgressIndicator, 0, 0)
    private val maxProgress = attributes.getString(R.styleable.StampsProgressIndicator_maxProgress)
    private val currentProgress =
        attributes.getString(R.styleable.StampsProgressIndicator_currentProgress)
    private val voucherStatus = attributes.getString(R.styleable.StampsProgressIndicator_status)

    init {
        Log.d("WRKR", maxProgress.toString() + "\n" + currentProgress + "\n" + voucherStatus)
        setupStamps()
    }

    private fun setupStamps() {
        val stampBackgroundDrawable =
            when (voucherStatus) {
                "1" -> resources.getDrawable(
                    R.drawable.bg_stamp_in_progress,
                    null
                )
                "2" -> resources.getDrawable(
                    R.drawable.bg_stamp_earned,
                    null
                )
                "3" -> resources.getDrawable(
                    R.drawable.bg_stamp_redeemed,
                    null
                )
                else -> resources.getDrawable(
                    R.drawable.bg_stamp_pending,
                    null
                )
            }
        (1..(maxProgress?.toInt() ?: 0)).forEachIndexed { index, _ ->
            binding.container.addView(
                ImageView(context).apply {
                    background =
                        if (currentProgress?.toInt() != null && index < currentProgress.toInt()) {
                            stampBackgroundDrawable
                        } else {
                            resources.getDrawable(
                                R.drawable.bg_stamp_pending,
                                null
                            )
                        }
                    layoutParams = LayoutParams(
                        resources.getDimension(R.dimen.margin_padding_size_medium_large).toInt(),
                        resources.getDimension(R.dimen.margin_padding_size_medium_large).toInt()
                    ).apply {
                        setMargins(0, 0, 12, 0)
                    }
                }
            )
        }
    }
}