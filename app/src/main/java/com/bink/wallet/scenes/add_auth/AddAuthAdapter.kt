package com.bink.wallet.scenes.add_auth

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView
import com.bink.wallet.databinding.AddAuthSpinnerItemBinding
import com.bink.wallet.databinding.AddAuthSwitchItemBinding
import com.bink.wallet.databinding.AddAuthSwitchItemBindingImpl
import com.bink.wallet.databinding.AddAuthTextItemBinding
import com.bink.wallet.model.response.membership_plan.AddFields
import com.bink.wallet.model.response.membership_plan.AuthoriseFields
import com.bink.wallet.model.response.membership_plan.MembershipPlan


class AddAuthAdapter(
    private val brands: List<Any>,
    val itemClickListener: (MembershipPlan) -> Unit = {}
) :
    RecyclerView.Adapter<BaseViewHolder<*>>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<*> {
        val inflater = LayoutInflater.from(parent.context)

        val binding : ViewDataBinding


        when (viewType){
            0,1,5,6 -> binding = AddAuthTextItemBinding.inflate(inflater)
            2,7 -> binding = AddAuthSpinnerItemBinding.inflate(inflater)
            else -> binding = AddAuthSwitchItemBindingImpl.inflate(inflater)
        }

        return when (viewType < 5) {
            true -> AddFieldHolder(binding)
            false -> AuthFieldHolder(binding)
        }
    }

    override fun onBindViewHolder(holder: BaseViewHolder<*>, position: Int) {

        when (holder) {
            is AddFieldHolder -> brands[position].let { holder.bind(it as AddFields) }
            is AuthFieldHolder -> brands[position].let { holder.bind(it as AuthoriseFields) }
        }
    }

    override fun getItemCount(): Int {
        return brands.size
    }

    override fun getItemViewType(position: Int): Int {
        return when (brands[position]) {
            is AddFields -> (brands[position] as AddFields).type!!
            else -> (brands[position] as AuthoriseFields).type!! + 5

        }
    }

    class AddFieldHolder(val binding: ViewDataBinding) :
        BaseViewHolder<AddFields>(binding) {

        override fun bind(item: AddFields) {
            when (binding) {
                is AddAuthTextItemBinding -> binding.addFields = item
                is AddAuthSpinnerItemBinding -> binding.addFields = item
                is AddAuthSwitchItemBinding -> binding.addFields = item
            }
            binding.executePendingBindings()
        }
    }

    class AuthFieldHolder(val binding: ViewDataBinding) :
        BaseViewHolder<AuthoriseFields>(binding) {

        override fun bind(item: AuthoriseFields) {
            when (binding) {
                is AddAuthTextItemBinding -> binding.authFields = item
                is AddAuthSpinnerItemBinding -> binding.authFields = item
                is AddAuthSwitchItemBinding -> binding.authFields = item
            }
            binding.executePendingBindings()
        }
    }
}