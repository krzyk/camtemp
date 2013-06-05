package com.kirela.android.camtemp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.RemoteViews;
import org.apache.commons.lang.StringUtils;

public class CamWidgetProvider extends AppWidgetProvider {
    private static final String LOG = CamWidgetProvider.class.getName();

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);
        Log.i(LOG, "onUpdate");
        for (int appWidgetId : appWidgetIds) {
            Log.i(LOG, "Starting widget id " + appWidgetId);
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.appwidget);
            views.setTextViewText(R.id.text, "init" + appWidgetId);
            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
    }

    @Override
    public void onDisabled(Context context) {
        super.onDisabled(context);
        Log.i(LOG, "Disabling intent");
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        super.onDeleted(context, appWidgetIds);
        Log.i(LOG, "Deleting intents: " + Arrays.toString(appWidgetIds));
        SharedPreferences prefs = context.getSharedPreferences(CamWidgetConfigure.PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor edit = prefs.edit();
        List<String> configuredIds = new ArrayList<String>(Arrays.asList(prefs.getString(CamWidgetConfigure.CONFIGURED_IDS, "").split(",")));
        for (int widgetId : appWidgetIds) {
            configuredIds.remove(Integer.toString(widgetId));
        }
        List<String> idsToRemove = new ArrayList<String>();
        for (String configuredId : configuredIds) {
            if (prefs.getString(CamWidgetConfigure.CAMERA_SET + "_" + configuredId, "").equals("")) {
                idsToRemove.add(configuredId);
            }
        }
        configuredIds.removeAll(idsToRemove);
        edit.putString(CamWidgetConfigure.CONFIGURED_IDS, StringUtils.join(configuredIds, ","));
        edit.commit();
        if (configuredIds.isEmpty()) {
            Log.i(LOG, "Removing alarm");
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            alarmManager.cancel(createPendingIntent(context));
        }
    }

    private PendingIntent createPendingIntent(Context context) {
        Intent intent = new Intent(context.getApplicationContext(), UpdateWidgetService.class);
        return PendingIntent.getService(context.getApplicationContext(), 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
    }
}

