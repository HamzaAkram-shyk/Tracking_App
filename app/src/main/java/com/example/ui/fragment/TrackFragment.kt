package com.example.ui.fragment

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.example.localDb.Run
import com.example.stepcounterapp.R
import com.example.stepcounterapp.TrackingService
import com.example.stepcounterapp.polyline
import com.example.util.Constant.ACTION_PAUSE
import com.example.util.Constant.ACTION_RESUME
import com.example.util.Constant.ACTION_STOP
import com.example.util.Constant.MAP_ZOOM
import com.example.util.Constant.POLYLINE_COLOR
import com.example.util.Constant.POLYLINE_WIDTH
import com.example.util.TrackingUtility
import com.example.viewmodels.RunViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.*
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_tracking.*


@AndroidEntryPoint
class TrackFragment : Fragment(R.layout.fragment_tracking) {
    private val viewModel: RunViewModel by viewModels()
    private var map: GoogleMap? = null
    private var isTracking = false
    private var pathPoints = mutableListOf<polyline>()
    private lateinit var AllMarkers: ArrayList<Marker>
    private var curTimeInMillis = 0L


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mapView?.onCreate(savedInstanceState)
        mapView.getMapAsync {
            map = it
            addAllPolyLine()
        }
        btnToggleRun.setOnClickListener {
            if (TrackingUtility.isLocationEnabled(requireContext())) {
                toggleRun()
            } else {
                Toast.makeText(
                    requireContext(),
                    "Please Enable Your GPS For Using this App",
                    Toast.LENGTH_SHORT
                )
                    .show()
            }

        }
        subscribeToObserve()
        AllMarkers = ArrayList<Marker>()
        btnFinishRun.setOnClickListener {
            zoomWholeTrack()
            sendRunIntoDb()
        }


    }


    private fun subscribeToObserve() {
        TrackingService.isTracking.observe(viewLifecycleOwner, Observer {
            updateTracking(it)
        })
        TrackingService.pathPoints.observe(viewLifecycleOwner, Observer {
            pathPoints = it
            addLatestPolyline()
            moveCamera()
            moveRider()

        })

        TrackingService.timeRunInMillis.observe(viewLifecycleOwner, Observer {
            curTimeInMillis = it
            val formattedTime = TrackingUtility.getFormattedStopWatchTime(curTimeInMillis, true)
            tvTimer.text = formattedTime
        })
    }


    private fun toggleRun() {
        if (isTracking) {
            startTracking(ACTION_PAUSE)
        } else {
            startTracking(ACTION_RESUME)
        }
    }

    private fun updateTracking(isTracking: Boolean) {
        this.isTracking = isTracking
        if (!isTracking) {
            btnToggleRun.text = "Start"
            btnFinishRun.visibility = View.VISIBLE
        } else {
            btnToggleRun.text = "Stop"
            btnFinishRun.visibility = View.GONE
        }
    }

    private fun moveCamera() {
        if (pathPoints.isNotEmpty() && pathPoints.last().isNotEmpty()) {
            map?.animateCamera(
                CameraUpdateFactory.newLatLngZoom(
                    pathPoints.last().last(),
                    MAP_ZOOM
                )
            )

        }
    }


    private fun moveRider() {
        if (pathPoints.isNotEmpty() && pathPoints.last().isNotEmpty()) {
            removeAllMarkers()
            val marker = map?.addMarker(
                MarkerOptions()
                    .position(pathPoints.last().last())
                    .title("Running")
                    .snippet("Snippet")
                    .icon(BitmapDescriptorFactory.fromBitmap(convertIntoSmallIcon()))
            )
            AllMarkers.add(marker!!)

        }

    }

    private fun removeAllMarkers() {
        for (marker in AllMarkers) {
            marker.remove()
        }
        AllMarkers.clear()
    }

    private fun zoomWholeTrack() {
        val bounds = LatLngBounds.Builder()
        for (polyline in pathPoints) {
            for (pos in polyline) {
                bounds.include(pos)
            }
        }
        map?.moveCamera(
            CameraUpdateFactory.newLatLngBounds(
                bounds.build(),
                mapView.width,
                mapView.height,
                (mapView.height * 0.05f).toInt()
            )
        )
    }

    private fun sendRunIntoDb() {
        map?.snapshot { screenShot ->
            val run = Run(screenShot, 8f, 30, 90L, 98L, 30)
            viewModel.insertRun(run)
            Snackbar.make(
                requireActivity().findViewById(R.id.rootView),
                "Run Send Successfully ...",
                Snackbar.LENGTH_LONG
            ).show()
            startTracking(ACTION_STOP)
        }

        findNavController().navigate(R.id.action_trackFragment_to_runFragment)

    }


    private fun convertIntoSmallIcon(): Bitmap {
        val height = 100
        val width = 100
        val bitmap = BitmapFactory.decodeResource(context?.resources, R.drawable.rider)
        return Bitmap.createScaledBitmap(bitmap, width, height, false)
    }

    private fun addLatestPolyline() {
        if (pathPoints.isNotEmpty() && pathPoints.last().size > 1) {
            val preLastLatLng = pathPoints.last()[pathPoints.last().size - 2]
            val lastLatLng = pathPoints.last().last()
            val polyLineOptions = PolylineOptions()
                .color(POLYLINE_COLOR)
                .width(POLYLINE_WIDTH)
                .add(preLastLatLng)
                .add(lastLatLng)
            map?.addPolyline(polyLineOptions)
        }
    }

    private fun addAllPolyLine() {
        for (polyline in pathPoints) {
            val polylineOptions = PolylineOptions()
                .color(POLYLINE_COLOR)
                .width(POLYLINE_WIDTH)
                .addAll(polyline)
            map?.addPolyline(polylineOptions)
        }
    }

    private fun startTracking(action: String) {
        Intent(requireContext(), TrackingService::class.java).also {
            it.action = action
            requireContext().startService(it)
        }
    }

    override fun onResume() {
        super.onResume()
        mapView?.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView?.onPause()
    }

    override fun onStart() {
        super.onStart()
        mapView?.onStart()

    }

    override fun onStop() {
        super.onStop()
        mapView?.onStop()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView?.onLowMemory()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView?.onDestroy()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView?.onSaveInstanceState(outState)
    }


}