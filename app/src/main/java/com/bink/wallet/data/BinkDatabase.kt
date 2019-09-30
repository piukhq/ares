package com.bink.wallet.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.bink.wallet.model.response.membership_card.MembershipCard
import com.bink.wallet.model.response.membership_plan.MembershipPlan
import com.bink.wallet.model.response.payment_card.PaymentCard

@Database(entities = [MembershipCard::class, MembershipPlan::class, PaymentCard:: class], version = 9, exportSchema = false)
@TypeConverters(MembershipCardConverters::class, MembershipPlanConverters::class, PaymentCardConverters::class)
abstract class BinkDatabase : RoomDatabase() {
    abstract fun membershipCardDao(): MembershipCardDao
    abstract fun membershipPlanDao(): MembershipPlanDao
    abstract fun paymentCardDao(): PaymentCardDao
}