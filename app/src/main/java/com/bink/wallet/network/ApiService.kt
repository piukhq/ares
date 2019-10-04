package com.bink.wallet.network

import com.bink.wallet.model.request.membership_card.MembershipCardRequest
import com.bink.wallet.model.response.membership_card.MembershipCard
import com.bink.wallet.model.response.membership_plan.MembershipPlan
import com.bink.wallet.model.response.payment_card.PaymentCard
import com.bink.wallet.scenes.login.LoginResponse
import kotlinx.coroutines.Deferred
import okhttp3.ResponseBody
import retrofit2.http.*

interface ApiService {
    @GET("/ubiquity/service")
    fun checkRegisteredUser(): Deferred<LoginResponse>

    @POST("/ubiquity/service")
    fun loginOrRegisterAsync(@Body loginResponse: LoginResponse): Deferred<LoginResponse>


    @GET("/ubiquity/membership_cards")
    fun getMembershipCardsAsync(): Deferred<List<MembershipCard>>

    @GET("/ubiquity/payment_cards")
    fun getPaymentCardsAsync(): Deferred<List<PaymentCard>>

    @PATCH("/ubiquity/membership_card/{membershipCardId}/payment_card/{paymentCardId}")
    fun linkToPaymentCardAsync(@Path("membershipCardId") membershipCardId: String, @Path("paymentCardId") paymentCardId: String): Deferred<PaymentCard>

    @DELETE("/ubiquity/payment_card/{paymentCardId}/membership_card/{membershipCardId}")
    fun unlinkFromPaymentCardAsync(@Path("paymentCardId") paymentCardId: String, @Path("membershipCardId") membershipCardId: String): Deferred<ResponseBody>

    @DELETE("/ubiquity/membership_card/{card_id}")
    fun deleteCardAsync(@Path("card_id") cardId: String): Deferred<ResponseBody>

    @GET("/ubiquity/membership_plans")
    fun getMembershipPlansAsync(): Deferred<List<MembershipPlan>>

    @POST("/ubiquity/membership_cards")
    fun createMembershipCardAsync(@Body membershipCardRequest: MembershipCardRequest): Deferred<MembershipCard>

    @PUT("/ubiquity/membership_card/{card_id}")
    fun updateMembershipCardAsync(
        @Path("card_id") cardId: String,
        @Body membershipCardRequest: MembershipCardRequest
    ): Deferred<MembershipCard>
}