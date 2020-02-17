package com.bink.wallet.network

import com.bink.wallet.model.spreedly.SpreedlyPaymentCard
import com.bink.wallet.model.spreedly.response.SpreedlyResponse
import kotlinx.coroutines.Deferred
import retrofit2.http.*

interface ApiSpreedly {

    //todo encrypt env key
    @Headers("Content-Type: application/json")
    @POST("/v1/payment_methods.json")
    fun postPaymentCardToSpreedly(
        @Body spreedlyCard: SpreedlyPaymentCard,
        @Query("environment_key") environmentKey: String
    ): Deferred<SpreedlyResponse>
}