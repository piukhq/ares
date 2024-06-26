package com.bink.wallet.model.response.membership_plan

import android.os.Parcelable
import com.squareup.moshi.JsonClass
import kotlinx.parcelize.Parcelize

@Parcelize
@JsonClass(generateAdapter = true)
data class Account(
    val plan_name: String?,
    val plan_name_card: String?,
    val plan_url: String?,
    val plan_summary: String?,
    val plan_description: String?,
    val plan_documents: List<PlanDocument>?,
    val barcode_redeem_instructions: String?,
    val plan_register_info: String?,
    val company_name: String?,
    val company_url: String?,
    val enrol_incentive: String?,
    val category: String?,
    val tiers: List<Tiers>?,
    val add_fields: List<PlanField>?,
    val authorise_fields: List<PlanField>?,
    val registration_fields: List<PlanField>?,
    val enrol_fields: List<PlanField>?
) : Parcelable