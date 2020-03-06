package com.bink.wallet.stampsprogressindicator

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.databinding.DataBindingUtil
import com.bink.wallet.R
import com.bink.wallet.databinding.StampsProgressIndicatorBinding
import com.bink.wallet.utils.enums.VoucherStates

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

    fun setupStamps(maxProgress: Int, currentProgress: Int, voucherStatus: String) {
        val stampBackgroundDrawable =
            when (voucherStatus) {
                VoucherStates.IN_PROGRESS.state -> resources.getDrawable(
                    R.drawable.bg_stamp_in_progress,
                    null
                )
                VoucherStates.ISSUED.state -> {
                    resources.getDrawable(
                        R.drawable.bg_stamp_earned,
                        null
                    )
                }
                VoucherStates.REDEEMED.state -> {
                    resources.getDrawable(
                        R.drawable.bg_stamp_redeemed,
                        null
                    )
                }
                VoucherStates.EXPIRED.state -> {
                    resources.getDrawable(
                        R.drawable.bg_stamp_expired,
                        null
                    )
                }
                else -> resources.getDrawable(
                    R.drawable.bg_stamp_pending,
                    null
                )
            }
        binding.container.removeAllViews()
        (1..maxProgress).forEachIndexed { index, _ ->
            binding.container.addView(
                ImageView(context).apply {
                    background =
                        if (index < currentProgress) {
                            stampBackgroundDrawable
                        } else {
                            resources.getDrawable(
                                R.drawable.bg_stamp_pending,
                                null
                            )
                        }
                    layoutParams = LayoutParams(
                        resources.getDimension(R.dimen.voucher_stamp_size).toInt(),
                        resources.getDimension(R.dimen.voucher_stamp_size).toInt()
                    ).apply {
                        setMargins(
                            0,
                            0,
                            resources.getDimension(R.dimen.voucher_stamp_margin_horizontal).toInt(),
                            0
                        )
                    }
                }
            )
        }
    }
}