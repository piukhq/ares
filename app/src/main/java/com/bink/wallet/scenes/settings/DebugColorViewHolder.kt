package com.bink.wallet.scenes.settings

import android.graphics.Color
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bink.wallet.R
import com.bink.wallet.model.response.membership_plan.MembershipPlan
import com.bink.wallet.utils.ColorUtil

class DebugColorViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    private val primary = itemView.findViewById<View>(R.id.primary)
    private val secondary = itemView.findViewById<View>(R.id.secondary)
    private val tvPlanName = itemView.findViewById<TextView>(R.id.tv_plan_name)

    fun bind(plan: MembershipPlan) {
        primary.setBackgroundColor(Color.parseColor(plan.card?.colour))
        secondary.setBackgroundColor(Color.parseColor(plan.card?.getSecondaryColor()))
        tvPlanName.text = plan.account?.plan_name

        if (ColorUtil.isColorLight(
                Color.parseColor(plan.card?.colour),
                ColorUtil.LIGHT_THRESHOLD_TEXT
            )
        ) {
            tvPlanName.setTextColor(ContextCompat.getColor(itemView.context, android.R.color.black))
        } else {
            tvPlanName.setTextColor(ContextCompat.getColor(itemView.context, android.R.color.white))
        }
    }
}