package com.bink.wallet.scenes.add_auth_enrol

import com.bink.wallet.model.request.membership_card.PlanFieldsRequest
import com.bink.wallet.model.response.membership_plan.PlanField

data class FormField(var planField: PlanField, val fieldsRequest: PlanFieldsRequest? = null,var isValidField : Boolean = false)