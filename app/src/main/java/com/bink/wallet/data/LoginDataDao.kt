package com.bink.wallet.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.bink.wallet.model.LoginData

@Dao
interface LoginDataDao {
    @Query("SELECT * FROM login_data")
    suspend fun getLoginData(): LoginData

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun store(loginData: LoginData?)

    @Query("DELETE FROM login_data")
    suspend fun deleteEmails()

}