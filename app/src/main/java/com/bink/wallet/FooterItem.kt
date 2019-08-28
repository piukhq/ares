package com.bink.wallet

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.databinding.DataBindingUtil
import com.bink.wallet.databinding.FooterItemBinding

class FooterItem @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet,
    defStyle: Int = 0
) : ConstraintLayout(context, attrs, defStyle) {

    var binding: FooterItemBinding =
        DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.footer_item, this, true)
    private val attributes = context.theme.obtainStyledAttributes(attrs, R.styleable.FooterItem, 0, 0)
    private val footerType = attributes.getString(R.styleable.FooterItem_type)

    companion object {
        private const val ABOUT = "0"
        private const val SECURITY = "1"
        private const val DELETE = "2"
    }

    init {
        footerType?.let { populateItem(it) }
        attributes.recycle()
    }

    private fun populateItem(type: String) {
        when (type) {
            ABOUT -> {
                binding.title.text = resources.getText(R.string.about_membership)
                binding.description.text = resources.getText(R.string.learn_more)
            }

            SECURITY -> {
                binding.title.text = resources.getText(R.string.security_privacy)
                binding.description.text = resources.getText(R.string.how_we_protect)
            }

            DELETE -> {
                binding.title.text = resources.getText(R.string.delete_card)
                binding.description.text = resources.getText(R.string.remove_card)
            }
        }
    }
}