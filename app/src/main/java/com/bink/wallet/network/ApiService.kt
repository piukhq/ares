package com.bink.wallet.network

import com.bink.wallet.model.request.membership_card.MembershipCardRequest
import com.bink.wallet.model.response.membership_card.MembershipCard
import com.bink.wallet.model.response.membership_plan.MembershipPlan
import com.bink.wallet.scenes.login.LoginResponse
import kotlinx.coroutines.Deferred
import okhttp3.ResponseBody
import retrofit2.http.*

interface ApiService {
    @GET("/ubiquity/service")
    fun checkRegisteredUser(): Deferred<LoginResponse>

    @POST("/ubiquity/service")
    fun loginOrRegisterAsync(@Body loginResponse: LoginResponse): Deferred<LoginResponse>


    @GET("/ubiquity/membership_cards?fields=id,membership_plan,status,payment_cards,card,account,balances,images")
    fun getMembershipCardsAsync(): Deferred<List<MembershipCard>>

    @DELETE("/ubiquity/membership_card/{card_id}")
    fun deleteCardAsync(@Path("card_id") cardId: String): Deferred<ResponseBody>

    @GET("/ubiquity/membership_plans?fields=id,status,feature_set,account,images,balances")
    fun getMembershipPlansAsync(): Deferred<List<MembershipPlan>>

    @POST("/ubiquity/membership_cards")
    fun createMembershipCardAsync(@Body membershipCardRequest: MembershipCardRequest): Deferred<MembershipCard>
}