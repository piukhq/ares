package com.bink.wallet.scenes.add_auth_enrol.adapter

import androidx.databinding.ViewDataBinding
import com.bink.wallet.model.response.membership_plan.Account
import com.bink.wallet.model.response.membership_plan.PlanField
import com.bink.wallet.scenes.BaseViewHolder
import com.bink.wallet.scenes.add_auth_enrol.AddAuthItemWrapper
import com.bink.wallet.scenes.add_auth_enrol.FormsUtil

abstract class BaseAddAuthViewHolder<T>(
    viewDataBinding: ViewDataBinding
) :
    BaseViewHolder<T>(viewDataBinding) {

    var checkValidation: (String?) -> Unit = {}

    var setFieldRequestValue: (item: AddAuthItemWrapper, value: String) -> Unit = { _, _ ->  }

    var addFields : List<PlanField>? = null

    var account: Account? = null

    var position :Int ? = null

    override fun bind(item: T) {}

    open fun onBarcodeScanSuccess(){}

    open fun addFormField( planField1: PlanField) {
        position?.let {
            FormsUtil.addFormField(it,planField1 )
        }
    }



}