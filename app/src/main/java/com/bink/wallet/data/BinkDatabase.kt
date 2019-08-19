package com.bink.wallet.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.bink.wallet.scenes.browse_brands.model.MembershipPlan
import com.bink.wallet.scenes.loyalty_wallet.model.MembershipCard

@Database(entities = [MembershipCard::class, MembershipPlan::class], version = 1)
@TypeConverters(MembershipCardConverters::class, MembershipPlanConverters::class)
abstract class BinkDatabase : RoomDatabase() {
    abstract fun membershipCardDao(): MembershipCardDao
    abstract fun membershipPlanDao(): MembershipPlanDao
}