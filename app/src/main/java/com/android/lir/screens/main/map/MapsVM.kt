package com.android.lir.screens.main.map

import android.content.ContentValues
import android.content.Context
import android.content.res.Resources
import android.graphics.*
import android.location.Location
import android.util.Log
import androidx.annotation.DrawableRes
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.viewModelScope
import com.android.lir.R
import com.android.lir.base.vm.BaseVM
import com.android.lir.base.vm.Event
import com.android.lir.dataclases.Chat
import com.android.lir.dataclases.ThematicChatInfo
import com.android.lir.network.AuthRepo
import com.android.lir.utils.AppExtensions.toChat
import com.android.lir.utils.AppExtensions.toThematicChat
import com.android.lir.utils.toBitmapWithBadge
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.*
import com.google.android.gms.tasks.Task
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.android.synthetic.main.fragment_maps.*
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject


@HiltViewModel
class MapsVM @Inject constructor(
    @ApplicationContext context: Context,
    private val resources: Resources,
    private val repo: AuthRepo,
) : BaseVM() {

    private val markers = mutableListOf<Marker>()

    var map: GoogleMap? = null
        set(value) {
            field = value
            field?.setOnMarkerClickListener(
                GoogleMap.OnMarkerClickListener {
                    val tag = it.tag?.toString() ?: return@OnMarkerClickListener false

                    if (tag.contains("thematic")) {
                        tag.toThematicChat()?.let {
                            viewModelScope.launch { tasksEventChannel.send(ShowThematicDialog(it)) }
                            return@OnMarkerClickListener true
                        }
                    } else {
                        tag.toChat()?.let {
                            viewModelScope.launch { tasksEventChannel.send(ShowEasyDialog(it)) }
                            return@OnMarkerClickListener true
                        }
                    }

                    return@OnMarkerClickListener false
                }
            )
        }

    private val defaultLocation = LatLng(55.7522200, 37.6155600)

    val fusedLocationProviderClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)
    var lastKnownLocation: Location? = null

    fun getAllChats(isInitial: Boolean = true) = viewModelScope.launch {
        makeJob(
            { repo.getAllChats() },
            onAnswer = {
                if (!isInitial) removeMarkersByContainsTag("chat")
                it.filterNotNull().filter { c -> c.coordinates?.contains("_") == true }
                    .forEach { chat ->
                        val lat = chat.coordinates?.substringBefore("_")?.toDoubleOrNull() ?: 0.0
                        val lng = chat.coordinates?.substringAfter("_")?.toDoubleOrNull() ?: 0.0
                        val icon = generateIcon(R.drawable.ic_chat_easy, count = chat.count)
                            ?: return@forEach
                        val tag = Gson().toJson(chat)
                        putMarkerOnMap(LatLng(lat, lng), icon, tag)
                    }
            }
        )
    }

    fun getAllThematicChats(isInitial: Boolean = true) = viewModelScope.launch {
        makeJob({ repo.getAllThematicChats() },
            onAnswer = {
                if (!isInitial) removeMarkersByContainsTag("thematic_chat")
                it.chats.filter { c -> c.coordinates?.contains("_") == true }
                    .forEach { thematicChat ->
                        val lat =
                            thematicChat.coordinates?.substringBefore("_")?.toDoubleOrNull() ?: 0.0

                        val lng =
                            thematicChat.coordinates?.substringAfter("_")?.toDoubleOrNull() ?: 0.0

                        val icon = generateIcon(R.drawable.ic_chat_themed) ?: return@forEach
                        var tag = Gson().toJson(thematicChat)
                        tag = tag.substring(0, tag.length - 1) + ", \"type\":\"thematic\"}"

                        putMarkerOnMap(LatLng(lat, lng), icon, tag)
                    }
            })
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

    fun locationReceived(task: Task<Location>) {
        if (task.isSuccessful) {
            lastKnownLocation = task.result?.also { location ->
                viewModelScope.launch {
                    generateIcon(R.drawable.ic_my_location, 120, 120)?.let {
                        removeMarkersByContainsTag("my_location")
                        putMarkerOnMap(
                            LatLng(location.latitude, location.longitude),
                            it, "my_location"
                        )
                    }
                    moveCamera(LatLng(location.latitude, location.longitude))
                }
            }
        } else {
            Log.d(ContentValues.TAG, "Current location is null. Using defaults.")
            Log.e(ContentValues.TAG, "Exception: %s", task.exception)
            moveCamera(defaultLocation)
        }
    }

    fun removeMarkersByContainsTag(str: String) {
        markers.filter { it.tag.toString().contains(str) }.forEach { it.remove() }
    }

    private fun putMarkerOnMap(point: LatLng, image: BitmapDescriptor, tag: String) {
        map?.addMarker(MarkerOptions().position(point).icon(image))?.also {
            it.tag = tag
            markers.add(it)
        }
    }

    fun moveCamera(point: LatLng, zoom: Float = 15f) {
        map?.animateCamera(CameraUpdateFactory.newLatLngZoom(point, zoom))
    }

    fun addChatImage(coordinates: String) {
        val lat = coordinates.substringBefore("_").toDoubleOrNull() ?: 0.0
        val lng = coordinates.substringAfter("_").toDoubleOrNull() ?: 0.0
        val icon = generateIcon(R.drawable.ic_chat_easy) ?: return

        putMarkerOnMap(LatLng(lat, lng), icon, "chat")
    }

    fun addThematicChatImage(coordinates: String) {
        val lat = coordinates.substringBefore("_").toDoubleOrNull() ?: 0.0
        val lng = coordinates.substringAfter("_").toDoubleOrNull() ?: 0.0
        val icon = generateIcon(R.drawable.ic_chat_themed) ?: return

        putMarkerOnMap(LatLng(lat, lng), icon, "chat_thematic")
    }

    fun addPointImage(coordinates: String, anotherTag: String? = null) {
        val lat = coordinates.substringBefore("_").toDoubleOrNull() ?: 0.0
        val lng = coordinates.substringAfter("_").toDoubleOrNull() ?: 0.0
//        val icon = generateIcon(R.drawable.ic_baseline_location_on_24) ?: return

        val markerBitmap =
            ResourcesCompat.getDrawable(resources, R.drawable.ic_baseline_location_on_24, null)
                ?.toBitmap() ?: return
//        val resultBitmap = Bitmap.createBitmap(markerBitmap, 0, 0, markerBitmap.width - 1, markerBitmap.height - 1)
//        val filter = PorterDuffColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN)

        val markerPaint = Paint()
//        markerPaint.colorFilter = filter

        val resultBitmap = addGradient(markerBitmap)

        val canvas = Canvas(resultBitmap)
        canvas.drawBitmap(resultBitmap, 0f, 0f, markerPaint)

        putMarkerOnMap(
            LatLng(lat, lng),
            BitmapDescriptorFactory.fromBitmap(resultBitmap),
            anotherTag ?: "simple_point"
        )
    }

    fun addGradient(originalBitmap: Bitmap): Bitmap {
        val width = originalBitmap.width
        val height = originalBitmap.height
        val updatedBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(updatedBitmap)
        canvas.drawBitmap(originalBitmap, 0F, 0F, null)

        val paint = Paint()
        val shader =
            LinearGradient(
                0F,
                0F,
                0F,
                height.toFloat(),
                0xffc86dd7.toInt(),
                0xff3023ae.toInt(),
                Shader.TileMode.CLAMP
            )
        paint.shader = shader
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
        canvas.drawRect(0F, 0F, width.toFloat(), height.toFloat(), paint)

        return updatedBitmap
    }
}

data class ShowEasyDialog(val chat: Chat) : Event()
data class ShowThematicDialog(val thematicChat: ThematicChatInfo) : Event()
