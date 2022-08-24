package com.bink.wallet.scenes.loyalty_details.locations

import android.app.Application
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import com.bink.wallet.BaseViewModel
import com.bink.wallet.model.tescolocations.Properties
import com.bink.wallet.model.tescolocations.TescoLocation
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import java.io.IOException

class LoyaltyCardLocationsViewModel(private val application: Application) : BaseViewModel() {

    val locations: MutableState<TescoLocation?> = mutableStateOf(null)
    val selectedLocationProperties: MutableState<Properties?> = mutableStateOf(null)

    fun getLocations() {
        try {
            val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
            val adapter = moshi.adapter(TescoLocation::class.java)
            val jsonString =
                application.assets.open("tesco_locations.geojson").bufferedReader()
                    .use { it.readText() }

            adapter.fromJson(jsonString)?.let { tescoLocations ->
                locations.value = tescoLocations
            }
        } catch (e: IOException) {
        }

    }

}