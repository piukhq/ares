package com.bink.wallet.scenes.preference

import android.text.SpannableString
import android.text.style.UnderlineSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import androidx.recyclerview.widget.RecyclerView
import com.bink.wallet.databinding.PreferenceItemLayoutBinding
import com.bink.wallet.model.request.Preference
import com.bink.wallet.utils.CLEAR_PREF_KEY
import com.bink.wallet.utils.UtilFunctions

class PreferenceAdapter(
    private var preferences: List<Preference>,
    var onClickListener: (Preference, Boolean, CheckBox) -> Unit = { _, _, _ -> }
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

                if (item.slug == CLEAR_PREF_KEY) {
                    preferenceItem.visibility = View.GONE
                    preferenceText.visibility = View.VISIBLE

                    val spannedText = SpannableString(preferenceText.text)
                    spannedText.setSpan(UnderlineSpan(), 0, spannedText.length, 0)
                    preferenceText.text = spannedText

                    preferenceText.setOnClickListener {
                        onClickListener(
                            item,
                            false,
                            preferenceItem
                        )
                    }

                } else {
                    preferenceItem.visibility = View.VISIBLE
                    preferenceText.visibility = View.GONE
                    preferenceItem.isChecked = try {
                        item.value?.toInt() == 1
                    } catch (e: NumberFormatException) {
                        false
                    }

                    preferenceItem.setOnClickListener {
                        if (UtilFunctions.isNetworkAvailable(it.context, true)) {
                            onClickListener(
                                item,
                                preferenceItem.isChecked,
                                preferenceItem
                            )
                        }
                    }
                    executePendingBindings()
                }

            }
        }
    }
}
