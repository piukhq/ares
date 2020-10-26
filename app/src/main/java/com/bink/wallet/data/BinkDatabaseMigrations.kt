package com.bink.wallet.data

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

class BinkDatabaseMigrations {

    companion object {
        val MIGRATION_17_18: Migration = object : Migration(17, 18) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE membership_plan ADD COLUMN card TEXT")
            }
        }

        val MIGRATION_18_19: Migration = object : Migration(18, 19) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE membership_plan ADD COLUMN content TEXT ")
            }

        }

        val MIGRATION_19_20: Migration = object : Migration(19, 20) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE payment_card ADD COLUMN uuid TEXT")
                database.execSQL("ALTER TABLE membership_card ADD COLUMN uuid TEXT")
            }
        }
    }

}