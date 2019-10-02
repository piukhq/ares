package com.bink.wallet.model.request.membership_card

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Account(
    var add_fields: MutableList<PlanFields>?,
    var authorise_fields: MutableList<PlanFields>?,
    var enrol_fields: MutableList<PlanFields>?,
    var registration_fields: MutableList<PlanFields>?
) {
    constructor() : this(
        ArrayList(),
        ArrayList(),
        ArrayList(),
        ArrayList()
    )
}