package com.bluesrv.cryptracker;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * The configuration screen for the {@link CryptrackerWidget CryptrackerWidget} AppWidget.
 */
public class CryptrackerConfigureActivity extends Activity {

    private static final String PREFS_NAME = "com.bluesrv.cryptracker.CryptrackerWidget";
    private static final String PREF_PREFIX_KEY = "cryptracker_widget_";
    static int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;

    View.OnClickListener mOnClickListener = new View.OnClickListener() {
        public void onClick(View v) {
            final Context context = CryptrackerConfigureActivity.this;

            savePreferences(context, mAppWidgetId);

            // It is the responsibility of the configuration activity to update the app widget
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            CryptrackerWidget.updateAppWidget(context, appWidgetManager, mAppWidgetId);

            // Make sure we pass back the original appWidgetId
            Intent resultValue = new Intent();
            resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
            setResult(RESULT_OK, resultValue);
            finish();
        }
    };

    static String preferencesPrefix(int appWidgetId) {
        return PREF_PREFIX_KEY + appWidgetId;
    }

    static Map<String, ?> loadPreferences(Context context, int appWidgetId) {
        final String prefix = preferencesPrefix(appWidgetId);
        final SharedPreferences prefsViewer = context.getSharedPreferences(PREFS_NAME, 0);
        final Map<String, String> preferencesToReturn = new HashMap<String, String>();

        final Map<String, ?> preferences = prefsViewer.getAll();
        for (String key : preferences.keySet()) {
            if (key.startsWith(prefix)) {
                preferencesToReturn.put(key, preferences.get(key).toString());
            }
        }

        return preferencesToReturn;
    }

    static Integer[] rowsFromPreferences(Context context, int appWidgetId) {
        final String prefix = preferencesPrefix(appWidgetId);
        final SharedPreferences prefsViewer = context.getSharedPreferences(PREFS_NAME, 0);
        final ArrayList<Integer> rows = new ArrayList<Integer>();
        int i = 1;

        // Iterate over preferences to detect the number of rows that were configured.
        while (prefsViewer.getString(prefix + "_" + Integer.toString(i) + "_source", null) != null) {
            rows.add(i++); // Increment i, after storing, before looping again.
        }

        return rows.toArray(new Integer[rows.size()]);
    }

    // Delete the preferences for this widget.
    static void deletePreferences(Context context, int appWidgetId) {
        final String prefix = preferencesPrefix(appWidgetId);
        final SharedPreferences prefsViewer = context.getSharedPreferences(PREFS_NAME, 0);
        final SharedPreferences.Editor prefsEditor = context.getSharedPreferences(PREFS_NAME, 0).edit();

        final Map<String, ?> preferences = prefsViewer.getAll();
        for (String key : preferences.keySet()) {
            if (key.startsWith(prefix)) {
                prefsEditor.remove(key);
            }
        }

        prefsEditor.apply();
    }

    // Write the preferences for this widget out.
    void savePreferences(Context context, int appWidgetId) {
        final SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_NAME, 0).edit();
        final String basePrefix = preferencesPrefix(appWidgetId) + "_";

        // Retrieve rows from table; first row is headers, so start at i=1
        final TableLayout mEntriesTable = (TableLayout) findViewById(R.id.table_entries);
        for (int i = 1; i < mEntriesTable.getChildCount(); i++) {
            TableRow row = (TableRow) mEntriesTable.getChildAt(i);

            EditText source = (EditText) row.getChildAt(0);
            EditText amount = (EditText) row.getChildAt(1);
            EditText paid = (EditText) row.getChildAt(2);

            String prefix = basePrefix + Integer.toString(i) + "_";
            prefs.putString(prefix + "source", source.getText().toString());
            prefs.putString(prefix + "amount", amount.getText().toString());
            prefs.putString(prefix + "paid", paid.getText().toString());
        }

        final Spinner fiatCurrenciesSpinner = (Spinner) findViewById(R.id.spinner_fiat_currencies);
        prefs.putString(basePrefix + "target", fiatCurrenciesSpinner.getSelectedItem().toString());

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

        Spinner fiatCurrenciesSpinner = (Spinner) findViewById(R.id.spinner_fiat_currencies);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.supported_fiat_currencies, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        fiatCurrenciesSpinner.setAdapter(adapter);

        // If this activity was started with an intent without an app widget ID, finish with an error.
        if (mAppWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish();
            return;
        }
    }
}

