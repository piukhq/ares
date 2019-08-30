package com.bink.wallet.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.bink.wallet.model.response.membership_plan.MembershipPlan
import com.bink.wallet.model.response.membership_card.MembershipCard

@Database(entities = [MembershipCard::class, MembershipPlan::class], version = 4)
@TypeConverters(MembershipCardConverters::class, MembershipPlanConverters::class)
abstract class BinkDatabase : RoomDatabase() {
    abstract fun membershipCardDao(): MembershipCardDao
    abstract fun membershipPlanDao(): MembershipPlanDao
}