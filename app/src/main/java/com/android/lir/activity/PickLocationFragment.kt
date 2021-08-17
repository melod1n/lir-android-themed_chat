package com.android.lir.activity

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.viewbinding.library.fragment.viewBinding
import android.widget.EditText
import androidx.annotation.DrawableRes
import androidx.appcompat.app.AlertDialog
import androidx.core.content.res.ResourcesCompat
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.setFragmentResult
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.android.lir.R
import com.android.lir.base.BaseFragment
import com.android.lir.databinding.FragmentPickLocationBinding
import com.android.lir.utils.toBitmapWithBadge
import com.github.florent37.runtimepermission.kotlin.askPermission
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.gms.tasks.Task
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.fragment_pick_location.*
import kotlinx.android.synthetic.main.view_search.*
import kotlinx.coroutines.launch
import java.util.*

class PickLocationFragment : BaseFragment(R.layout.fragment_pick_location),
    OnMapReadyCallback {

    private val markers = mutableListOf<Marker>()

    private val binding: FragmentPickLocationBinding by viewBinding()

    private var map: GoogleMap? = null

    private val defaultLocation = LatLng(55.7522200, 37.6155600)

    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    private var lastKnownLocation: Location? = null

    private var latLng: LatLng? = null

    private var showAlreadyPickedLocation = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        showAlreadyPickedLocation = requireArguments().getBoolean("alreadyPicked", false)

        fusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(requireContext())

        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as? SupportMapFragment
        mapFragment?.getMapAsync(this)

        requireView().findViewById<EditText>(R.id.etSearch).apply {
            setSelectAllOnFocus(true)
            setOnEditorActionListener { v, actionId, event ->
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    search()
                    hideKeyboard()
                    return@setOnEditorActionListener true
                }

                false
            }

            pick.setOnClickListener {
                latLng?.let {
                    setFragmentResult(
                        "pickLocation",
                        bundleOf("coordinates" to "${it.latitude}_${it.longitude}")
                    )
                    findNavController().navigateUp()
                }
            }
        }
    }

    private fun search() {
        val addresses = searchAddresses(
            locationName = requireView().findViewById<EditText>(R.id.etSearch).text.toString()
                .trim()
        )
        if (addresses.isNullOrEmpty()) {
            Snackbar.make(requireView(), "Ничего не найдено", Snackbar.LENGTH_LONG)
                .show()
            return
        }

        val address = addresses[0]
        val lat = address.latitude
        val lon = address.longitude

        removeMarkersByContainsTag("my_location")

        latLng = LatLng(lat, lon).also {
            addPointImage("${it.latitude}_${it.longitude}")
            moveCamera(it)
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap

        googleMap.setOnCameraMoveStartedListener {
            if (etSearch != null && etSearch.isFocused) {
                etSearch.clearFocus()
                etSearch.requestFocus()
                hideKeyboard()
                (requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager)?.hideSoftInputFromWindow(
                    etSearch.windowToken,
                    0
                )
            }
        }
        googleMap.setOnMapClickListener {
            if (etSearch != null && etSearch.isFocused) {
                etSearch.clearFocus()
                etSearch.requestFocus()
                hideKeyboard()
                (requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager)?.hideSoftInputFromWindow(
                    etSearch.windowToken,
                    0
                )
            }

            if (showAlreadyPickedLocation) return@setOnMapClickListener

            removeMarkersByContainsTag("my_location")
            addPointImage("${it.latitude}_${it.longitude}")
            moveCamera(it)
        }

        googleMap.uiSettings.isCompassEnabled = false

        if (showAlreadyPickedLocation) {
            val coordinates = requireArguments().getString("coordinates") ?: return
            val lat = coordinates.substringBefore("_").toDoubleOrNull() ?: 0.0
            val lon = coordinates.substringAfter("_").toDoubleOrNull() ?: 0.0

            addPointImage(coordinates)
            moveCamera(LatLng(lat, lon))

            requireView().findViewById<EditText>(R.id.etSearch).apply {
                isClickable = false
                isFocusable = false
            }
        } else {
            getDeviceLocation()
        }
    }

    private fun addPointImage(coordinates: String) {
        if (!showAlreadyPickedLocation) pick.isVisible = true

        val lat = coordinates.substringBefore("_").toDoubleOrNull() ?: 0.0
        val lon = coordinates.substringAfter("_").toDoubleOrNull() ?: 0.0

        val bitmap = generateIcon(R.drawable.ic_my_location, 120, 120) ?: return

        latLng = LatLng(lat, lon)

        searchAddresses(lat, lon)?.let {
            val address = it[0]

            val addressString = address.getAddressLine(0)

            requireView().findViewById<EditText>(R.id.etSearch).apply {
                setText(addressString)
                setSelection(addressString.length)
            }
        }

        putMarkerOnMap(LatLng(lat, lon), bitmap, "my_location")
    }

    private fun searchAddresses(
        lat: Double? = null,
        lon: Double? = null,
        locationName: String? = null
    ): List<Address>? {
        if (lat == null && lon == null && locationName == null) return null

        val geocoder = Geocoder(requireContext(), Locale.getDefault())

        val list =
            if (locationName == null && lat != null && lon != null)
                geocoder.getFromLocation(lat, lon, 100)
            else geocoder.getFromLocationName(locationName, 100)
        return if (list.isEmpty()) null else list
    }

    private fun putMarkerOnMap(point: LatLng, image: BitmapDescriptor, tag: String) {
        map?.addMarker(MarkerOptions().position(point).icon(image))?.also {
            it.tag = tag
            markers.add(it)
        }
    }

    private fun removeMarkersByContainsTag(str: String) {
        markers.filter { it.tag.toString().contains(str) }.forEach { it.remove() }
    }

    private fun moveCamera(point: LatLng, zoom: Float = 15f) {
        map?.animateCamera(CameraUpdateFactory.newLatLngZoom(point, zoom))
    }

    private fun getDeviceLocation() {
        askPermission(Manifest.permission.ACCESS_FINE_LOCATION) {
            try {
                val locationResult = fusedLocationProviderClient.lastLocation
                locationResult.addOnCompleteListener(requireActivity()) { task ->
                    locationReceived(task)
                }

            } catch (e: SecurityException) {
                Log.e("Exception: %s", e.message, e)
            }
        }.onDeclined { e ->
            lastKnownLocation = null
            if (e.hasDenied()) {
                AlertDialog.Builder(requireContext())
                    .setMessage(getString(R.string.permission_need))
                    .setPositiveButton("Ок") { _, _ -> e.askAgain() }
                    .show()
            }

            if (e.hasForeverDenied()) {
                e.goToSettings()
            }
            return@onDeclined
        }
    }

    private fun locationReceived(task: Task<Location>) {
        if (task.isSuccessful) {
            lastKnownLocation = task.result?.also { location ->
                lifecycleScope.launch {
                    addPointImage("${location.latitude}_${location.longitude}")
//                    generateIcon(R.drawable.ic_my_location, 120, 120)?.let {
//                        removeMarkersByContainsTag("my_location")
//                        putMarkerOnMap(
//                            LatLng(location.latitude, location.longitude),
//                            it, "my_location"
//                        )
//                    }
                    moveCamera(LatLng(location.latitude, location.longitude))
                }
            }
        } else {
            Log.d(ContentValues.TAG, "Current location is null. Using defaults.")
            Log.e(ContentValues.TAG, "Exception: %s", task.exception)
            moveCamera(defaultLocation)
        }
    }

    private fun generateIcon(
        @DrawableRes res: Int,
        width: Int = 120,
        height: Int = 120,
        count: Int? = null
    ) = ResourcesCompat.getDrawable(resources, res, null)
        ?.toBitmapWithBadge(width, height, count ?: 0)?.let {
            BitmapDescriptorFactory.fromBitmap(Bitmap.createScaledBitmap(it, width, height, true))
        }
}