package com.bink.wallet

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.databinding.DataBindingUtil
import com.bink.wallet.databinding.ModalBrandHeaderBinding

class ModalBrandHeader @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : ConstraintLayout(context, attrs, defStyle) {

    var binding: ModalBrandHeaderBinding =
        DataBindingUtil.inflate(
            LayoutInflater.from(context),
            R.layout.modal_brand_header,
            this,
            true
        )

}