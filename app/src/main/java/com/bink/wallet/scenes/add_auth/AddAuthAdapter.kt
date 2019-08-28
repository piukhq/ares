package com.bink.wallet.scenes.add_auth

import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatEditText
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView
import com.bink.wallet.R
import com.bink.wallet.databinding.AddAuthSpinnerItemBinding
import com.bink.wallet.databinding.AddAuthSwitchItemBinding
import com.bink.wallet.databinding.AddAuthSwitchItemBindingImpl
import com.bink.wallet.databinding.AddAuthTextItemBinding
import com.bink.wallet.model.request.membership_card.Account
import com.bink.wallet.model.response.membership_plan.AddFields
import com.bink.wallet.model.response.membership_plan.AuthoriseFields


class AddAuthAdapter(
    private val brands: List<Any>,
    private var account: Account
) :
    RecyclerView.Adapter<BaseViewHolder<*>>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<*> {
        val inflater = LayoutInflater.from(parent.context)

        val binding: ViewDataBinding


        binding = when (viewType) {
            0, 1, 5, 6 -> AddAuthTextItemBinding.inflate(inflater)
            2, 7 -> AddAuthSpinnerItemBinding.inflate(inflater)
            else -> AddAuthSwitchItemBindingImpl.inflate(inflater)
        }

        return when (viewType < 5) {
            true -> AddFieldHolder(binding, account)
            false -> AuthFieldHolder(binding, account)
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

    class AddFieldHolder(val binding: ViewDataBinding, val account: Account) :
        BaseViewHolder<AddFields>(binding) {

        private var text: AppCompatEditText? = null

        init {
            text = itemView.findViewById(R.id.content_add_auth_text)
        }

        override fun bind(item: AddFields) {
            when (binding) {
                is AddAuthTextItemBinding -> {
                    val currentAddField =
                        com.bink.wallet.model.request.membership_card.AddFields(item.column, "")
                    binding.addFields = item
                    binding.addFieldsRequest = currentAddField

                    account.add_fields?.add(currentAddField)

                    text?.addTextChangedListener(object : TextWatcher {
                        override fun afterTextChanged(p0: Editable?) {
                        }

                        override fun beforeTextChanged(
                            p0: CharSequence?,
                            p1: Int,
                            p2: Int,
                            p3: Int
                        ) {
                        }

                        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                            currentAddField.value = p0.toString()
                        }
                    })
                }
                is AddAuthSpinnerItemBinding -> binding.addFields = item
                is AddAuthSwitchItemBinding -> binding.addFields = item
            }

            binding.executePendingBindings()
        }
    }

    class AuthFieldHolder(val binding: ViewDataBinding, val account: Account) :
        BaseViewHolder<AuthoriseFields>(binding) {
        private var text: AppCompatEditText? = null

        init {
            text = itemView.findViewById(R.id.content_add_auth_text)
        }

        override fun bind(item: AuthoriseFields) {
            when (binding) {
                is AddAuthTextItemBinding -> {
                    val currentAuthotiseField =
                        com.bink.wallet.model.request.membership_card.AuthoriseFields(
                            item.column, ""
                        )
                    binding.authFields = item
                    binding.authFieldsRequest =
                        currentAuthotiseField
                    account.authorise_fields?.add(currentAuthotiseField)

                    text?.addTextChangedListener(object : TextWatcher {
                        override fun afterTextChanged(p0: Editable?) {
                        }

                        override fun beforeTextChanged(
                            p0: CharSequence?,
                            p1: Int,
                            p2: Int,
                            p3: Int
                        ) {
                        }

                        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                            currentAuthotiseField.value = p0.toString()
                        }
                    })
                }
                is AddAuthSpinnerItemBinding -> binding.authFields = item
                is AddAuthSwitchItemBinding -> binding.authFields = item
            }
            binding.executePendingBindings()
        }
    }
}