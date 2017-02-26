package com.bluesrv.cryptracker;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.RemoteViews;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.util.Currency;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

import static android.content.ContentValues.TAG;

/**
 * Created by Samuel on 25/02/2017.
 */

class DownloadRatesTask extends AsyncTask<Integer, Map<String, Object>, Void> {
    private Context context;
    private AppWidgetManager manager;
    private int id;
    private RemoteViews views;
    private Map<String, ?> widgetText;
    private String prefix;

    DownloadRatesTask(Context context, AppWidgetManager appWidgetManager, int appWidgetId, RemoteViews views, Map<String, ?> widgetText) {
        this.context = context;
        this.manager = appWidgetManager;
        this.id = appWidgetId;
        this.views = views;
        this.widgetText = widgetText;

        this.prefix = CryptrackerConfigureActivity.preferencesPrefix(appWidgetId);
    }

    @Override
    protected Void doInBackground(Integer... integers) {
        String targetCurrencyIdent = (String) widgetText.get(prefix + "_target");
        String sourceCryptocurrencyName;
        String pricePaidKey;
        Float numberOfCoins, usdPerCoin, pricePaid;
        HashMap<String, Object> coinRate;

        for (int i : integers) {
            Log.d(TAG, "doInBackground: " + prefix);
            Log.d(TAG, "doInBackground: " + Integer.toString(i));
            Log.d(TAG, "doInBackground: " + prefix + "_" + Integer.toString(i) + "_source");
            Log.d(TAG, "doInBackground: " + widgetText.get(prefix + "_" + Integer.toString(i) + "_source"));

            sourceCryptocurrencyName = ((String) widgetText.get(prefix + "_" + Integer.toString(i) + "_source"));
            numberOfCoins = Float.parseFloat((String) widgetText.get(prefix + "_" + Integer.toString(i) + "_amount"));

            pricePaidKey = prefix + "_" + Integer.toString(i) + "_paid";
            if (widgetText.containsKey(pricePaidKey))
                pricePaid = Float.parseFloat((String) widgetText.get(pricePaidKey));
            else
                pricePaid = 0.0f;

            String baseURL = "";
            try {
                JsonParser jp = new JsonParser();
                JsonElement root = jp.parse(new InputStreamReader(context.getAssets().open("secrets/config.json")));
                JsonObject rootobj = root.getAsJsonObject();
                baseURL = "https://" + rootobj.get("domain").getAsString();
            } catch (IOException e) {
                Log.e(TAG, "doInBackground: Could not retrieve API domain from assets.");
                break;
            }

            try {
                Uri builtUri = Uri.parse(baseURL)
                        .buildUpon()
                        .appendPath("convert")
                        .appendPath(sourceCryptocurrencyName)
                        .appendPath(numberOfCoins.toString())
                        .appendPath(targetCurrencyIdent).build();
                URL url = new URL(builtUri.toString());
                Log.d(TAG, "doInBackground: " + builtUri.toString());
                HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
                conn.connect();

                // Convert to a JSON object to print data
                JsonParser jp = new JsonParser();
                JsonElement root = jp.parse(new InputStreamReader((InputStream) conn.getContent()));
                JsonObject rootobj = root.getAsJsonObject();

                coinRate = new HashMap<String, Object>();
                coinRate.put("coins", numberOfCoins.toString());
                coinRate.put("value", rootobj.get(targetCurrencyIdent.toLowerCase()).getAsFloat());
                coinRate.put("source", sourceCryptocurrencyName);
                coinRate.put("target", targetCurrencyIdent);
                coinRate.put("change", ((Float) coinRate.get("value")) - pricePaid);

                publishProgress(coinRate);
            } catch (MalformedURLException e) {
                Log.e(TAG, "doInBackground: MalformedURLException " + e.toString());
            } catch (IOException e) {
                Log.e(TAG, "doInBackground: IOException " + e.toString());

                coinRate = new HashMap<String, Object>();
                coinRate.put("coins", numberOfCoins.toString());
                coinRate.put("value", 0.0f);
                coinRate.put("source", sourceCryptocurrencyName.toUpperCase());
                coinRate.put("target", targetCurrencyIdent.toUpperCase());
                coinRate.put("change", 0.0f);
                publishProgress(coinRate);
            }

            if (isCancelled()) break;
        }

        return null;
    }

    @Override
    protected void onProgressUpdate(Map<String, Object>... result) {
        Integer source_id, amount_id;
        NumberFormat nf = NumberFormat.getCurrencyInstance();
        nf.setCurrency(Currency.getInstance((String) result[0].get("target")));

        RemoteViews widgetRow = new RemoteViews(context.getPackageName(), R.layout.widget_row);
        widgetRow.setTextViewText(R.id.widget_row_source, ((String) result[0].get("coins")) + " " + result[0].get("source"));
        widgetRow.setTextViewText(R.id.widget_row_value, nf.format(result[0].get("value")));
        widgetRow.setTextViewText(R.id.widget_row_change, "(" + ((((Float) result[0].get("value")) > 0.0f) ? "+" : "") + nf.format(result[0].get("change")).toString() + ")");

        if (Math.signum((Float) result[0].get("change")) < 0) {
            widgetRow.setTextColor(R.id.widget_row_change, context.getResources().getColor(android.R.color.holo_red_dark));
        }
        else
            widgetRow.setTextColor(R.id.widget_row_change, context.getResources().getColor(android.R.color.holo_green_dark));

        views.addView(R.id.widget_layout_wrapper, widgetRow);

        this.manager.updateAppWidget(this.id, views);
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        GregorianCalendar gc = new GregorianCalendar();
        DateFormat dt = DateFormat.getDateTimeInstance();

        views.setTextViewText(R.id.txt_last_updated,  dt.format(gc.getTime()));

        this.manager.updateAppWidget(this.id, views);
    }
}
