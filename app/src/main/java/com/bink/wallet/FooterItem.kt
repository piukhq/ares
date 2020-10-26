package com.bink.wallet

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.databinding.DataBindingUtil
import com.bink.wallet.databinding.FooterItemBinding
import com.bink.wallet.utils.enums.FooterType

class FooterItem @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet,
    defStyle: Int = 0
) : ConstraintLayout(context, attrs, defStyle) {

    var binding: FooterItemBinding =
        DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.footer_item, this, true)
    private val attributes =
        context.theme.obtainStyledAttributes(attrs, R.styleable.FooterItem, 0, 0)
    private val footerType = attributes.getString(R.styleable.FooterItem_type)


    init {
        footerType?.let { populateItem(it) }
        attributes.recycle()
    }

    private fun populateItem(id: String) {
        getFooterTypeById(id).let {
            binding.title.text = resources.getString(it.footerTitle)
            binding.description.text = resources.getString(it.footerDescription)
        }
    }

    private fun getFooterTypeById(type: String): FooterType {
        return FooterType.values().first { it.type == type }
    }

}