package com.mindmap.widget



import android.app.PendingIntent
import android.app.TaskStackBuilder
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.kikunote.R
import com.kikunote.activity.MainActivity

class TaskWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            val views = RemoteViews(context.packageName, R.layout.widget_layout)

            // Update your widget's task display here
            val taskTitle = "Next Task: ..."
            views.setTextViewText(R.id.widget_task, taskTitle)

            // Set up click handling to open the app when the widget is tapped
            val intent = Intent(context, MainActivity::class.java)
            val pendingIntent = TaskStackBuilder.create(context).addNextIntentWithParentStack(intent)
                .getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT)
            views.setOnClickPendingIntent(R.id.widget_task, pendingIntent)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}
