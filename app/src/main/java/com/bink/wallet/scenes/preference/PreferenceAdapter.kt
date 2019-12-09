package com.bink.wallet.scenes.preference

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.CheckBox
import androidx.recyclerview.widget.RecyclerView
import com.bink.wallet.databinding.PreferenceItemLayoutBinding
import com.bink.wallet.model.request.Preference

class PreferenceAdapter(
    private var preferences: List<Preference>,
    var onClickListener: (Preference, Int, CheckBox) -> Unit = { _, _, _ -> }
) : RecyclerView.Adapter<PreferenceAdapter.PreferenceItemHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PreferenceItemHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = PreferenceItemLayoutBinding.inflate(inflater)
        return PreferenceItemHolder(binding)
    }

    override fun onBindViewHolder(holder: PreferenceItemHolder, position: Int) {
        holder.bind(preferences[position])
    }

    override fun getItemCount() = preferences.size

    inner class PreferenceItemHolder(val binding: PreferenceItemLayoutBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Preference) {
            with(binding) {
                preference = item
                executePendingBindings()

                preferenceItem.setOnCheckedChangeListener { _, isChecked ->
                    onClickListener(
                        item,
                        when (isChecked) {
                            true -> 1
                            else -> 0
                        },
                        preferenceItem
                    )
                }
            }
        }
    }
}
