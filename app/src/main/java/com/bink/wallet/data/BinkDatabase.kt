package com.bink.wallet.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.bink.wallet.model.BannerDisplay
import com.bink.wallet.model.LoginData
import com.bink.wallet.model.response.membership_card.MembershipCard
import com.bink.wallet.model.response.membership_plan.MembershipPlan
import com.bink.wallet.model.response.payment_card.PaymentCard
import com.bink.wallet.utils.LocalPointScraping.WebScrapeCredentials

@Database(
    entities = [MembershipCard::class, MembershipPlan::class, LoginData::class, PaymentCard::class, BannerDisplay::class, WebScrapeCredentials::class],
    version = 21,
    exportSchema = false
)
@TypeConverters(
    MembershipCardConverters::class,
    MembershipPlanConverters::class,
    LoginDataConverters::class,
    PaymentCardConverters::class
)
abstract class BinkDatabase : RoomDatabase() {
    abstract fun membershipCardDao(): MembershipCardDao
    abstract fun membershipPlanDao(): MembershipPlanDao
    abstract fun paymentCardDao(): PaymentCardDao
    abstract fun loginDataDao(): LoginDataDao
    abstract fun bannersDisplayDao(): BannersDisplayDao
    abstract fun webScrapeDao(): WebScrapeDao
}