package com.bink.wallet

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.databinding.DataBindingUtil
import com.bink.wallet.databinding.LoyaltyCardHeaderBinding

class LoyaltyCardHeader @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : ConstraintLayout(context, attrs, defStyle) {
    var binding: LoyaltyCardHeaderBinding = DataBindingUtil.inflate(
        LayoutInflater.from(context),
        R.layout.loyalty_card_header,
        this,
        true
    )
}