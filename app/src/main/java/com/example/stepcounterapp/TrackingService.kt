package com.example.stepcounterapp

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.NotificationManager.IMPORTANCE_LOW
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.Build
import android.os.Looper
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.example.util.Constant.ACTION_PAUSE
import com.example.util.Constant.ACTION_RESUME
import com.example.util.Constant.ACTION_STOP
import com.example.util.Constant.ACTION_TRACKING
import com.example.util.Constant.Notification_Channel_Id
import com.example.util.Constant.Notification_ID
import com.example.util.Constant.Notification_Name
import com.example.util.TrackingUtility
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY
import com.google.android.gms.location.LocationResult
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

typealias polyline = MutableList<LatLng>
typealias mainPath = MutableList<polyline>

@AndroidEntryPoint
class TrackingService : LifecycleService() {

    var isFirstTime = true
    private val timeRunInSeconds = MutableLiveData<Long>()

    @Inject
    lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    @Inject
    lateinit var baseNotification: NotificationCompat.Builder
    lateinit var runningNotificationBuilder: NotificationCompat.Builder

    private var isTimerEnabled = false
    private var lapTime = 0L
    private var timeRun = 0L
    private var timeStarted = 0L
    private var lastSecondTimestamp = 0L

    companion object {
        val isTracking = MutableLiveData<Boolean>()
        val pathPoints = MutableLiveData<mainPath>()
        val timeRunInMillis = MutableLiveData<Long>()

    }

    override fun onCreate() {
        super.onCreate()
        InitValues()
        runningNotificationBuilder = baseNotification
        isTracking.observe(this, Observer {
            updateLocationTracking(it)
            updateNotification(it)
            Log.d("MY", "Observing..... $it")
        })

    }

    private fun InitValues() {
        isTracking.postValue(false)
        pathPoints.postValue(mutableListOf())
        timeRunInSeconds.postValue(0L)
        timeRunInMillis.postValue(0L)
    }


    private fun startTimer() {
        addEmptyPolyline()
        isTracking.postValue(true)
        timeStarted = System.currentTimeMillis()
        isTimerEnabled = true
        CoroutineScope(Dispatchers.Main).launch {
            while (isTracking.value!!) {
                // time difference between now and timeStarted
                lapTime = System.currentTimeMillis() - timeStarted
                // post the new lapTime
                timeRunInMillis.postValue(timeRun + lapTime)
                if (timeRunInMillis.value!! >= lastSecondTimestamp + 1000L) {
                    timeRunInSeconds.postValue(timeRunInSeconds.value!! + 1)
                    lastSecondTimestamp += 1000L
                }
                delay(3000)
            }
            timeRun += lapTime
        }
    }

    private fun pauseService() {
        isTracking.postValue(false)
        isTimerEnabled = false
    }

    private fun addPathPoint(location: Location?) {
        location?.let {
            val pos = LatLng(location.latitude, location.longitude)
            pathPoints.value?.apply {
                last().add(pos)
                pathPoints.postValue(this)
            }
        }
    }

    private fun addEmptyPolyline() = pathPoints.value?.apply {
        add(mutableListOf())
        pathPoints.postValue(this)
    } ?: pathPoints.postValue(mutableListOf(mutableListOf()))

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let {
            when (it.action) {
                ACTION_RESUME -> {
                    if (isFirstTime) {
                        startForegroundService()
                        isFirstTime = false
                    } else {

                    }
                }

                ACTION_STOP -> {
                    stopSelf()
                }
                ACTION_PAUSE -> {
                    pauseService()
                }
            }
        }

        return super.onStartCommand(intent, flags, startId)
    }

    private fun startForegroundService() {
        startTimer()
        isTracking.postValue(true)
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel(notificationManager)
        }
        startForeground(Notification_ID, baseNotification.build())

        timeRunInSeconds.observe(this, Observer {
            val notification = runningNotificationBuilder
                .setContentText(TrackingUtility.getFormattedStopWatchTime(it * 1000L))
            notificationManager.notify(Notification_ID, notification.build())
        })
    }

    private fun updateNotification(isTracking: Boolean) {
        val text = if (isTracking) "Pause" else "Resume"
        val pendingIntent = if (isTracking) {
            val pauseIntent = Intent(this, TrackingService::class.java).apply {
                action = ACTION_PAUSE
            }
            PendingIntent.getService(this, 1, pauseIntent, FLAG_UPDATE_CURRENT)
        } else {
            val resumeIntent = Intent(this, TrackingService::class.java).apply {
                action = ACTION_RESUME
            }
            PendingIntent.getService(this, 2, resumeIntent, FLAG_UPDATE_CURRENT)
        }

        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        runningNotificationBuilder.javaClass.getDeclaredField("mActions").apply {
            isAccessible = true
            set(runningNotificationBuilder, ArrayList<NotificationCompat.Action>())
        }

        runningNotificationBuilder = baseNotification
            .addAction(R.drawable.ic_baseline_settings_24, text, pendingIntent)

        notificationManager.notify(Notification_ID, runningNotificationBuilder.build())

    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(notificationManager: NotificationManager) {
        val channel = NotificationChannel(
            Notification_Channel_Id,
            Notification_Name,
            IMPORTANCE_LOW
        )
        notificationManager.createNotificationChannel(channel)
    }

    val locationCallback = object : LocationCallback() {
        override fun onLocationResult(p0: LocationResult?) {
            super.onLocationResult(p0)
            if (isTracking.value!!) {
                Log.d("MY", "Tracking Working....")
                p0?.locations?.let { locations ->
                    for (location in locations) {
                        addPathPoint(location)
                        Log.d("MYLocation", "${location.latitude} : ${location.longitude}")
                    }
                }
            }
        }
    }


    @SuppressLint("MissingPermission")
    private fun updateLocationTracking(isTracking: Boolean) {
        if (isTracking) {
            if (TrackingUtility.hasPermission(this)) {
                val request = LocationRequest().apply {
                    interval = 5000L
                    fastestInterval = 200L
                    priority = PRIORITY_HIGH_ACCURACY
                }
                fusedLocationProviderClient.requestLocationUpdates(
                    request,
                    locationCallback,
                    Looper.getMainLooper()
                )

            }
            Log.d("MY", "Update Working....")
        } else {
            Log.d("MY", "Remove")
            fusedLocationProviderClient.removeLocationUpdates(locationCallback)
        }
    }


}