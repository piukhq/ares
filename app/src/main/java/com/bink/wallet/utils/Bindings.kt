package com.bink.wallet.utils

import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.bink.wallet.scenes.browse_brands.model.MembershipPlan
import com.bumptech.glide.Glide

@BindingAdapter("bind:imageUrl")
fun ImageView.loadImage(item: MembershipPlan) {
    Glide.with(context)
        .load(item.images?.filter { it.type == 3 }?.get(0)?.url)
        .into(this)
}