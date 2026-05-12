package com.e4heart

import android.content.Context
import androidx.wear.protolayout.ActionBuilders
import androidx.wear.protolayout.ColorBuilders
import androidx.wear.protolayout.DeviceParametersBuilders
import androidx.wear.protolayout.LayoutElementBuilders
import androidx.wear.protolayout.ModifiersBuilders
import androidx.wear.protolayout.TimelineBuilders
import androidx.wear.tiles.ResourceBuilders
import androidx.wear.protolayout.material.Text
import androidx.wear.protolayout.material.Typography
import androidx.wear.protolayout.material.layouts.PrimaryLayout
import androidx.wear.tiles.RequestBuilders
import androidx.wear.tiles.TileBuilders
import androidx.wear.tiles.TileService
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture

class HeartRateTileService : TileService() {
    override fun onTileRequest(requestParams: RequestBuilders.TileRequest): ListenableFuture<TileBuilders.Tile> {
        android.util.Log.d("e4heart", "Tile onTileRequest")
        try {
            val prefs = getSharedPreferences("e4heart_prefs", Context.MODE_PRIVATE)
            val bpm = prefs.getInt("last_bpm", HeartRateService.currentBpm)
            val bpmText = if (bpm > 0) bpm.toString() else "--"

            val tile = TileBuilders.Tile.Builder()
                .setResourcesVersion("1")
                .setTileTimeline(
                    TimelineBuilders.Timeline.Builder().addTimelineEntry(
                        TimelineBuilders.TimelineEntry.Builder().setLayout(
                            LayoutElementBuilders.Layout.Builder().setRoot(
                                layout(bpmText)
                            ).build()
                        ).build()
                    ).build()
                ).build()
            
            android.util.Log.d("e4heart", "Tile creata con successo")
            return Futures.immediateFuture(tile)
        } catch (e: Exception) {
            android.util.Log.e("e4heart", "Errore nella creazione della Tile", e)
            throw e
        }
    }

    private fun layout(bpmText: String): LayoutElementBuilders.LayoutElement {
        return LayoutElementBuilders.Column.Builder()
            .addContent(
                LayoutElementBuilders.Text.Builder()
                    .setText(getString(R.string.tile_label))
                    .setFontStyle(
                        LayoutElementBuilders.FontStyle.Builder()
                            .setSize(androidx.wear.protolayout.DimensionBuilders.sp(14f))
                            .setColor(ColorBuilders.argb(0xFFBDBDBD.toInt()))
                            .build()
                    )
                    .build()
            )
            .addContent(
                LayoutElementBuilders.Text.Builder()
                    .setText(bpmText)
                    .setFontStyle(
                        LayoutElementBuilders.FontStyle.Builder()
                            .setSize(androidx.wear.protolayout.DimensionBuilders.sp(40f))
                            .setWeight(LayoutElementBuilders.FONT_WEIGHT_BOLD)
                            .setColor(ColorBuilders.argb(0xFFFFFFFF.toInt()))
                            .build()
                    )
                    .build()
            )
            .addContent(
                LayoutElementBuilders.Text.Builder()
                    .setText(getString(R.string.watchface_bpm_unit))
                    .setFontStyle(
                        LayoutElementBuilders.FontStyle.Builder()
                            .setSize(androidx.wear.protolayout.DimensionBuilders.sp(16f))
                            .setColor(ColorBuilders.argb(0xFFFF5252.toInt()))
                            .build()
                    )
                    .build()
            )
            .setModifiers(
                ModifiersBuilders.Modifiers.Builder()
                    .setClickable(
                        ModifiersBuilders.Clickable.Builder()
                            .setOnClick(
                                ActionBuilders.LaunchAction.Builder()
                                    .setAndroidActivity(
                                        ActionBuilders.AndroidActivity.Builder()
                                            .setPackageName(packageName)
                                            .setClassName(MainActivity::class.java.name)
                                            .build()
                                    ).build()
                            ).build()
                    ).build()
            )
            .build()
    }

    override fun onResourcesRequest(requestParams: RequestBuilders.ResourcesRequest): ListenableFuture<ResourceBuilders.Resources> {
        return Futures.immediateFuture(
            ResourceBuilders.Resources.Builder().setVersion("1").build()
        )
    }
}
