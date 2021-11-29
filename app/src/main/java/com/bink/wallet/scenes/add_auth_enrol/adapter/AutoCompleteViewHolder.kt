package com.bink.wallet.scenes.add_auth_enrol.adapter

import com.bink.wallet.databinding.AutoCompleteItemBinding
import com.bink.wallet.scenes.BaseViewHolder

class AutoCompleteViewHolder(val binding: AutoCompleteItemBinding, val selectedField: (String) -> Unit) : BaseViewHolder<String>(binding) {

    override fun bind(item: String) {
        with(binding) {
            title.text = item
            parent.setOnClickListener {
                selectedField(item)
            }
        }
    }

}