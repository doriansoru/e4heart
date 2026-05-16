package com.e4heart

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.*
import android.os.BatteryManager
import android.view.SurfaceHolder
import androidx.wear.watchface.*
import androidx.wear.watchface.style.CurrentUserStyleRepository
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import android.util.Log

class MyWatchFaceService : WatchFaceService() {
    override suspend fun createWatchFace(
        surfaceHolder: SurfaceHolder,
        watchState: WatchState,
        complicationSlotsManager: ComplicationSlotsManager,
        currentUserStyleRepository: CurrentUserStyleRepository
    ): WatchFace {
        Log.d("e4heart", "createWatchFace: inizio")
        try {
            val renderer = MyCanvasRenderer(
                context = applicationContext,
                surfaceHolder = surfaceHolder,
                watchState = watchState,
                currentUserStyleRepository = currentUserStyleRepository,
                canvasType = CanvasType.SOFTWARE
            )
            Log.d("e4heart", "createWatchFace: renderer creato")

            val watchFace = WatchFace(
                watchFaceType = WatchFaceType.DIGITAL,
                renderer = renderer
            )
            Log.d("e4heart", "createWatchFace: watchFace creata")
            return watchFace
        } catch (e: Exception) {
            Log.e("e4heart", "createWatchFace: ERRORE", e)
            throw e
        }
    }
}

class MyCanvasRenderer(
    private val context: Context,
    surfaceHolder: SurfaceHolder,
    private val watchState: WatchState,
    currentUserStyleRepository: CurrentUserStyleRepository,
    canvasType: Int
) : Renderer.CanvasRenderer2<Renderer.SharedAssets>(
    surfaceHolder = surfaceHolder,
    currentUserStyleRepository = currentUserStyleRepository,
    watchState = watchState,
    canvasType = canvasType,
    interactiveDrawModeUpdateDelayMillis = 1000L,
    clearWithBackgroundTintBeforeRenderingHighlightLayer = true
) {
    override suspend fun createSharedAssets(): Renderer.SharedAssets {
        return object : Renderer.SharedAssets {
            override fun onDestroy() {}
        }
    }
    private val timePaint = Paint().apply {
        color = Color.WHITE
        textSize = 90f
        textAlign = Paint.Align.CENTER
        typeface = Typeface.DEFAULT_BOLD
        isAntiAlias = true
    }

    private val datePaint = Paint().apply {
        color = Color.GRAY
        textSize = 44f
        textAlign = Paint.Align.CENTER
        isAntiAlias = true
    }

    private val heartPaint = Paint().apply {
        color = Color.WHITE
        textSize = 54f
        textAlign = Paint.Align.CENTER
        typeface = Typeface.DEFAULT_BOLD
        isAntiAlias = true
    }

    private val labelPaint = Paint().apply {
        color = Color.LTGRAY
        textSize = 34f
        textAlign = Paint.Align.CENTER
        isAntiAlias = true
    }

    private val dateFormatter = DateTimeFormatter.ofPattern("EEE dd MMM", Locale.getDefault())
    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
    private val secondsFormatter = DateTimeFormatter.ofPattern(":ss")

    private var batteryLevel = 0

    init {
        // Registra ricevitore per la batteria
        val batteryStatus: Intent? = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        batteryLevel = batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: 0
    }

    override fun render(
        canvas: Canvas,
        bounds: Rect,
        zonedDateTime: ZonedDateTime,
        sharedAssets: Renderer.SharedAssets
    ) {
        val isAmbient = watchState.isAmbient.value == true
        canvas.drawColor(Color.BLACK) // Risparmio batteria AMOLED

        val centerX = bounds.centerX().toFloat()
        val centerY = bounds.centerY().toFloat()

        // Colori attenuati per Ambient Mode (evita effetto torcia di notte)
        val ambientMainColor = Color.parseColor("#808080") // Grigio medio
        val ambientLabelColor = Color.parseColor("#404040") // Grigio scuro

        // 1. DATA (In alto) - Nascosta in Ambient Mode
        if (!isAmbient) {
            val dateText = zonedDateTime.format(dateFormatter).uppercase()
            canvas.drawText(dateText, centerX, centerY - 80f, datePaint)
        }

        // 2. ORA (Centro)
        timePaint.color = if (isAmbient) ambientMainColor else Color.WHITE
        if (isAmbient) {
            val timeText = zonedDateTime.format(timeFormatter)
            canvas.drawText(timeText, centerX, centerY + 20f, timePaint)
        } else {
            val timeText = zonedDateTime.format(timeFormatter)
            val secondsText = zonedDateTime.format(secondsFormatter)
            
            val timeWidth = timePaint.measureText(timeText)
            canvas.drawText(timeText, centerX - 15f, centerY + 20f, timePaint)
            
            val secondsPaint = Paint(datePaint).apply { textSize = 30f; color = Color.LTGRAY }
            canvas.drawText(secondsText, centerX + (timeWidth/2) + 5f, centerY + 20f, secondsPaint)
        }

        // 3. BATTERIA (Sotto l'ora)
        labelPaint.color = if (isAmbient) ambientLabelColor else Color.LTGRAY
        val batteryText = context.getString(R.string.watchface_battery_label, batteryLevel)
        canvas.drawText(batteryText, centerX, centerY + 80f, labelPaint)

        // 4. BATTITO (In fondo)
        val bpm = HeartRateService.currentBpm
        val isPaused = HeartRateService.isPausedGlobal
        
        // Colore battito: Rosso se allerta, grigio se in pausa o ambient, altrimenti bianco
        heartPaint.color = when {
            HeartRateService.isAlertingGlobal -> Color.RED
            isPaused || isAmbient -> ambientMainColor
            else -> Color.WHITE
        }
        
        val bpmText = if (isPaused) "||" else (if (bpm > 0) bpm.toString() else "--")
        
        canvas.drawText(bpmText, centerX, centerY + 140f, heartPaint)
        canvas.drawText(context.getString(R.string.watchface_bpm_unit), centerX, centerY + 180f, labelPaint)
        
        // Ricarica batteria ogni minuto circa
        if (zonedDateTime.second == 0 && (zonedDateTime.toInstant().toEpochMilli() - lastBatteryCheck > 1000)) {
            lastBatteryCheck = zonedDateTime.toInstant().toEpochMilli()
            val batteryStatus: Intent? = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
            batteryLevel = batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: batteryLevel
        }
    }

    private var lastBatteryCheck = 0L

    override fun renderHighlightLayer(canvas: Canvas, bounds: Rect, zonedDateTime: ZonedDateTime, sharedAssets: Renderer.SharedAssets) {}
}
