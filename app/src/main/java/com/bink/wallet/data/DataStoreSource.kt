package com.bink.wallet.data

import kotlinx.coroutines.flow.Flow

interface DataStoreSource {
    suspend fun saveSelectedTheme(theme : String)
    suspend fun getCurrentlySelectedTheme(): Flow<String>
}