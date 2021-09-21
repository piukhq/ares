package com.bink.wallet.model

import com.google.gson.annotations.SerializedName

data class PointScrapingResponse(
    @SerializedName("points")
    var pointsString: String?,
    @SerializedName("did_attempt_login")
    var didAttemptLogin: Boolean?,
    @SerializedName("error_message")
    var errorMessage: String?
//    @SerializedName("points")
//    var userActionRequired: Boolean?,
//    @SerializedName("points")
//    var userActionComplete: Boolean
)