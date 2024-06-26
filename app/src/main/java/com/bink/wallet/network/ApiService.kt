package com.bink.wallet.network

import com.bink.wallet.model.MagicLinkAccessToken
import com.bink.wallet.model.MagicLinkBody
import com.bink.wallet.model.MagicLinkToken
import com.bink.wallet.model.PostServiceRequest
import com.bink.wallet.model.auth.User
import com.bink.wallet.model.request.MarketingOption
import com.bink.wallet.model.request.Preference
import com.bink.wallet.model.request.SignUpRequest
import com.bink.wallet.model.request.forgot_password.ForgotPasswordRequest
import com.bink.wallet.model.request.membership_card.MembershipCardRequest
import com.bink.wallet.model.response.SignUpResponse
import com.bink.wallet.model.response.membership_card.MembershipCard
import com.bink.wallet.model.response.membership_plan.MembershipPlan
import com.bink.wallet.model.response.payment_card.PaymentCard
import com.bink.wallet.model.response.payment_card.PaymentCardAdd
import com.bink.wallet.scenes.login.LoginResponse
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.http.*

interface ApiService {

    @POST("/ubiquity/service")
    suspend fun loginOrRegisterAsync(
        @Body loginResponse: LoginResponse
    ): LoginResponse

    @DELETE("/ubiquity/service")
    suspend fun deleteUser(): ResponseBody

    @POST("/users/forgotten_password/")
    suspend fun forgotPasswordAsync(
        @Body forgotPasswordRequest: ForgotPasswordRequest
    ): ResponseBody

    @GET("/ubiquity/membership_cards")
    suspend fun getMembershipCardsAsync(): List<MembershipCard>

    @GET("/ubiquity/payment_cards")
    suspend fun getPaymentCardsAsync(): List<PaymentCard>

    @POST("/ubiquity/payment_cards")
    suspend fun addPaymentCardAsync(
        @Body cardAdd: PaymentCardAdd, @Query("autoLink") autoLink: Boolean = true
    ): PaymentCard

    @PATCH("/ubiquity/membership_card/{membershipCardId}/payment_card/{paymentCardId}")
    suspend fun linkToPaymentCardAsync(
        @Path("membershipCardId") membershipCardId: String,
        @Path("paymentCardId") paymentCardId: String
    ): PaymentCard

    @DELETE("/ubiquity/payment_card/{paymentCardId}/membership_card/{membershipCardId}")
    suspend fun unlinkFromPaymentCardAsync(
        @Path("paymentCardId") paymentCardId: String,
        @Path("membershipCardId") membershipCardId: String
    ): ResponseBody

    @DELETE("/ubiquity/membership_card/{card_id}")
    suspend fun deleteCardAsync(
        @Path("card_id") cardId: String
    ): ResponseBody

    @GET("/ubiquity/membership_plans")
    suspend fun getMembershipPlansAsync(): List<MembershipPlan>

    @POST("/ubiquity/membership_cards")
    suspend fun createMembershipCardAsync(
        @Body membershipCardRequest: MembershipCardRequest
    ): MembershipCard

    @PUT("/ubiquity/membership_card/{card_id}")
    suspend fun updateMembershipCardAsync(
        @Path("card_id") cardId: String,
        @Body membershipCardRequest: MembershipCardRequest
    ): MembershipCard

    @PATCH("/ubiquity/membership_card/{card_id}")
    suspend fun ghostMembershipCardAsync(
        @Path("card_id") cardId: String,
        @Body membershipCardRequest: MembershipCardRequest
    ): MembershipCard

    @DELETE("/ubiquity/payment_card/{payment_id}")
    suspend fun deletePaymentCardAsync(
        @Path("payment_id") cardId: String
    ): ResponseBody

    @POST("/ubiquity/service")
    suspend fun postServiceAsync(@Body requestRequest: PostServiceRequest): ResponseBody

    @GET("/ubiquity/payment_card/{payment_id}")
    suspend fun getPaymentCardAsync(
        @Path("payment_id") cardId: String
    ): PaymentCard

    @POST("/users/register")
    suspend fun signUpAsync(
        @Body signUpRequest: SignUpRequest
    ): SignUpResponse

    @PUT("/users/me/settings")
    suspend fun checkMarketingPrefAsync(
        @Body checkedOption: MarketingOption
    ): ResponseBody

    @POST("/users/login")
    suspend fun logInAsync(
        @Body signUpRequest: SignUpRequest
    ): SignUpResponse

    @POST("/users/me/logout")
    suspend fun logOutAsync(): ResponseBody

    @POST("/users/magic_links")
    suspend fun postMagicLink(@Body magicLinkBody: MagicLinkBody)

    @POST("/users/magic_links/access_tokens")
    suspend fun postMagicLinkToken(@Body magicLinkToken: MagicLinkToken): MagicLinkAccessToken

    @GET("/users/me/settings")
    suspend fun getPreferencesAsync(): List<Preference>

    @PUT("/users/me/settings")
    suspend fun putPreferencesAsync(
        @Body preferenceRequest: RequestBody
    ): ResponseBody

    @PUT("/users/me")
    suspend fun putUserDetailsAsync(
        @Body userRequest: User
    ): User

    @GET("/users/me")
    suspend fun getUserAsync(): User
}