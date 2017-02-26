package com.bluesrv.cryptracker;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import java.util.Map;

/**
 * Implementation of App CryptrackerWidget functionality.
 * App CryptrackerWidget Configuration implemented in {@link CryptrackerConfigureActivity CryptrackerConfigureActivity}
 */
public class CryptrackerWidget extends AppWidgetProvider {
    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {
        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.new_app_widget);
        Map<String, ?> widgetText = CryptrackerConfigureActivity.loadPreferences(context, appWidgetId);

        // Clear out the widget and repopulate.
        views.removeAllViews(R.id.widget_layout_wrapper);

        Intent intent = new Intent(context, CryptrackerWidget.class);
        intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);

        // super.onReceive only handles EXTRA_APPWIDGET_IDS, not EXTRA_APPWIDGET_ID, so add it as
        // an array.
        int[] appWidgetIds = new int[1];
        appWidgetIds[0] = appWidgetId;
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        views.setOnClickPendingIntent(R.id.widget_layout_wrapper, pendingIntent);

        appWidgetManager.updateAppWidget(appWidgetId, views);

        // Kick off the background task to update rates.
        if (widgetText.size() > 0) {
            DownloadRatesTask drt = new DownloadRatesTask(context, appWidgetManager, appWidgetId, views, widgetText);
            drt.execute(CryptrackerConfigureActivity.rowsFromPreferences(context, appWidgetId));
        }
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        // When the user deletes the widget, delete the preference associated with it.
        for (int appWidgetId : appWidgetIds) {
            CryptrackerConfigureActivity.deletePreferences(context, appWidgetId);
        }
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }
}

