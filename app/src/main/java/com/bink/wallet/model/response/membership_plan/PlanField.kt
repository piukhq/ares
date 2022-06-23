package com.bink.wallet.model.response.membership_plan

import android.os.Parcelable
import com.bink.wallet.utils.enums.FieldType
import com.bink.wallet.utils.enums.TypeOfField
import com.squareup.moshi.JsonClass
import kotlinx.parcelize.Parcelize

@Parcelize
@JsonClass(generateAdapter = true)
data class PlanField(
    val column: String?,
    val validation: String?,
    val common_name: String?,
    val type: Int?,
    val choice: List<String>?,
    val description: String?,
    var typeOfField: TypeOfField?,
    val alternatives: List<String>?
) : Parcelable {

    var alternativePlanField: PlanField? = null

    fun isBooleanType(): Boolean =
        type == FieldType.BOOLEAN_OPTIONAL.type || type == FieldType.BOOLEAN_REQUIRED.type
}