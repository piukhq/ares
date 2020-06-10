package com.bink.wallet.data

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

class BinkDatabaseMigrations {

    companion object {
        val MIGRATION_17_18: Migration = object : Migration(17, 18) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE membership_plan ADD COLUMN card")
            }
        }
    }

}