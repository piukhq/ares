package com.bink.wallet.scenes.loyalty_details.locations

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import com.bink.wallet.BaseViewModel
import com.bink.wallet.model.LocationOpeningHours
import com.bink.wallet.model.locations.MerchantLocation
import com.bink.wallet.model.locations.Properties
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.lang.reflect.Type
import java.nio.charset.StandardCharsets

class LoyaltyCardLocationsViewModel : BaseViewModel() {

    private val _locationUiState = MutableStateFlow(LocationUiState())
    val locationUiState: StateFlow<LocationUiState> = _locationUiState.asStateFlow()

    val selectedLocationProperties: MutableState<Properties?> = mutableStateOf(null)


    fun getLocations(name: String) {
        _locationUiState.update {
            it.copy(companyName = name)
        }

        val storageRef =
            Firebase.storage.reference.child("locations/${name.lowercase()}.geojson")

        storageRef.getBytes(2048 * 2048).addOnSuccessListener { byteArray ->
            val jsonString = String(byteArray, StandardCharsets.UTF_8)

            val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
            val adapter = moshi.adapter(MerchantLocation::class.java)


            adapter.fromJson(jsonString)?.let { loc ->
                _locationUiState.update {
                    it.copy(location = loc)
                }
            }
        }.addOnFailureListener {
        }
    }

    fun getLocationOpeningHours(properties: Properties): LocationOpeningHours {
        val gson = Gson()
        val type: Type = object : TypeToken<LocationOpeningHours>() {}.type
        val openingTimesString = properties.openHours?.replace("\\", "")
        return gson.fromJson(openingTimesString, type)
    }

}

data class LocationUiState(val location: MerchantLocation? = null, val companyName: String? = null)