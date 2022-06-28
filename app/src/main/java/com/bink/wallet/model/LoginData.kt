package com.bink.wallet.model

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.JsonClass
import kotlinx.parcelize.Parcelize

@Parcelize
@JsonClass(generateAdapter = true)
@Entity(tableName = "login_data")
class LoginData(
    @PrimaryKey @ColumnInfo(name = "id") var id: String,
    @ColumnInfo(name = "email") val email: String?
) : Parcelable