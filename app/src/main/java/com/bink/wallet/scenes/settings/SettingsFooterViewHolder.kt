package com.bink.wallet.scenes.settings

import androidx.recyclerview.widget.RecyclerView
import com.bink.wallet.BuildConfig
import com.bink.wallet.R
import com.bink.wallet.databinding.SettingsFooterBinding

class SettingsFooterViewHolder(val binding: SettingsFooterBinding) :
    RecyclerView.ViewHolder(binding.root) {

    fun bind(usersEmail: String) {
        binding.tvEmail.text = usersEmail
        binding.tvBuildVersion.text = binding.root.context.getString(
            R.string.settings_build_version,
            BuildConfig.VERSION_NAME,
            BuildConfig.VERSION_CODE
        )
    }

}