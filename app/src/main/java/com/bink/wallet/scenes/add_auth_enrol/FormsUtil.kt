package com.bink.wallet.scenes.add_auth_enrol

import com.bink.wallet.model.request.membership_card.PlanFieldsRequest
import com.bink.wallet.model.response.membership_plan.PlanField
import com.bink.wallet.utils.UtilFunctions

object FormsUtil {

    private var fields = mutableMapOf<Int, FormField>()

    fun addFormField(position: Int, planField: PlanField) {
        fields.put(position, FormField(planField, PlanFieldsRequest(planField.column,null,null)))
    }

    fun getSize() = fields.size

    fun clearForms() {
        fields.clear()
    }

    fun updateField(position:Int,value:String){
        //Update the
        val form = fields.get(position)

        form?.fieldsRequest?.value = value
    }

//    fun formValueValidation():Boolean{
//        return UtilFunctions.isValidField()
//    }
}