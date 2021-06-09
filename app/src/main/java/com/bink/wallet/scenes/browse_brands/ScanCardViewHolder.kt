package com.bink.wallet.scenes.browse_brands

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.bink.wallet.BrandItemBinding
import com.bink.wallet.ScanItemBinding

class ScanCardViewHolder(val binding: ScanItemBinding) :
    RecyclerView.ViewHolder(binding.root) {

    fun bind(onClickListener: View.OnClickListener?) {
        onClickListener?.let {
            binding.root.setOnClickListener(it)
        }
    }
}
