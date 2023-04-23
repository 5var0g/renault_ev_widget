package com.example.renault_app

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.view.View
import android.widget.RemoteViews

const val WIDGET_SYNC = "WIDGET_SYNC"

/**
 * Implementation of App Widget functionality.
 */
class BatteryWidget : AppWidgetProvider() {
    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        // There may be multiple widgets active, so update all of them
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onEnabled(context: Context) {
    }

    override fun onDisabled(context: Context) {
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)

        if(WIDGET_SYNC == intent.action) {
            val appWidgetId = intent.getIntExtra("appWidgetId", 0)
            updateAppWidget(context, AppWidgetManager.getInstance(context), appWidgetId)
        }
    }

    companion object {
        internal fun updateAppWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            val intent = Intent(context, BatteryWidget::class.java)
            intent.action = WIDGET_SYNC
            intent.putExtra("appWidgetId", appWidgetId)
            val pendingIntent = PendingIntent.getBroadcast(context, 0, intent,PendingIntent.FLAG_MUTABLE)

            val views = RemoteViews(context.packageName, R.layout.battery_widget)
            views.setOnClickPendingIntent(R.id.widget_layout, pendingIntent)

            getRenaultData(context)

            views.setTextViewText(R.id.appwidget_battery_level, "$batteryLevel %")
            views.setTextViewText(R.id.appwidget_battery_autonomy, "$batteryAutonomy km")
            views.setTextViewText(R.id.appwidget_battery_charging_stat, "$chargingRemainingTime min / $chargingInstantaneousPower kW")
            views.setTextViewText(R.id.appwidget_battery_charging_status, plugStatus(plugStatus, chargingStatus))
            views.setTextViewText(R.id.appwidget_timestamp, "$timestampShort / $updated")

            if(batteryLevel.toIntOrNull() != null) {
                if (batteryLevel.toInt() <= 10) views.setTextColor(
                    R.id.appwidget_battery_level,
                    Color.RED
                )
                else if (batteryLevel.toInt() <= 20) views.setTextColor(
                    R.id.appwidget_battery_level,
                    Color.parseColor("#FFA500")
                )
                else views.setTextColor(R.id.appwidget_battery_level, Color.WHITE)
            }
            else views.setTextColor(R.id.appwidget_battery_level, Color.WHITE)

            if(chargingStatus == "1.0")views.setViewVisibility(R.id.appwidget_battery_charging_stat,View.VISIBLE)
            else views.setViewVisibility(R.id.appwidget_battery_charging_stat, View.GONE)

            if (plugStatus(plugStatus, chargingStatus) != "") views.setViewVisibility(R.id.appwidget_battery_charging_status, View.VISIBLE)
            else views.setViewVisibility(R.id.appwidget_battery_charging_status, View.GONE)

            // Instruct the widget manager to update the widget
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}



