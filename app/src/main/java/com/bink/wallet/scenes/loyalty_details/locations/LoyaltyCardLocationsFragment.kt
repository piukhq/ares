package com.bink.wallet.scenes.loyalty_details.locations

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.bink.wallet.BaseFragment
import com.bink.wallet.R
import com.bink.wallet.databinding.LoyaltyCardLocationFragmentBinding
import com.bink.wallet.model.getCurrentOpeningTimes
import com.bink.wallet.model.tescolocations.Feature
import com.bink.wallet.model.tescolocations.Properties
import com.bink.wallet.utils.MixpanelEvents
import com.bink.wallet.utils.noRippleClickable
import com.bink.wallet.utils.nunitoSans
import com.bink.wallet.utils.showDialog
import com.bink.wallet.utils.toolbar.FragmentToolbar
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.maps.android.clustering.ClusterManager
import com.google.maps.android.compose.*
import org.json.JSONObject
import org.koin.androidx.viewmodel.ext.android.viewModel


@SuppressLint("MissingPermission")
class LoyaltyCardLocationsFragment : BaseFragment<LoyaltyCardLocationsViewModel, LoyaltyCardLocationFragmentBinding>() {

    companion object {
        private const val REQUEST_CHECK_SETTINGS = 100
        private const val LOCATION_REQUEST_INTERVAL: Long = 5000
    }

    private var fusedLocationProvider: FusedLocationProviderClient? = null
    private val mapProperties = mutableStateOf(MapProperties(isMyLocationEnabled = false))
    private val uiSettings = mutableStateOf(MapUiSettings(myLocationButtonEnabled = false))

    //Defaulted to London
    private val cameraPos = mutableStateOf(
        LatLng(
            51.54,
            -0.14
        )
    )

    private val permissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            when {
                permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false -> {
                    manageLocation()
                }
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false -> {
                    manageLocation()
                }
            }

            setMapCurrentLocationSettings()
        }

    override val layoutRes: Int
        get() = R.layout.loyalty_card_location_fragment
    override val viewModel: LoyaltyCardLocationsViewModel by viewModel()

    override fun builder(): FragmentToolbar {
        return FragmentToolbar.Builder()
            .withId(FragmentToolbar.NO_TOOLBAR)
            .build()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.getLocations()

        fusedLocationProvider = LocationServices.getFusedLocationProviderClient(requireContext())
        checkLocationPermission()

        binding.compose.setContent {
            Column(modifier = Modifier.fillMaxSize()) {
                BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                    GoogleMapClustering(items = viewModel.locations.value?.features ?: arrayListOf(), modifier = Modifier.fillMaxSize())
                    LocationCard(modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = dimensionResource(id = R.dimen.margin_padding_size_medium))
                        .clip(RoundedCornerShape(dimensionResource(id = R.dimen.margin_padding_size_medium))))

                }
            }
        }

        logMixpanelEvent(
            MixpanelEvents.SHOW_LOCATIONS,
            JSONObject().put(
                MixpanelEvents.BRAND_NAME,
                "Tesco"
            )
        )
    }

    @OptIn(MapsComposeExperimentalApi::class)
    @Composable
    fun GoogleMapClustering(items: List<Feature>, modifier: Modifier) {
        val cameraPosState = rememberCameraPositionState {
            position = CameraPosition.fromLatLngZoom(
                cameraPos.value, 10f
            )
        }

        GoogleMap(
            modifier = modifier,
            cameraPositionState = cameraPosState,
            uiSettings = uiSettings.value,
            properties = mapProperties.value,
            onMapClick = {
                viewModel.selectedLocationProperties.value = null
            })
        {
            val context = LocalContext.current
            var clusterManager by remember { mutableStateOf<ClusterManager<Feature>?>(null) }

            clusterManager?.setOnClusterItemClickListener {
                viewModel.selectedLocationProperties.value = it.properties
                true
            }
            MapEffect(items) { map ->
                clusterManager = ClusterManager<Feature>(context, map)
                clusterManager?.addItems(items)
            }
            LaunchedEffect(key1 = cameraPosState.isMoving) {
                if (!cameraPosState.isMoving) {
                    clusterManager?.onCameraIdle()
                }
            }
        }
    }

    @Composable
    private fun LocationCard(modifier: Modifier) {
        AnimatedVisibility(visible = viewModel.selectedLocationProperties.value != null, modifier = modifier) {
            viewModel.selectedLocationProperties.value?.let { properties ->

                val locationOpenTitle = requireContext().getCurrentOpeningTimes(viewModel.getLocationOpeningHours(properties))
                val locationOpenTitleColour = when {
                    locationOpenTitle.contains(getString(R.string.location_open_title)) -> R.color.green_ok
                    locationOpenTitle.contains(getString(R.string.location_closed_title)) -> R.color.red_attention
                    else -> R.color.orange
                }

                Row(modifier = modifier
                    .background(Color.White)
                    .padding(dimensionResource(id = R.dimen.margin_padding_size_medium))
                    .noRippleClickable {

                        logMixpanelEvent(
                            MixpanelEvents.SHOW_LOCATIONS,
                            JSONObject().put(
                                MixpanelEvents.SHOW_DIRECTIONS,
                                "Tesco"
                            )
                        )

                        try {
                            val intent = Intent(Intent.ACTION_VIEW,
                                Uri.parse("geo:0,0?q=${properties.latitude},${properties.longitude} (${"${properties.locationName} - ${properties.city}"})"))
                            startActivity(intent)
                        } catch (e: Exception) {
                            requireContext().showDialog(
                                title = getString(R.string.error),
                                message = getString(R.string.map_error),
                                positiveBtn = getString(R.string.ok))
                        }

                    },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center) {
                    Image(
                        painter = painterResource(R.drawable.location_arrow),
                        contentDescription = "Location",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(dimensionResource(id = R.dimen.map_location_card_icon_size))
                    )
                    Spacer(modifier = Modifier.width(dimensionResource(id = R.dimen.margin_padding_size_medium)))
                    Column(verticalArrangement = Arrangement.Center) {
                        Text(
                            text = "${properties.locationName} - ${properties.city}",
                            fontFamily = nunitoSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )

                        Text(text = locationOpenTitle,
                            color = colorResource(id = locationOpenTitleColour),
                            fontFamily = nunitoSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp)

                        Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.margin_padding_size_small)))

                        Text(
                            text = getString(R.string.map_location_directions_text),
                            fontFamily = nunitoSans,
                            fontSize = 18.sp
                        )
                    }
                }
            }
        }

    }

    private fun turnOnLocation() {
        val builder =
            LocationSettingsRequest.Builder().addLocationRequest(LocationRequest.create().apply {
                interval = LOCATION_REQUEST_INTERVAL
                priority = PRIORITY_HIGH_ACCURACY
            })
        val client = LocationServices.getSettingsClient(requireContext())
        val task = client.checkLocationSettings(builder.build())

        task.addOnFailureListener { exception ->
            if (exception is ResolvableApiException) {
                try {
                    exception.startResolutionForResult(
                        requireActivity(),
                        REQUEST_CHECK_SETTINGS
                    )
                } catch (sendEx: IntentSender.SendIntentException) {
                }
            }
        }
    }

    private fun checkLocationPermission() {
        if (!hasLocationPermission()) {
            if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION) ||
                shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_COARSE_LOCATION)
            ) {
                requireContext().showDialog(
                    title = getString(R.string.location_permission_title),
                    message = getString(R.string.location_permission_settings_message),
                    positiveBtn = getString(R.string.ok),
                    negativeBtn = getString(R.string.cancel_text),
                    positiveCallback = { requestLocationPermission() })
            } else {
                requestLocationPermission()
            }
        } else {
            requestLocationPermission()
        }
    }

    private fun getLocation() {
        fusedLocationProvider?.getCurrentLocation(
            PRIORITY_HIGH_ACCURACY,
            CancellationTokenSource().token
        )?.addOnSuccessListener { location ->
            try {
                cameraPos.value = LatLng(
                    location.latitude,
                    location.longitude
                )
            } catch (e: NullPointerException) {

            }
        }
    }

    private fun requestLocationPermission() {
        permissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    private fun setMapCurrentLocationSettings() {
        if (hasLocationPermission()) {
            mapProperties.value = MapProperties(isMyLocationEnabled = true)
            uiSettings.value = MapUiSettings(myLocationButtonEnabled = true)
            getLocation()
        } else {
            mapProperties.value = MapProperties(isMyLocationEnabled = false)
            uiSettings.value = MapUiSettings(myLocationButtonEnabled = false)
        }
    }

    private fun manageLocation() {
        if (isLocationTurnedOn()) {
            getLocation()
        } else {
            turnOnLocation()
        }
    }

    private fun hasLocationPermission(): Boolean =
        ContextCompat.checkSelfPermission(
            requireContext(), Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED

    private fun isLocationTurnedOn(): Boolean =
        (requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager)
            .isProviderEnabled(LocationManager.GPS_PROVIDER)
}
