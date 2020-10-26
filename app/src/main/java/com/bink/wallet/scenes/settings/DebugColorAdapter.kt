package com.bink.wallet.scenes.settings

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bink.wallet.R
import com.bink.wallet.model.response.membership_plan.MembershipPlan

class DebugColorAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var plans = listOf<MembershipPlan>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return DebugColorViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.item_debug_color,
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return plans.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as DebugColorViewHolder).bind(plans[position])
    }
}