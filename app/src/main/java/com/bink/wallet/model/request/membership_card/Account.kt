package com.bink.wallet.model.request.membership_card

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Account(
    var add_fields: MutableList<PlanFieldsRequest>?,
    var authorise_fields: MutableList<PlanFieldsRequest>?,
    var enrol_fields: MutableList<PlanFieldsRequest>?,
    var registration_fields: MutableList<PlanFieldsRequest>?,
    var plan_documents: MutableList<PlanFieldsRequest>?
) {
    constructor() : this(
        ArrayList(),
        ArrayList(),
        ArrayList(),
        ArrayList(),
        ArrayList()
    )
}