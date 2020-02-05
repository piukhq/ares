package com.bink.wallet.network

import com.bink.wallet.model.auth.FacebookAuthRequest
import com.bink.wallet.model.auth.FacebookAuthResponse
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
import retrofit2.http.*

interface ApiService {
    @GET("/ubiquity/service")
    fun checkRegisteredUser(): Deferred<LoginResponse>

    @POST("/ubiquity/service")
    fun loginOrRegisterAsync(
        @Body loginResponse: LoginResponse
    ): Deferred<LoginResponse>

    @POST("/users/forgotten_password/")
    fun forgotPasswordAsync(
        @Body forgotPasswordRequest: ForgotPasswordRequest
    ): Deferred<ResponseBody>

    @GET("/ubiquity/membership_cards")
    fun getMembershipCardsAsync(): Deferred<List<MembershipCard>>

    @GET("/ubiquity/payment_cards")
    fun getPaymentCardsAsync(): Deferred<List<PaymentCard>>

    @POST("/ubiquity/payment_cards")
    fun addPaymentCardAsync(
        @Body cardAdd: PaymentCardAdd
    ): Deferred<PaymentCard>

    @PATCH("/ubiquity/membership_card/{membershipCardId}/payment_card/{paymentCardId}")
    fun linkToPaymentCardAsync(
        @Path("membershipCardId") membershipCardId: String,
        @Path("paymentCardId") paymentCardId: String
    ): Deferred<PaymentCard>

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
    fun getMembershipPlansAsync(): Deferred<List<MembershipPlan>>

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

    @GET("/ubiquity/payment_card/{payment_id}")
    fun getPaymentCardAsync(
        @Path("payment_id") cardId: String
    ): Deferred<PaymentCard>

    @POST("/users/auth/facebook")
    fun authWithFacebookAsync(
        @Body facebookAuthRequest: FacebookAuthRequest
    ): Deferred<FacebookAuthResponse>

    @POST("/users/register")
    fun signUpAsync(
        @Body signUpRequest: SignUpRequest
    ): Deferred<SignUpResponse>

    @PUT("/users/me/settings")
    fun checkMarketingPrefAsync(
        @Body checkedOption: MarketingOption
    ): Deferred<ResponseBody>

    @POST("/users/login")
    fun logInAsync(
        @Body signUpRequest: SignUpRequest
    ): Deferred<SignUpResponse>

    @POST("/users/me/logout")
    fun logOutAsync(): Deferred<ResponseBody>

    @GET("/users/me/settings")
    fun getPreferencesAsync(): Deferred<List<Preference>>

    @PUT("/users/me/settings")
    fun putPreferencesAsync(
        @Body preferenceRequest: RequestBody
    ): Deferred<ResponseBody>
}