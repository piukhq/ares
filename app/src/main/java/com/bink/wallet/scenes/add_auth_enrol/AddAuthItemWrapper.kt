package com.bink.wallet.scenes.add_auth_enrol

import com.bink.wallet.model.request.membership_card.PlanFieldsRequest
import com.bink.wallet.model.response.membership_plan.PlanField
import com.bink.wallet.utils.enums.AddAuthItemType

class AddAuthItemWrapper(
    val fieldType: Any,
    val fieldsRequest: PlanFieldsRequest
) {
    fun getFieldType(): AddAuthItemType {
        return if(fieldType is PlanField) {
            AddAuthItemType.PLAN_FIELD
        } else AddAuthItemType.PLAN_DOCUMENT
    }
}