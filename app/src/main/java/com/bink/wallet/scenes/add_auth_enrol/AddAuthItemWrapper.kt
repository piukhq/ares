package com.bink.wallet.scenes.add_auth_enrol

import com.bink.wallet.model.request.membership_card.PlanFieldsRequest
import com.bink.wallet.model.response.membership_plan.PlanDocument
import com.bink.wallet.model.response.membership_plan.PlanField
import com.bink.wallet.utils.enums.AddAuthItemType

class AddAuthItemWrapper(
    val fieldType: Any,
    val fieldsRequest: PlanFieldsRequest? = null
) {
    fun getFieldType(): AddAuthItemType {
        return when (fieldType) {
            is PlanField -> AddAuthItemType.PLAN_FIELD
            is PlanDocument -> AddAuthItemType.PLAN_DOCUMENT
            else -> AddAuthItemType.HEADER
        }
    }
}