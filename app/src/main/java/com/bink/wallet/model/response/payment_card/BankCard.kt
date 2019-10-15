package com.bink.wallet.model.response.payment_card

import android.os.Parcelable
import com.bink.wallet.utils.StringUtils
import com.bink.wallet.utils.md5
import com.squareup.moshi.JsonClass
import kotlinx.android.parcel.Parcelize

@Parcelize
@JsonClass(generateAdapter = true)
data class BankCard(
    val first_six_digits: String?,
    val last_four_digits: String?,
    val month: Int?,
    val year: Int?,
    val country: String?,
    val currency_code: String?,
    val name_on_card: String?,
    val provider: String?,
    val type: String?,
    val token: String?,
    val fingerprint: String?
) : Parcelable {

    companion object {
        fun tokenGenerator(): String {
            return StringUtils.randomString(100)
        }

        fun fingerprintGenerator(pan: String, expiryYear: String, expiryMonth: String): String {
            // Based a hash of the pan, it's the key identifier of the card
            return "$(pan)|$(expiryMonth)|$(expiryYear)".md5()
        }
    }

}