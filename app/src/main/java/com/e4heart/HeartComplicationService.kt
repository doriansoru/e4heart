package com.e4heart

import androidx.wear.watchface.complications.data.ComplicationData
import androidx.wear.watchface.complications.data.ComplicationType
import androidx.wear.watchface.complications.data.PlainComplicationText
import androidx.wear.watchface.complications.data.ShortTextComplicationData
import androidx.wear.watchface.complications.datasource.ComplicationRequest
import androidx.wear.watchface.complications.datasource.SuspendingComplicationDataSourceService

class HeartComplicationService : SuspendingComplicationDataSourceService() {
    override fun getPreviewData(type: ComplicationType): ComplicationData? {
        return createComplicationData("75", type)
    }

    override suspend fun onComplicationRequest(request: ComplicationRequest): ComplicationData {
        val prefs = getSharedPreferences("e4heart_prefs", android.content.Context.MODE_PRIVATE)
        val isPaused = prefs.getBoolean("paused", false)
        val bpm = prefs.getInt("last_bpm", HeartRateService.currentBpm)
        val text = if (isPaused) "||" else (if (bpm > 0) bpm.toString() else "--")
        return createComplicationData(text, request.complicationType)
    }

    private fun createComplicationData(text: String, type: ComplicationType): ComplicationData {
        return when (type) {
            ComplicationType.SHORT_TEXT -> {
                ShortTextComplicationData.Builder(
                    text = PlainComplicationText.Builder(text).build(),
                    contentDescription = PlainComplicationText.Builder("Battito Cardiaco").build()
                ).setTitle(PlainComplicationText.Builder("BPM").build())
                .build()
            }
            else -> {
                // Ritorna un dato vuoto invece di crashare
                androidx.wear.watchface.complications.data.NoDataComplicationData()
            }
        }
    }
}
