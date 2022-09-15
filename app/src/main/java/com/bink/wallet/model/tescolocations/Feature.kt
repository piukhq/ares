package com.bink.wallet.model.tescolocations


import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.clustering.ClusterItem
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Feature(
    @Json(name = "geometry")
    val geometry: Geometry?,
    @Json(name = "properties")
    val properties: Properties?,
    @Json(name = "type")
    val type: String?,
) : ClusterItem {
    override fun getPosition(): LatLng {
        return LatLng((geometry?.coordinates?.get(1) ?: 0.0), (geometry?.coordinates?.get(0) ?: 0.0))
    }

    override fun getTitle(): String {
        return ""
    }

    override fun getSnippet(): String {
        return ""
    }
}