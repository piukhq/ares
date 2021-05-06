package com.bink.wallet.scenes.add_auth_enrol

import com.bink.wallet.model.request.membership_card.PlanFieldsRequest

data class FormField(var fieldType:Any,val fieldsRequest: PlanFieldsRequest? = null) {
}