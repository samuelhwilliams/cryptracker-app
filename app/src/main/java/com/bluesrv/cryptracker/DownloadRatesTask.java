package com.bluesrv.cryptracker;

import android.appwidget.AppWidgetManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.RemoteViews;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.NumberFormat;
import java.util.Currency;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

import static android.content.ContentValues.TAG;

/**
 * Created by Samuel on 25/02/2017.
 */

class DownloadRatesTask extends AsyncTask<Integer, Map<String, Object>, Void> {
    private final static String CRYPTO_CONVERT_BASE_URL = "";
    private final static String API_PARAM_SOURCE = "source";
    private final static String API_PARAM_AMOUNT = "amount";
    private final static String API_PARAM_TARGET = "target";

    private AppWidgetManager manager;
    private int id;
    private RemoteViews views;
    private Map<String, ?> widgetText;
    private String prefix;
    private Integer row;

    DownloadRatesTask(AppWidgetManager appWidgetManager, int appWidgetId, RemoteViews views, Map<String, ?> widgetText, String prefix) {
        this.manager = appWidgetManager;
        this.id = appWidgetId;
        this.views = views;
        this.prefix = prefix;
        this.widgetText = widgetText;
    }

    @Override
    protected Void doInBackground(Integer... integers) {
        String sourceCryptocurrencyName, targetCurrencyIdent;
        Float numberOfCoins, usdPerCoin;
        HashMap<String, Object> coinRate;

        for (int i : integers) {
            Log.d(TAG, "doInBackground: " + prefix);
            Log.d(TAG, "doInBackground: " + Integer.toString(i));
            Log.d(TAG, "doInBackground: " + prefix + "_" + Integer.toString(i) + "_source");
            Log.d(TAG, "doInBackground: " + widgetText.get(prefix + "_" + Integer.toString(i) + "_source"));

            sourceCryptocurrencyName = ((String) widgetText.get(prefix + "_" + Integer.toString(i) + "_source")).toLowerCase();
            numberOfCoins = Float.parseFloat((String) widgetText.get(prefix + "_" + Integer.toString(i) + "_amount"));
            targetCurrencyIdent = ((String) widgetText.get(prefix + "_" + Integer.toString(i) + "_target")).toLowerCase();

            try {
                Uri builtUri = Uri.parse(CRYPTO_CONVERT_BASE_URL)
                        .buildUpon()
                        .appendQueryParameter(API_PARAM_SOURCE, sourceCryptocurrencyName)
                        .appendQueryParameter(API_PARAM_AMOUNT, numberOfCoins.toString())
                        .appendQueryParameter(API_PARAM_TARGET, targetCurrencyIdent).build();
                URL url = new URL(builtUri.toString());
                HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
                conn.connect();

                // Convert to a JSON object to print data
                JsonParser jp = new JsonParser();
                JsonElement root = jp.parse(new InputStreamReader((InputStream) conn.getContent()));

                coinRate = new HashMap<String, Object>();
                coinRate.put("amount", root.getAsJsonObject().get(targetCurrencyIdent).getAsString());
                coinRate.put("target", targetCurrencyIdent);

                Log.d(TAG, "doInBackground1: " + sourceCryptocurrencyName + ", " + root.getAsJsonObject().get(sourceCryptocurrencyName).getAsString());

                this.row = i;
                publishProgress(coinRate);

                Log.d(TAG, "doInBackground2: " + sourceCryptocurrencyName + ", " + root.getAsJsonObject().get(sourceCryptocurrencyName).getAsString());
            } catch (MalformedURLException e) {
                Log.e(TAG, "doInBackground: MalformedURLException " + e.toString());
            } catch (FileNotFoundException e) {
                Log.e(TAG, "doInBackground: File Not Found. 429 - rate limited by API?");
            } catch (IOException e) {
                Log.e(TAG, "doInBackground: IOException " + e.toString());
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

        switch (this.row) {
            case 1:
                source_id = R.id.widget_row1_source;
                amount_id = R.id.widget_row1_amount;
                break;

            case 2:
                source_id = R.id.widget_row2_source;
                amount_id = R.id.widget_row2_amount;
                break;

            case 3:
                source_id = R.id.widget_row3_source;
                amount_id = R.id.widget_row3_amount;
                break;

            case 4:
                source_id = R.id.widget_row4_source;
                amount_id = R.id.widget_row4_amount;
                break;

            case 5:
                source_id = R.id.widget_row5_source;
                amount_id = R.id.widget_row5_amount;
                break;

            case 6:
            default:
                source_id = R.id.widget_row6_source;
                amount_id = R.id.widget_row6_amount;
                break;
        }

        views.setTextViewText(source_id, (String) widgetText.get(prefix + "_" + this.row.toString() + "_source"));
        views.setTextViewText(amount_id, nf.format(Float.parseFloat(result[0].get("amount").toString())));

        this.manager.updateAppWidget(this.id, views);
    }
}
