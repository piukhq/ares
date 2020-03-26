package com.bink.wallet.model.response.payment_card

import android.os.Parcelable
import com.bink.wallet.utils.StringUtils
import com.bink.wallet.utils.md5
import com.squareup.moshi.JsonClass
import kotlinx.android.parcel.Parcelize
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
    var fingerprint: String?,
    var hash: String?
) : Parcelable {

    companion object {
        const val TOKEN_LENGTH = 100

        fun tokenGenerator(): String {
            return StringUtils.randomString(TOKEN_LENGTH)
        }

        fun fingerprintGenerator(pan: String, expiryYear: String, expiryMonth: String): String {
            // Based a hash of the pan, it's the key identifier of the card
            return "$pan|$expiryMonth|$expiryYear".md5()
        }
    }

    fun isExpired(): Boolean {
        val cal = Calendar.getInstance()
//        if (year != null && month != null) {
//            if (year < cal.get(Calendar.YEAR) ||
//                (year == cal.get(Calendar.YEAR) &&
//                 month <= cal.get(Calendar.MONTH))) {
//                return true
//            }
//        }
        return false
    }
}