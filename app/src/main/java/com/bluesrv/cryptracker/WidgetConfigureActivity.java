package com.bluesrv.cryptracker;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TableRow;

import java.util.HashMap;
import java.util.Map;

/**
 * The configuration screen for the {@link Widget Widget} AppWidget.
 */
public class WidgetConfigureActivity extends Activity {

    private static final String PREFS_NAME = "com.bluesrv.cryptracker.Widget";
    private static final String PREF_PREFIX_KEY = "cryptracker_widget_";
    int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
    EditText mAppWidgetText;
    View.OnClickListener mOnClickListener = new View.OnClickListener() {
        public void onClick(View v) {
            final Context context = WidgetConfigureActivity.this;

            savePreferences(context, mAppWidgetId);

            // It is the responsibility of the configuration activity to update the app widget
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            Widget.updateAppWidget(context, appWidgetManager, mAppWidgetId);

            // Make sure we pass back the original appWidgetId
            Intent resultValue = new Intent();
            resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
            setResult(RESULT_OK, resultValue);
            finish();
        }
    };

    public WidgetConfigureActivity() {
        super();
    }

    static String preferencesPrefix(int appWidgetId) {
        return PREF_PREFIX_KEY + appWidgetId;
    }

    static Map<String, ?> loadCryptoPreferences(Context context, int appWidgetId) {
        final String prefix = preferencesPrefix(appWidgetId);
        SharedPreferences prefsViewer = context.getSharedPreferences(PREFS_NAME, 0);
        Map<String, String> preferencesToReturn = new HashMap<String, String>();

        Map<String, ?> preferences = prefsViewer.getAll();
        for (String key : preferences.keySet()) {
            if (key.startsWith(prefix)) {
                preferencesToReturn.put(key, preferences.get(key).toString());
            }
        }

        return preferencesToReturn;
    }

    // Delete the preferences for this widget.
    static void deletePreferences(Context context, int appWidgetId) {
        final String prefix = preferencesPrefix(appWidgetId);
        SharedPreferences prefsViewer = context.getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor prefsEditor = context.getSharedPreferences(PREFS_NAME, 0).edit();

        Map<String, ?> preferences = prefsViewer.getAll();
        for (String key : preferences.keySet()) {
            if (key.startsWith(prefix)) {
                prefsEditor.remove(key);
            }
        }

        prefsEditor.apply();
    }

    // Read the prefix from the SharedPreferences object for this widget.
    // If there is no preference saved, get the default from a resource
    static String loadTitlePref(Context context, int appWidgetId) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
        String titleValue = prefs.getString(PREF_PREFIX_KEY + appWidgetId, null);
        if (titleValue != null) {
            return titleValue;
        } else {
            return context.getString(R.string.appwidget_text);
        }
    }

    // Write the preferences for this widget out.
    void savePreferences(Context context, int appWidgetId) {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_NAME, 0).edit();

        // Retrieve rows from table; first row is headers, so start at i=1
        TableLayout mEntriesTable = (TableLayout) findViewById(R.id.table_entries);
        for (int i = 1; i < mEntriesTable.getChildCount(); i++) {
            TableRow row = (TableRow) mEntriesTable.getChildAt(i);

            EditText source = (EditText) row.getChildAt(0);
            EditText amount = (EditText) row.getChildAt(1);
            EditText target = (EditText) row.getChildAt(2);

            String prefix = preferencesPrefix(appWidgetId) + "_" + Integer.toString(i) + "_";
            prefs.putString(prefix + "source", source.getText().toString());
            prefs.putString(prefix + "amount", amount.getText().toString());
            prefs.putString(prefix + "target", target.getText().toString());
        }

        prefs.apply();
    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        // Set the result to CANCELED.  This will cause the widget host to cancel
        // out of the widget placement if the user presses the back button.
        setResult(RESULT_CANCELED);

        setContentView(R.layout.new_app_widget_configure);
        findViewById(R.id.add_button).setOnClickListener(mOnClickListener);

        // Find the widget id from the intent.
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            mAppWidgetId = extras.getInt(
                    AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        }

        // If this activity was started with an intent without an app widget ID, finish with an error.
        if (mAppWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish();
            return;
        }
    }
}

