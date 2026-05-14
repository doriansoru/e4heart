/*
 * e4heart - Heart rate monitoring for Wear OS
 * Copyright (C) 2026 Dorian Soru
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.e4heart

import android.app.*
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.*
import androidx.core.app.NotificationCompat
import androidx.wear.watchface.complications.datasource.ComplicationDataSourceUpdateRequester
import android.content.ComponentName
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.abs

class HeartRateService : Service(), SensorEventListener {
    private var sensorManager: SensorManager? = null
    private var heartRateSensor: Sensor? = null
    private var vibrator: Vibrator? = null
    
    private var threshold = 100f
    private var rhr = 70f
    private var aboveThresholdStartTime = 0L
    private var isAlerting = false
    private var lastAlertVibrationTime = 0L
    private var lastBpmDuringAlert = 0
    private var lastSensorEventTime = 0L
    private val handler = Handler(Looper.getMainLooper())
    private val watchdogRunnable = object : Runnable {
        override fun run() {
            if (lastSensorEventTime > 0 && (System.currentTimeMillis() - lastSensorEventTime) > 35000) {
                android.util.Log.w("e4heart", "Watchdog: sensore bloccato, reset in corso...")
                resetSensor()
            }
            handler.postDelayed(this, 15000)
        }
    }

    private fun resetSensor() {
        sensorManager?.unregisterListener(this)
        isSensorRegistered = false
        heartRateSensor?.let {
            val registered = sensorManager?.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
            isSensorRegistered = registered ?: false
            lastSensorEventTime = System.currentTimeMillis()
            android.util.Log.d("e4heart", "Watchdog: sensore riregistrato: $isSensorRegistered")
        }
    }

    companion object {
        const val CHANNEL_ID = "HeartRateChannel"
        const val NOTIFICATION_ID = 1
        
        private val _bpmFlow = MutableStateFlow(0)
        val bpmFlow = _bpmFlow.asStateFlow()

        private val _alertFlow = MutableStateFlow(false)
        val alertFlow = _alertFlow.asStateFlow()
        
        // Esposti come var/val per retrocompatibilità limitata (MyWatchFaceService usa currentBpm)
        var currentBpm: Int
            get() = _bpmFlow.value
            private set(value) { _bpmFlow.value = value }
            
        var isAlertingGlobal: Boolean
            get() = _alertFlow.value
            private set(value) { _alertFlow.value = value }
    }

    override fun onCreate() {
        super.onCreate()
        android.util.Log.d("e4heart", "Service onCreate")
        createNotificationChannel()
        setupHardware()
        loadSettings()
        
        handler.postDelayed(watchdogRunnable, 15000)
        
        // Chiama startForeground il prima possibile
        val notification = createNotification()
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                startForeground(NOTIFICATION_ID, notification, android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_HEALTH)
            } else {
                startForeground(NOTIFICATION_ID, notification)
            }
            android.util.Log.d("e4heart", "startForeground chiamato con successo")
        } catch (e: Exception) {
            android.util.Log.e("e4heart", "Errore in startForeground", e)
        }
    }

    private fun setupHardware() {
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        heartRateSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_HEART_RATE)
        
        if (heartRateSensor == null) {
            android.util.Log.e("e4heart", "ERRORE: Sensore TYPE_HEART_RATE non trovato!")
        } else {
            android.util.Log.d("e4heart", "Sensore TYPE_HEART_RATE trovato: ${heartRateSensor?.name}")
        }

        vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
    }

    private fun loadSettings() {
        val prefs = getSharedPreferences("e4heart_prefs", Context.MODE_PRIVATE)
        rhr = prefs.getFloat("rhr", 70f)
        threshold = rhr + 15f
        
        // Se la soglia era salvata esplicitamente in precedenza, potremmo volerla onorare o migrare.
        // Ma seguiamo la nuova logica: Threshold = RHR + 15.
        // Se vogliamo permettere la modifica manuale della soglia, dovremmo riconsiderare.
        // Per ora usiamo RHR come base.
        
        aboveThresholdStartTime = 0L
        isAlerting = false
    }

    private var isSensorRegistered = false

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        android.util.Log.d("e4heart", "Service onStartCommand")
        
        // Obbligatorio chiamare startForeground immediatamente
        val notification = createNotification()
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                startForeground(NOTIFICATION_ID, notification, android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_HEALTH)
            } else {
                startForeground(NOTIFICATION_ID, notification)
            }
            android.util.Log.d("e4heart", "startForeground chiamato con successo")
        } catch (e: Exception) {
            android.util.Log.e("e4heart", "Errore in startForeground", e)
        }
        
        loadSettings()
        
        if (!isSensorRegistered) {
            heartRateSensor?.let {
                val registered = sensorManager?.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
                isSensorRegistered = registered ?: false
                android.util.Log.d("e4heart", "Registrazione listener sensore: $isSensorRegistered")
            }
        } else {
            android.util.Log.d("e4heart", "Sensore già registrato, salto")
        }
        
        return START_STICKY
    }

    private fun createNotification(): Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        val contentText = if (isAlerting) getString(R.string.service_alert_msg) else getString(R.string.service_monitoring_msg)

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.service_active_title))
            .setContentText(contentText)
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(CHANNEL_ID, getString(R.string.notification_channel_name), NotificationManager.IMPORTANCE_LOW)
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private var lastComplicationUpdateTime = 0L
    private var lastReportedBpm = 0

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_HEART_RATE) {
            lastSensorEventTime = System.currentTimeMillis()
            val bpm = event.values[0].toInt()
            
            // Su TicWatch e altri, valori <= 0 indicano che il sensore sta ancora cercando il battito
            if (bpm <= 0) {
                if (currentBpm != 0) {
                    currentBpm = 0
                    updateComplication()
                    updateTile()
                }
                return
            }
            
            android.util.Log.d("e4heart", "Sensore BPM: $bpm")
            currentBpm = bpm
                
            getSharedPreferences("e4heart_prefs", Context.MODE_PRIVATE)
                .edit().putInt("last_bpm", bpm).apply()

            checkThreshold(bpm)
            
            val currentTime = System.currentTimeMillis()
            val timeSinceLastUpdate = currentTime - lastComplicationUpdateTime
            val bpmDelta = abs(bpm - lastReportedBpm)
            
            // Aggiorna Complication/Tile solo se il battito cambia di >= 3 BPM o se è passato almeno 1 minuto
            if (bpmDelta >= 3 || timeSinceLastUpdate > 60000) {
                lastComplicationUpdateTime = currentTime
                lastReportedBpm = bpm
                updateComplication()
                updateTile()
            }
        }
    }

    private fun checkThreshold(bpm: Int) {
        val currentTime = System.currentTimeMillis()
        
        if (bpm > threshold) {
            if (aboveThresholdStartTime == 0L) {
                aboveThresholdStartTime = currentTime
                android.util.Log.d("e4heart", "Inizio superamento soglia a $bpm BPM")
            }
            
            val timeAboveThreshold = currentTime - aboveThresholdStartTime
            
            if (timeAboveThreshold >= 120000) { // 2 minuti (120.000 ms)
                if (!isAlerting) {
                    isAlerting = true
                    isAlertingGlobal = true
                    vibrate(false) // Vibrazione discreta iniziale
                    lastAlertVibrationTime = currentTime
                    lastBpmDuringAlert = bpm
                    updateNotification()
                    android.util.Log.d("e4heart", "ALLERTA ATTIVATA dopo 2 minuti")
                } else {
                    // Già in allarme. Più insistente se passano altri 2 min o se il battito sale ancora significativamente (+5)
                    if (currentTime - lastAlertVibrationTime > 120000 || bpm > lastBpmDuringAlert + 5) {
                        vibrate(true) // Vibrazione insistente
                        lastAlertVibrationTime = currentTime
                        lastBpmDuringAlert = bpm
                        android.util.Log.d("e4heart", "Promemoria allerta insistente")
                    }
                }
            }
        } else {
            // Sotto la soglia
            if (!isAlerting) {
                // Se non eravamo in allarme, è un picco momentaneo (spike) < 2 minuti. Resettiamo.
                if (aboveThresholdStartTime != 0L) {
                    android.util.Log.d("e4heart", "Picco ignorato, battito rientrato sotto soglia in ${(currentTime - aboveThresholdStartTime)/1000}s")
                    aboveThresholdStartTime = 0L
                }
            } else {
                // Siamo in allarme, aspettiamo il rientro entro RHR + 10 (Recupero)
                if (bpm <= rhr + 10) {
                    isAlerting = false
                    isAlertingGlobal = false
                    aboveThresholdStartTime = 0L
                    updateNotification()
                    android.util.Log.d("e4heart", "RECUPERO COMPLETATO: Battito $bpm <= ${rhr + 10}")
                }
            }
        }
    }

    private fun updateNotification() {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, createNotification())
    }

    private fun vibrate(isInsistent: Boolean) {
        if (isInsistent) {
            // Serie di vibrazioni (insistente)
            val pattern = longArrayOf(0, 500, 200, 500, 200, 500)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createWaveform(pattern, -1))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(pattern, -1)
            }
        } else {
            // Singola vibrazione discreta (800ms)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createOneShot(800L, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(800L)
            }
        }
    }

    private fun updateComplication() {
        val requester = ComplicationDataSourceUpdateRequester.create(
            this,
            ComponentName(this, HeartComplicationService::class.java)
        )
        requester.requestUpdateAll()
    }

    private fun updateTile() {
        androidx.wear.tiles.TileService.getUpdater(this)
            .requestUpdate(HeartRateTileService::class.java)
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    override fun onDestroy() {
        super.onDestroy()
        sensorManager?.unregisterListener(this)
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
