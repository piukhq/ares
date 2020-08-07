package com.bink.wallet.network

import com.bink.wallet.model.spreedly.SpreedlyPaymentCard
import com.bink.wallet.model.spreedly.response.SpreedlyResponse
import kotlinx.coroutines.Deferred
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Query

interface ApiSpreedly {

    @Headers("Content-Type: application/json")
    @POST("https://core.spreedly.com/v1/payment_methods.json")
    fun postPaymentCardToSpreedly(
        @Body spreedlyCard: SpreedlyPaymentCard,
        @Query("environment_key") environmentKey: String
    ): Deferred<SpreedlyResponse>
}