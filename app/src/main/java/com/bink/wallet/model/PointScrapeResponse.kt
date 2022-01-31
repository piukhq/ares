package com.bink.wallet.model

import com.google.gson.annotations.SerializedName

data class PointScrapingResponse(
    @SerializedName("points")
    var pointsString: String?,
    @SerializedName("did_attempt_login")
    var didAttemptLogin: Boolean?,
    @SerializedName("error_message")
    var errorMessage: String?,
    @SerializedName("user_action_required")
    var userActionRequired: Boolean?,
    @SerializedName("user_action_complete")
    var userActionComplete: Boolean?
)