package com.bink.wallet.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.bink.wallet.scenes.loyalty_wallet.model.MembershipCard

@Dao
interface MembershipCardDao {
    @Query("SELECT * FROM membership_card")
    fun getAll(): List<MembershipCard>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun storeAll(membershipCards: List<MembershipCard>)
}