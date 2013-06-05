package com.kirela.android.camtemp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import org.apache.commons.lang.StringUtils;

public class CamWidgetConfigure extends Activity {
    private static final String LOG = CamWidgetConfigure.class.getName();

    public static final String PREFS_NAME = "com.example.first.PREFS";
    public static final String CAMERA_SET = "cameraSet";
    public static final String CONFIGURED_IDS = "configuredIds";
    public static final String ALARM_STARTED = "alarmStarted";

    private final CameraDao cameraDao = new CameraDao();
    private List<Camera> camList;

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.widget_configure);
        //Spinner spinner = (Spinner) findViewById(R.id.camera_id);
        //ArrayAdapter<Camera> adapter = new ArrayAdapter<Camera>(this, android.R.layout.simple_spinner_item, cameraDao.getAll());
        //adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //spinner.setAdapter(adapter);
        //spinner.setEnabled(false);
        setResult(RESULT_CANCELED);
        Button button = (Button)findViewById(R.id.save_id);
        button.setEnabled(false);
        ProgressBar progressBar = (ProgressBar)findViewById(R.id.progress_id);
        ListView listView = (ListView)findViewById(R.id.camera_ids);
        listView.setVisibility(View.INVISIBLE);
        new CameraDownloadTask(button, progressBar, listView).execute("http://www.traxelektronik.pl/pogoda/kamery/index.php");
    }

    private class CameraDownloadTask extends AsyncTask<String, Void, ArrayAdapter<Camera>> {

        private final Button button;
        private final ProgressBar progressBar;
        private final ListView listView;

        public CameraDownloadTask(Button button, ProgressBar progressBar, ListView listView) {
            this.button = button;
            this.progressBar = progressBar;
            this.listView = listView;
        }

        @Override
        protected ArrayAdapter<Camera> doInBackground(String... urls) {
            List<Camera> camList = cameraDao.getAll(urls[0]);
            CamWidgetConfigure.this.camList = camList;
            ArrayAdapter <Camera> adapter = new ArrayAdapter<Camera>(CamWidgetConfigure.this, android.R.layout.simple_list_item_multiple_choice, camList);
            //adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            return adapter;
        }

        @Override
        protected void onPostExecute(ArrayAdapter <Camera> adapter) {
            button.setEnabled(true);
            progressBar.setVisibility(View.GONE);
            listView.setAdapter(adapter);
            listView.setVisibility(View.VISIBLE);
        }
    }

    public void saveConfig(View view) {
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        int widgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
        if (extras != null) {
            widgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        }
        Log.i(LOG, "widgetId = " + widgetId);
        if (widgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish();
            return;
        }
        ListView cameraList = (ListView) findViewById(R.id.camera_ids);
        SparseBooleanArray ids = cameraList.getCheckedItemPositions();
        ListAdapter adapter = cameraList.getAdapter();
        List<Integer> selectedCamIds = new ArrayList<Integer>();
        for (int i = 0; i < adapter.getCount(); i++) {
            if (ids.get(i)) {
                selectedCamIds.add(((Camera) adapter.getItem(i)).getId());
            }
        }
        Log.i(LOG, "Selected camIds = " + selectedCamIds.toString());
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        Log.i(LOG, "Configured id = " + widgetId);
        editor.putString(CAMERA_SET + "_" + widgetId, StringUtils.join(selectedCamIds, ","));
        editor.commit();

        Log.i(LOG, "Starting service, because there is at least one widget");
        SharedPreferences.Editor edit = prefs.edit();
        edit.putBoolean(CamWidgetConfigure.ALARM_STARTED, true);
        List<String> configuredIds = new ArrayList<String>(Arrays.asList(prefs.getString(CamWidgetConfigure.CONFIGURED_IDS, "").split(",")));
        configuredIds.add(Integer.toString(widgetId));
        edit.putString(CamWidgetConfigure.CONFIGURED_IDS, StringUtils.join(configuredIds, ","));
        edit.commit();
        AlarmManager alarmManager = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
        alarmManager.setRepeating(AlarmManager.RTC, System.currentTimeMillis(),
            10 * 1000, createPendingIntent(getApplicationContext()));

        Intent result = new Intent();
        result.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
        setResult(RESULT_OK, result);
        finish();
    }

    private PendingIntent createPendingIntent(Context context) {
        Intent intent = new Intent(context.getApplicationContext(), UpdateWidgetService.class);
        return PendingIntent.getService(context.getApplicationContext(), 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
    }
}
