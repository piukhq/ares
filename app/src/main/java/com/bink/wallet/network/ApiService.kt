package com.bink.wallet.network

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
import kotlinx.coroutines.Deferred
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {

    @POST("/ubiquity/service")
    fun loginOrRegisterAsync(
        @Body loginResponse: LoginResponse
    ): Deferred<LoginResponse>

    @POST("/users/forgotten_password/")
    suspend fun forgotPasswordAsync(
        @Body forgotPasswordRequest: ForgotPasswordRequest
    ): ResponseBody

    @GET("/ubiquity/membership_cards")
    suspend fun getMembershipCardsAsync(): List<MembershipCard>

    @GET("/ubiquity/payment_cards")
    fun getPaymentCardsAsync(): Deferred<List<PaymentCard>>

    @POST("/ubiquity/payment_cards")
    fun addPaymentCardAsync(
        @Body cardAdd: PaymentCardAdd,@Query("autoLink") autoLink:Boolean = true
    ): Deferred<PaymentCard>

    @PATCH("/ubiquity/membership_card/{membershipCardId}/payment_card/{paymentCardId}")
    suspend fun linkToPaymentCardAsync(
        @Path("membershipCardId") membershipCardId: String,
        @Path("paymentCardId") paymentCardId: String
    ): PaymentCard

    @DELETE("/ubiquity/payment_card/{paymentCardId}/membership_card/{membershipCardId}")
    fun unlinkFromPaymentCardAsync(
        @Path("paymentCardId") paymentCardId: String,
        @Path("membershipCardId") membershipCardId: String
    ): Deferred<ResponseBody>

    @DELETE("/ubiquity/membership_card/{card_id}")
    fun deleteCardAsync(
        @Path("card_id") cardId: String
    ): Deferred<ResponseBody>

    @GET("/ubiquity/membership_plans")
    suspend fun getMembershipPlansAsync(): List<MembershipPlan>

    @POST("/ubiquity/membership_cards")
    fun createMembershipCardAsync(
        @Body membershipCardRequest: MembershipCardRequest
    ): Deferred<MembershipCard>

    @PUT("/ubiquity/membership_card/{card_id}")
    fun updateMembershipCardAsync(
        @Path("card_id") cardId: String,
        @Body membershipCardRequest: MembershipCardRequest
    ): Deferred<MembershipCard>

    @PATCH("/ubiquity/membership_card/{card_id}")
    fun ghostMembershipCardAsync(
        @Path("card_id") cardId: String,
        @Body membershipCardRequest: MembershipCardRequest
    ): Deferred<MembershipCard>

    @DELETE("/ubiquity/payment_card/{payment_id}")
    fun deletePaymentCardAsync(
        @Path("payment_id") cardId: String
    ): Deferred<ResponseBody>

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