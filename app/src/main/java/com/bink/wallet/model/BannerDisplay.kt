package com.bink.wallet.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "dismissed_banner")
class BannerDisplay(
    @PrimaryKey var id: String
)