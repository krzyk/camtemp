package com.kirela.android.camtemp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.IntentService;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;
import org.apache.commons.lang.StringUtils;

public class UpdateWidgetService extends IntentService {
    private static final String LOG = UpdateWidgetService.class.getName();

    public UpdateWidgetService() {
        super("name");
    }

    public UpdateWidgetService(String name) {
        super(name);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.i(LOG, "Called");
        AppWidgetManager mgr = AppWidgetManager.getInstance(getApplicationContext());
        int[] widgetIds = mgr.getAppWidgetIds(new ComponentName(getApplicationContext(), CamWidgetProvider.class));

        for (int widgetId : widgetIds) {
            Log.i(LOG, "Updating widget id " + widgetId);
            refreshText(mgr, widgetId);

            SharedPreferences prefs = getApplicationContext().getSharedPreferences(CamWidgetConfigure.PREFS_NAME, Context.MODE_PRIVATE);

            List<String> cameraSet = split(prefs.getString(CamWidgetConfigure.CAMERA_SET + "_" + widgetId, ""));
            List<String> temps = new ArrayList<String>();
            for (String camId : cameraSet) {
                int retries = 3;
                String temp = null;
                for (int i = 0; i < retries && temp == null; i++) {
                    temp = tempFromCamera(Integer.valueOf(camId));
                }
                temps.add(temp);
            }
            StringBuilder result = new StringBuilder();
            for (String temp : temps) {
                if (temp == null) {
                    result.append("?");
                } else {
                    result.append(temp);
                }
                result.append("\n");
            }
            if (result.length() > 0) {
                result.delete(result.length() - 1, result.length());
            }
            RemoteViews remoteViews = new RemoteViews(getApplicationContext().getPackageName(), R.layout.appwidget);
            remoteViews.setTextViewText(R.id.text, result.toString());
            remoteViews.setViewVisibility(R.id.text, View.VISIBLE);
            remoteViews.setViewVisibility(R.id.progress, View.INVISIBLE);
            mgr.updateAppWidget(widgetId, remoteViews);
        }
    }

    private List<String> split(String text) {
        if (StringUtils.isBlank(text)) {
            return Collections.emptyList();
        }
        return new ArrayList<String>(Arrays.asList(text.split(",")));
    }

    private void refreshText(AppWidgetManager mgr, int widgetId) {
        RemoteViews remoteViews = new RemoteViews(getApplicationContext().getPackageName(), R.layout.appwidget);
        remoteViews.setViewVisibility(R.id.text, View.INVISIBLE);
        remoteViews.setViewVisibility(R.id.progress, View.VISIBLE);
        mgr.updateAppWidget(widgetId, remoteViews);
    }

    private String tempFromCamera(int camId) {
        ConnectivityManager mgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo network = mgr.getActiveNetworkInfo();
        if ((network != null) && network.isConnected()) {
            String html;
            html = new UrlDownloader().download("http://www.traxelektronik.pl/pogoda/kamery/kamera.php?pkamnum=" + camId);
            Pattern tempPat = Pattern.compile(".*Temp. powietrza:.*?<b>([0-9.]+?)</b>.*", Pattern.MULTILINE | Pattern.DOTALL);
            Matcher matcher = tempPat.matcher(html);
            if (matcher.matches()) {
                Log.i(LOG, "---" + matcher.group(1));
                return matcher.group(1) + "°C";
            }
            return null;
        }
        return "⚠";
    }

}
