package com.bink.wallet.network

import com.bink.wallet.model.spreedly.SpreedlyPaymentCard
import com.bink.wallet.model.spreedly.response.SpreedlyResponse
import kotlinx.coroutines.Deferred
import retrofit2.http.*

interface ApiSpreedly {

    //todo encrypt env key
    @Headers("Content-Type: application/json")
    @POST("/v1/payment_methods.json?environment_key=1Lf7DiKgkcx5Anw7QxWdDxaKtTa")
    fun postPaymentCardToSpreedly(
        @Body spreedlyCard: SpreedlyPaymentCard
    ): Deferred<SpreedlyResponse>
}