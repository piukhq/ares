package com.bink.wallet.di

import androidx.room.Room
import com.bink.wallet.data.BinkDatabase
import org.koin.android.ext.koin.androidApplication
import org.koin.dsl.module

val dataModule = module {
    single {
        Room.databaseBuilder(androidApplication(), BinkDatabase::class.java, "bink-db")
            .fallbackToDestructiveMigration()
            .build()

    }

    single { get<BinkDatabase>().membershipPlanDao() }
    single { get<BinkDatabase>().membershipCardDao() }
    single { get<BinkDatabase>().paymentCardDao() }
}
