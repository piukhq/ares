package com.bink.wallet.data

import androidx.room.*
import com.bink.wallet.scenes.loyalty_wallet.model.MembershipCard


@Dao
interface MembershipCardDao {
    @Query("SELECT * FROM membership_card")
    suspend fun getAllAsync(): List<MembershipCard>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun storeAll(membershipCards: List<MembershipCard>?)

    @Query("DELETE FROM membership_card WHERE id = :membershipCardId")
    suspend fun deleteCard(membershipCardId: String)
}