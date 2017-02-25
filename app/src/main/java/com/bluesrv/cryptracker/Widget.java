package com.bluesrv.cryptracker;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.util.Log;
import android.widget.RemoteViews;

import java.util.Map;

import static android.content.ContentValues.TAG;

/**
 * Implementation of App Widget functionality.
 * App Widget Configuration implemented in {@link WidgetConfigureActivity WidgetConfigureActivity}
 */
public class Widget extends AppWidgetProvider {
    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {
        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.new_app_widget);
        Map<String, ?> widgetText = WidgetConfigureActivity.loadCryptoPreferences(context, appWidgetId);

        Log.d(TAG, "updateAppWidget: BLAH BLAH BLAH");
        for (String key : widgetText.keySet()) {
            Log.d(TAG, "updateAppWidget: key=" + key + ", val=" + widgetText.get(key));
        }
        Log.d(TAG, "updateAppWidget: FOO FOO FOO");

        String prefix = WidgetConfigureActivity.preferencesPrefix(appWidgetId);

        if (widgetText.size() > 0) {
            DownloadRatesTask drt = new DownloadRatesTask(appWidgetManager, appWidgetId, views, widgetText, prefix);
            drt.execute(1, 2, 3, 4, 5, 6);
        }

        // Show updating... text while rates are being downloaded
        views.setTextViewText(R.id.widget_row1_source, (String) widgetText.get(prefix + "_1_source"));
        views.setTextViewText(R.id.widget_row1_amount, "Updating...");

        views.setTextViewText(R.id.widget_row2_source, (String) widgetText.get(prefix + "_2_source"));
        views.setTextViewText(R.id.widget_row2_amount, "Updating...");

        views.setTextViewText(R.id.widget_row3_source, (String) widgetText.get(prefix + "_3_source"));
        views.setTextViewText(R.id.widget_row3_amount, "Updating...");

        views.setTextViewText(R.id.widget_row4_source, (String) widgetText.get(prefix + "_4_source"));
        views.setTextViewText(R.id.widget_row4_amount, "Updating...");

        views.setTextViewText(R.id.widget_row5_source, (String) widgetText.get(prefix + "_5_source"));
        views.setTextViewText(R.id.widget_row5_amount, "Updating...");

        views.setTextViewText(R.id.widget_row6_source, (String) widgetText.get(prefix + "_6_source"));
        views.setTextViewText(R.id.widget_row6_amount, "Updating...");

        appWidgetManager.updateAppWidget(appWidgetId, views);

//        Intent intent = new Intent(context, Widget.class);
//        intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
//        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
//        PendingIntent pendingIntent = PendingIntent.getBroadcast(context,
//                0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
//        views.setOnClickPendingIntent(R.id.rel_layout, pendingIntent);
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
            WidgetConfigureActivity.deletePreferences(context, appWidgetId);
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

