package com.bink.wallet.scenes.add_auth_enrol.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bink.wallet.databinding.AutoCompleteItemBinding

class AutoCompleteAdapter(private var autoCompleteFields: ArrayList<String>, private val selectedField: (String) -> Unit) : RecyclerView.Adapter<AutoCompleteViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AutoCompleteViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return AutoCompleteViewHolder(AutoCompleteItemBinding.inflate(inflater, parent, false), selectedField)
    }

    override fun onBindViewHolder(holder: AutoCompleteViewHolder, position: Int) = autoCompleteFields[position].let { holder.bind(it) }

    override fun getItemCount(): Int {
        return autoCompleteFields.size
    }

}