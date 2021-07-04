package com.android.lir.screens.main.map

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.android.lir.R
import com.android.lir.base.vm.BaseVMFragment
import com.android.lir.base.vm.Event
import com.android.lir.dataclases.Chat
import com.android.lir.dataclases.ThematicChatInfo
import com.android.lir.screens.main.map.createchat.ChatType
import com.github.florent37.runtimepermission.kotlin.askPermission
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_maps.*
import kotlinx.android.synthetic.main.view_search.*
import java.util.*

@AndroidEntryPoint
class MapsFragment : BaseVMFragment<MapsVM>(R.layout.fragment_maps), OnMapReadyCallback {

    override val viewModel: MapsVM by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.viewTreeObserver.addOnGlobalLayoutListener {
            val r = Rect()

            view.getWindowVisibleDisplayFrame(r)

            val screenHeight: Int = view.rootView.height
            val keypadHeight: Int = screenHeight - r.bottom

            if (keypadHeight <= screenHeight * 0.20) etSearch?.clearFocus()
        }

        if (viewModel.map == null) {
            val mapFragment = childFragmentManager.findFragmentById(R.id.map) as? SupportMapFragment
            mapFragment?.getMapAsync(this)
        }

        lifecycleScope.launchWhenStarted {
            etSearch.text = null
        }

        ivZoomPlus.setOnClickListener { viewModel.map?.animateCamera(CameraUpdateFactory.zoomIn()) }
        ivZoomMinus.setOnClickListener { viewModel.map?.animateCamera(CameraUpdateFactory.zoomOut()) }

        setFragmentResultListener("create_chat") { _, bundle ->
            when (val type = bundle.getParcelable<ChatType>("chat_type")) {
                ChatType.THEMED -> showThemedChat(
                    isCreate = true,
                    type = type,
                    coordinates = bundle.getString("coordinates") ?: "",
                )
                ChatType.CHANNEL, ChatType.EASY -> showEasyChat(
                    isCreate = true,
                    type = type,
                    coordinates = bundle.getString("coordinates") ?: ""
                )
                ChatType.COMMERCIAL -> showThemedChat(
                    isCreate = true,
                    type = type,
                    coordinates = bundle.getString("coordinates") ?: "",
                )
            }
        }

        setFragmentResultListener("update") { _, _ ->
            viewModel.getAllChats(false)
            viewModel.getAllThematicChats(false)
        }

        getDeviceLocation()

        ivGps.setOnClickListener { getDeviceLocation() }
    }

    private fun getDeviceLocation() {
        askPermission(Manifest.permission.ACCESS_FINE_LOCATION) {
            try {
                val locationResult = viewModel.fusedLocationProviderClient.lastLocation
                activity?.let { activity ->
                    locationResult.addOnCompleteListener(activity) { task ->
                        viewModel.locationReceived(task)
                    }
                }
            } catch (e: SecurityException) {
                Log.e("Exception: %s", e.message, e)
            }
        }.onDeclined { e ->
            viewModel.lastKnownLocation = null
            if (e.hasDenied()) {
                AlertDialog.Builder(activity)
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

    override fun onEvent(event: Event) {
        super.onEvent(event)
        when (event) {
            is ShowEasyDialog -> {
                showEasyChat(event.chat)
            }
            is ShowThematicDialog -> {
                showThemedChat(event.thematicChat)
            }
        }
    }

    override fun onMapReady(googleMap: GoogleMap?) {
        viewModel.map = googleMap ?: return
        googleMap.setOnCameraMoveStartedListener {
            if (etSearch != null && etSearch.isFocused) {
                etSearch.clearFocus()
                etSearch.requestFocus()
                hideKeyboard()
                (context?.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager)?.hideSoftInputFromWindow(
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
                (context?.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager)?.hideSoftInputFromWindow(
                    etSearch.windowToken,
                    0
                )
            }
        }

        viewModel.getAllChats()
        viewModel.getAllThematicChats()

        googleMap.uiSettings.isCompassEnabled = false
        googleMap.setOnMapLongClickListener {
            createSelectChatTypeDialog(it)
        }
    }

    private fun createSelectChatTypeDialog(point: LatLng) {
        findNavController().navigate(
            R.id.toCreateChatDialog,
            bundleOf(
                "coordinates" to "${point.latitude}_${point.longitude}"
            )
        )
    }


    private fun showEasyChat(
        chat: Chat? = null,
        isCreate: Boolean = false,
        type: ChatType? = null,
        coordinates: String = ""
    ) {
        viewModel.addChatImage(coordinates)
        findNavController().navigate(
            R.id.toEasyChatDialog,
            bundleOf(
                "chat" to chat,
                "isCreate" to isCreate,
                "type" to type,
                "coordinates" to coordinates
            )
        )
    }

    private fun showThemedChat(
        info: ThematicChatInfo? = null,
        isCreate: Boolean = false,
        type: ChatType? = null,
        coordinates: String = ""
    ) {
        viewModel.addThematicChatImage(coordinates)

        if (isCreate) {
            findNavController().navigate(
                R.id.toThematicChatCreateDialog,
                bundleOf(
                    "coordinates" to coordinates,
                    "isCreate" to isCreate,
                    "type" to type,
                    "info" to info
                )
            )
            return
        }

        findNavController().navigate(
            R.id.toThematicChatCommentsDialog,
            bundleOf(
                "coordinates" to coordinates,
                "type" to type,
                "info" to info
            )
        )
    }

    private fun showCommercialChat(chat: Chat? = null, isCreate: Boolean = false) {
        showThemedChat()
//        Toast.makeText(
//            context,
//            "Комерческий чат ${if (isCreate) "будет создан" else "открывается"}, id=${chat?.id ?: "Пока не известно"}",
//            Toast.LENGTH_SHORT
//        ).show()
    }


}
