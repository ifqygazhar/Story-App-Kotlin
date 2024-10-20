package com.example.storyapp.ui.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.RemoteViews
import com.example.storyapp.R

class StoryAppWidget : AppWidgetProvider() {
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)

        Log.d("StoryAppWidget", "onReceive action: ${intent.action}")
        if (AppWidgetManager.ACTION_APPWIDGET_UPDATE == intent.action) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val appWidgetIds = intent.getIntArrayExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS)
            if (appWidgetIds != null) {
                onUpdate(context, appWidgetManager, appWidgetIds)
            }
        }
    }

    override fun onEnabled(context: Context) {
        // Fungsi yang dijalankan ketika widget pertama kali dibuat
    }

    override fun onDisabled(context: Context) {
        // Fungsi yang dijalankan ketika widget terakhir dihapus
    }
}

internal fun updateAppWidget(
    context: Context,
    appWidgetManager: AppWidgetManager,
    appWidgetId: Int
) {
    // Set up the intent for the StackView to point to StoryWidgetService
    val intent = Intent(context, StoryWidgetService::class.java)

    // Construct the RemoteViews object
    val views = RemoteViews(context.packageName, R.layout.story_app_widget).apply {
        setRemoteAdapter(R.id.stack_view, intent)
        setEmptyView(R.id.stack_view, R.id.empty_view)
    }

    // Menginstruksikan widget manager untuk memperbarui widget
    appWidgetManager.updateAppWidget(appWidgetId, views)
}
