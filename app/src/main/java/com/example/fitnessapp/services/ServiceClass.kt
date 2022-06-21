package com.example.fitnessapp.services

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
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.example.fitnessapp.ui.MainActivity
import com.example.fitnessapp.R
import com.example.fitnessapp.others.Constants.ACTION_PAUSE_SERVICE
import com.example.fitnessapp.others.Constants.ACTION_START_OR_RESUME_SERVICE
import com.example.fitnessapp.others.Constants.ACTION_STOP_SERVICE
import com.example.fitnessapp.others.Constants.FASTEST_LOCATION_INTERVAL
import com.example.fitnessapp.others.Constants.GO_TO_TRACKING_FRAGMENT
import com.example.fitnessapp.others.Constants.LOCATION_UPDATE_INTERVAL
import com.example.fitnessapp.others.Constants.NOTIFICATION_CHANNEL_ID
import com.example.fitnessapp.others.Constants.NOTIFICATION_CHANNEL_IDS
import com.example.fitnessapp.others.Constants.NOTIFICATION_CHANNEL_NAME
import com.example.fitnessapp.others.TrackingUtility
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY
import com.google.android.gms.location.LocationResult
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import timber.log.Timber
import javax.inject.Inject

typealias polyLine = MutableList<LatLng>
typealias polyLines = MutableList<polyLine>

@AndroidEntryPoint
class ServiceClass : LifecycleService() {
    lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    @Inject
    lateinit var baseNotificationBuilder: NotificationCompat.Builder
    lateinit var curNotificationBuilder: NotificationCompat.Builder
    private val timeInSeconds = MutableLiveData<Long>()
    var startedFirstTime = true
    var killedService = false
    override fun onCreate() {
        super.onCreate()
        curNotificationBuilder = baseNotificationBuilder
        addInitialValuesToLiveData()
        fusedLocationProviderClient = FusedLocationProviderClient(this)
        isTracked.observe(this, Observer {
            updateLocation(it)
            updateNotification(it)
        })
    }

    companion object {
        val timeInMillis = MutableLiveData<Long>()
        val isTracked = MutableLiveData<Boolean>()
        val pathDraw = MutableLiveData<polyLines>()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let {
            when (it.action) {
                ACTION_START_OR_RESUME_SERVICE -> {
                    if (startedFirstTime) {
                        startForegroundService()
                        startedFirstTime = false
                    } else {
                        calculateTime()
                    }
                }
                ACTION_PAUSE_SERVICE -> {
                    Timber.d("Paused service")
                    pauseServices()
                }
                ACTION_STOP_SERVICE -> {
                    Timber.d("Stopped service")
                    killService()
                }
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private var isTimerEnabled = false
    private var lapTime = 0L
    private var timeRun = 0L
    private var timeStarted = 0L
    private var lastSecondTimestamp = 0L
    private fun calculateTime() {
        addPolyLines()
        isTracked.postValue(true)
        timeStarted = System.currentTimeMillis()
        isTimerEnabled = true
        CoroutineScope(Dispatchers.Main).launch {
            while (isTracked.value!!) {
                //total time in millis and post it in liveData when updates happened
                lapTime = System.currentTimeMillis() - timeStarted
                //calculate time for stop watch
                timeInMillis.postValue(timeRun + lapTime)
                //compare between millis and seconds if millis greater than that mean one second pass and i can update time in seconds every Time by add one
                if (timeInMillis.value!! >= lastSecondTimestamp + 1000L) {

                    //line update notification time later after 1,2,3..etc seconds not calculating it actually it is just update after every second
                    timeInSeconds.postValue(timeInSeconds.value!! + 1)
                    lastSecondTimestamp += 1000
                }
                //to prevent observe each msec
                delay(50L)

            }
            timeRun += lapTime
        }

    }

    private fun addInitialValuesToLiveData() {
        isTracked.postValue(false)
        pathDraw.postValue(mutableListOf())
        timeInSeconds.postValue(0L)
        timeInMillis.postValue(0L)
    }

    private fun addPolyLines() = pathDraw.value?.apply {
        add(mutableListOf())
        pathDraw.postValue(this)
    } ?: pathDraw.postValue(mutableListOf(mutableListOf()))

    private fun updateLocation(isTracking: Boolean) {
        if (isTracking) {
            if (TrackingUtility.hasLocationPermission(this)) {
                val request = LocationRequest().apply {
                    interval = LOCATION_UPDATE_INTERVAL
                    fastestInterval = FASTEST_LOCATION_INTERVAL
                    priority = PRIORITY_HIGH_ACCURACY
                }
                fusedLocationProviderClient.requestLocationUpdates(
                    request,
                    localLocationCallBack(),
                    Looper.getMainLooper()
                )
            }
        } else {
            fusedLocationProviderClient.removeLocationUpdates(localLocationCallBack())
        }
    }

    private fun addLatLang(location: Location?) {
        location?.let {
            val currentPosition = LatLng(location.latitude, location.longitude)
            pathDraw.value?.apply {
                last().add(currentPosition)
                pathDraw.postValue(this)
            }
        }

    }

    private fun localLocationCallBack() = object : LocationCallback() {
        override fun onLocationResult(p0: LocationResult?) {
            if (isTracked.value!!)
                p0?.locations?.let { locations ->
                    for (i in locations) {
                        addLatLang(i)
                        Timber.d("NEW LOCATION: ${i.latitude}, ${i.longitude}")
                    }
                }
        }
    }

    private fun pauseServices() {
        isTracked.postValue(false)
        isTimerEnabled = false
    }

    private fun startForegroundService() {
        //calculateTime()
        calculateTime()
        isTracked.postValue(true)
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE)
                as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel(notificationManager)
        }
        startForeground(NOTIFICATION_CHANNEL_ID, baseNotificationBuilder.build())
        timeInSeconds.observe(this, Observer {
            if (!killedService){
                val notification = curNotificationBuilder
                    .setContentText(TrackingUtility.getFormattedStopWatchTime(it * 1000))
                notificationManager.notify(NOTIFICATION_CHANNEL_ID, notification.build())
            }

        })

    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(notificationManager: NotificationManager) {
        val channel = NotificationChannel(
            NOTIFICATION_CHANNEL_IDS,
            NOTIFICATION_CHANNEL_NAME,
            IMPORTANCE_LOW
        )
        notificationManager.createNotificationChannel(channel)
    }

    private fun updateNotification(isTracking: Boolean) {
        val notificationActionText = if (isTracking) "Pause" else "Resume"
        val pendingIntent = if (isTracking) {
            val pauseIntent = Intent(this, ServiceClass::class.java).apply {
                action = ACTION_PAUSE_SERVICE
            }
            PendingIntent.getService(this, 1, pauseIntent, FLAG_UPDATE_CURRENT)
        } else {
            val resumeIntent = Intent(this, ServiceClass::class.java).apply {
                action = ACTION_START_OR_RESUME_SERVICE
            }
            PendingIntent.getService(this, 2, resumeIntent, FLAG_UPDATE_CURRENT)
        }
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        curNotificationBuilder.javaClass.getDeclaredField("mActions").apply {
            isAccessible = true
            set(curNotificationBuilder, ArrayList<NotificationCompat.Action>())
        }
        if (!killedService){
            curNotificationBuilder = baseNotificationBuilder
                .addAction(R.drawable.ic_pause_black_24dp, notificationActionText, pendingIntent)
            notificationManager.notify(NOTIFICATION_CHANNEL_ID, curNotificationBuilder.build())
        }

    }
    private fun killService(){
        killedService = true
        startedFirstTime = true
        pauseServices()
        addInitialValuesToLiveData()
        stopForeground(true)
        stopSelf()
    }

}