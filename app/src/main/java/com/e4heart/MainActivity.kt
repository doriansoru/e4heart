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

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.wear.compose.material.*

class MainActivity : ComponentActivity(), SensorEventListener {
    private var rhrState = mutableFloatStateOf(70f)

    private val PREFS_NAME = "e4heart_prefs"
    private val KEY_RHR = "rhr"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        loadSettings()
        
        // Se i permessi sono già concessi, avvia il servizio immediatamente
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.BODY_SENSORS) == PackageManager.PERMISSION_GRANTED) {
            startHeartRateService()
        }
        
        checkPermissions()

        setContent {
            // Usiamo il Flow esposto dal servizio
            val bpmValue by HeartRateService.bpmFlow.collectAsState()
            val isAlerting by HeartRateService.alertFlow.collectAsState()
            val isPaused by HeartRateService.pausedFlow.collectAsState()

            WearApp(
                bpm = bpmValue,
                rhr = rhrState.floatValue,
                isAlerting = isAlerting,
                isPaused = isPaused,
                onRhrChange = { newValue ->
                    rhrState.floatValue = newValue
                    saveSettings(newValue)
                    startHeartRateService() // Riavvia per aggiornare parametri
                },
                onTogglePause = {
                    val action = if (isPaused) HeartRateService.ACTION_RESUME else HeartRateService.ACTION_PAUSE
                    val intent = Intent(this, HeartRateService::class.java).apply { this.action = action }
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        startForegroundService(intent)
                    } else {
                        startService(intent)
                    }
                }
            )
        }
    }

    private fun loadSettings() {
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        rhrState.floatValue = prefs.getFloat(KEY_RHR, 70f)
    }

    private fun saveSettings(value: Float) {
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putFloat(KEY_RHR, value).commit()
    }

    private fun checkPermissions() {
        val permissions = mutableListOf(Manifest.permission.BODY_SENSORS)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }
        
        val missingPermissions = permissions.filter {
            val isGranted = ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
            android.util.Log.d("e4heart", "Permesso $it concesso: $isGranted")
            !isGranted
        }

        if (missingPermissions.isNotEmpty()) {
            android.util.Log.d("e4heart", "Richiesta permessi: $missingPermissions")
            ActivityCompat.requestPermissions(this, missingPermissions.toTypedArray(), 1)
        } else {
            android.util.Log.d("e4heart", "Tutti i permessi già concessi, avvio servizio")
            startHeartRateService()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1) {
            val allGranted = grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }
            android.util.Log.d("e4heart", "Risultato permessi: allGranted=$allGranted")
            if (allGranted) {
                startHeartRateService()
            }
        }
    }

    private fun startHeartRateService() {
        val intent = Intent(this, HeartRateService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {}
    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    override fun onResume() {
        super.onResume()
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.BODY_SENSORS) == PackageManager.PERMISSION_GRANTED) {
            startHeartRateService()
        }
    }

    override fun onPause() {
        super.onPause()
    }
}

@Composable
fun WearApp(
    bpm: Int,
    rhr: Float,
    isAlerting: Boolean,
    isPaused: Boolean,
    onRhrChange: (Float) -> Unit,
    onTogglePause: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val threshold = rhr + 15
    val recoveryLimit = rhr + 10
    var showHelp by remember { mutableStateOf(false) }

    MaterialTheme {
        if (showHelp) {
            ScalingLazyColumn(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                item {
                    Text(
                        androidx.compose.ui.res.stringResource(R.string.how_it_works_title),
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFF9500),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
                item {
                    Text(
                        androidx.compose.ui.res.stringResource(
                            R.string.how_it_works_content,
                            threshold.toInt(),
                            recoveryLimit.toInt()
                        ),
                        fontSize = 12.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
                item {
                    Button(
                        onClick = { showHelp = false },
                        modifier = Modifier.padding(top = 12.dp)
                    ) {
                        Text(androidx.compose.ui.res.stringResource(R.string.ok_button))
                    }
                }
            }
        } else {
            Scaffold(
                timeText = { TimeText() },
                modifier = Modifier.fillMaxSize()
            ) {
                ScalingLazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Sezione Battito Corrente
                    item {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = if (isPaused) "||" else (if (bpm > 0) bpm.toString() else "--"),
                                fontSize = 44.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isAlerting) Color.Red else (if (isPaused) Color.Gray else Color.White)
                            )
                            Text(
                                if (isPaused) androidx.compose.ui.res.stringResource(R.string.action_pause).uppercase() 
                                else androidx.compose.ui.res.stringResource(R.string.current_bpm_label),
                                fontSize = 10.sp,
                                color = Color.Gray
                            )
                        }
                    }

                    item { Spacer(modifier = Modifier.height(4.dp)) }

                    // Pulsante Pausa/Riprendi
                    item {
                        Button(
                            onClick = onTogglePause,
                            modifier = Modifier.height(36.dp).fillMaxWidth(0.8f),
                            colors = ButtonDefaults.buttonColors(
                                backgroundColor = if (isPaused) Color(0xFF4CAF50) else Color(0xFFF44336)
                            )
                        ) {
                            Text(
                                if (isPaused) androidx.compose.ui.res.stringResource(R.string.action_resume)
                                else androidx.compose.ui.res.stringResource(R.string.action_pause)
                            )
                        }
                    }

                    item { Spacer(modifier = Modifier.height(8.dp)) }

                    // Sezione Impostazione RHR
                    item {
                        Card(
                            onClick = {},
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    androidx.compose.ui.res.stringResource(R.string.resting_hr_label, rhr.toInt()),
                                    fontWeight = FontWeight.Bold
                                )
                                InlineSlider(
                                    value = rhr,
                                    onValueChange = onRhrChange,
                                    valueRange = 40f..120f,
                                    steps = 79,
                                    increaseIcon = { Icon(androidx.wear.compose.material.InlineSliderDefaults.Increase, "+") },
                                    decreaseIcon = { Icon(androidx.wear.compose.material.InlineSliderDefaults.Decrease, "-") }
                                )
                            }
                        }
                    }

                    // Sezione Soglie Calcolate
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    androidx.compose.ui.res.stringResource(R.string.rec_label),
                                    fontSize = 10.sp,
                                    color = Color.Green
                                )
                                Text("< ${recoveryLimit.toInt()}", fontSize = 14.sp)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    androidx.compose.ui.res.stringResource(R.string.alert_label),
                                    fontSize = 10.sp,
                                    color = Color(0xFFFF9500)
                                )
                                Text("> ${threshold.toInt()}", fontSize = 14.sp)
                            }
                        }
                    }

                    // Tasto Info
                    item {
                        CompactButton(
                            onClick = { showHelp = true },
                            colors = ButtonDefaults.secondaryButtonColors()
                        ) {
                            Text("?")
                        }
                    }
                }
            }
        }
    }
}
