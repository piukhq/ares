package com.bink.wallet.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.bink.wallet.scenes.browse_brands.model.MembershipPlan

@Dao
interface MembershipPlanDao {
    @Query("SELECT * FROM membership_plan")
    suspend fun getAllAsync(): List<MembershipPlan>

    @Query("SELECT * FROM membership_plan WHERE id= :id ")
    fun getPlanById(id:String): MembershipPlan

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun storeAll(plans: List<MembershipPlan>)
}