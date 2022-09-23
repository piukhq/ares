package com.bink.wallet.scenes.loyalty_details.locations

import android.app.Application
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import com.bink.wallet.BaseViewModel
import com.bink.wallet.model.LocationOpeningHours
import com.bink.wallet.model.tescolocations.Properties
import com.bink.wallet.model.tescolocations.TescoLocation
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import java.io.IOException
import java.lang.reflect.Type

class LoyaltyCardLocationsViewModel(private val application: Application) : BaseViewModel() {

    val locations: MutableState<TescoLocation?> = mutableStateOf(null)
    val selectedLocationProperties: MutableState<Properties?> = mutableStateOf(null)

    fun getLocations(geoJson: String) {
        try {
            val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
            val adapter = moshi.adapter(TescoLocation::class.java)
            adapter.fromJson(geoJson)?.let { tescoLocations ->
                locations.value = tescoLocations
            }
        } catch (e: IOException) {
        }

    }

    fun getLocationOpeningHours(properties: Properties): LocationOpeningHours {
        val gson = Gson()
        val type: Type = object : TypeToken<LocationOpeningHours>() {}.type
        val openingTimesString = properties.openHours?.replace("\\", "")
        return gson.fromJson(openingTimesString, type)
    }

}