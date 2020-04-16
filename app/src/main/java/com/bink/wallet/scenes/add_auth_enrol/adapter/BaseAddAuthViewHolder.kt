package com.bink.wallet.scenes.add_auth_enrol.adapter

import androidx.databinding.ViewDataBinding
import com.bink.wallet.scenes.BaseViewHolder
import com.bink.wallet.scenes.add_auth_enrol.AddAuthItemWrapper

abstract class BaseAddAuthViewHolder<T>(
    viewDataBinding: ViewDataBinding
) :
    BaseViewHolder<T>(viewDataBinding) {

    var addAuthItems = mutableListOf<AddAuthItemWrapper>()
    var checkValidation: () -> Unit = {}

    override fun bind(item: T) {}

}