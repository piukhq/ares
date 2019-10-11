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
    private val attributes = context.theme.obtainStyledAttributes(attrs, R.styleable.FooterItem, 0, 0)
    private val footerType = attributes.getString(R.styleable.FooterItem_type)


    init {
        footerType?.let { populateItem(it) }
        attributes.recycle()
    }

    private fun populateItem(type: String) {
        when (type) {
            FooterType.ABOUT.type -> {
                binding.title.text = resources.getText(R.string.about_membership)
                binding.description.text = resources.getText(R.string.learn_more)
            }

            FooterType.SECURITY.type -> {
                binding.title.text = resources.getText(R.string.security_privacy)
                binding.description.text = resources.getText(R.string.how_we_protect)
            }

            FooterType.DELETE.type -> {
                binding.title.text = resources.getText(R.string.delete_card)
                binding.description.text = resources.getText(R.string.remove_card)
            }

            FooterType.RENAME.type -> {
                binding.title.text = resources.getText(R.string.rename_card)
                binding.description.text = resources.getText(R.string.rename_card_description)
            }
        }
    }
}