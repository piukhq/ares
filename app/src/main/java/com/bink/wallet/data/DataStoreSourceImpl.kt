package com.bink.wallet.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.bink.wallet.utils.ThemeHelper
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class DataStoreSourceImpl(private val dataStore:DataStore<Preferences>) : DataStoreSource {

    companion object{
        val MODE_STATUS = stringPreferencesKey("MODE_STATUS")
    }

    override suspend fun saveSelectedTheme(theme: String) {
        dataStore.edit {
            it[MODE_STATUS] = theme
        }
    }

    override suspend fun getCurrentlySelectedTheme(): Flow<String> {
        return dataStore.data.map {
            it[MODE_STATUS] ?: ThemeHelper.SYSTEM //Default of Android Q , without this code, Android Q can use this also
        }
    }
}