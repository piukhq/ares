package com.bink.wallet.model.response.payment_card

import android.os.Parcelable
import com.bink.wallet.utils.StringUtils
import com.bink.wallet.utils.md5
import com.squareup.moshi.JsonClass
import kotlinx.parcelize.Parcelize
import java.util.*

@Parcelize
@JsonClass(generateAdapter = true)
data class BankCard(
    var first_six_digits: String?,
    var last_four_digits: String?,
    var month: String?,
    var year: String?,
    val country: String?,
    val currency_code: String?,
    val name_on_card: String?,
    val provider: String?,
    val type: String?,
    var token: String?,
    var fingerprint: String?
) : Parcelable {

    companion object {
        const val TOKEN_LENGTH = 100
    }

    fun isExpired(): Boolean {
        val cal = Calendar.getInstance()
        year?.let { safeYear ->
            month?.let { safeMonth ->
                val comparableYear = safeYear.toInt()
                val comparableMonth = safeMonth.toInt()
                if (year != null && month != null) {
                    if (comparableYear < cal.get(Calendar.YEAR) ||
                        (comparableYear == cal.get(Calendar.YEAR) &&
                                comparableMonth <= cal.get(Calendar.MONTH))
                    ) {
                        return true
                    }
                }
            }
        }

        return false
    }
}