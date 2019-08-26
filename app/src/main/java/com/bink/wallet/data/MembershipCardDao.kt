package com.bink.wallet.data

import androidx.room.*
import com.bink.wallet.model.response.membership_card.MembershipCard

@Dao
interface MembershipCardDao {
    @Query("SELECT * FROM membership_card")
    suspend fun getAllAsync(): List<MembershipCard>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun storeAll(membershipCards: List<MembershipCard>)
}