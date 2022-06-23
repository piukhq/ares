package com.bink.wallet.scenes.add_auth_enrol.adapter

import android.widget.EditText
import androidx.databinding.ViewDataBinding
import com.bink.wallet.model.response.membership_plan.Account
import com.bink.wallet.model.response.membership_plan.PlanField
import com.bink.wallet.scenes.BaseViewHolder
import com.bink.wallet.scenes.add_auth_enrol.FormsUtil

abstract class BaseAddAuthViewHolder<T>(
    viewDataBinding: ViewDataBinding
) :
    BaseViewHolder<T>(viewDataBinding) {

    var showSoftkeyboard: (EditText) -> Unit = {}

    var checkValidation: (String?) -> Unit = {}

    var addFields: List<PlanField>? = null

    var account: Account? = null

    var position: Int? = null

    var barcode: String? = null

    private var fieldValue: String? = null

    override fun bind(item: T) {}

    open fun onBarcodeScanSuccess(scannedBarcode: String?) {}

    open fun addFormField(planField: PlanField) {
        position?.let {
            FormsUtil.addFormField(it, planField)
        }
    }

    open fun updateFieldValue(value: String) {
        position?.let {
            fieldValue = value
            FormsUtil.updateField(it, value)
        }
    }

    open fun updateValidation(isValid: Boolean) {
        position?.let {
            FormsUtil.updateValidation(it, isValid)
        }
    }

    open fun addPlanDocument(hasCheckBoxBeenTicked: Boolean) {
        position?.let {
            FormsUtil.addPlanDocument(it, hasCheckBoxBeenTicked)
        }
    }

}