package com.prikshit.recorder.activities;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.prikshit.recorder.R;
import com.prikshit.recorder.constants.Constants;
import com.prikshit.recorder.main.AlarmManagers;
import com.prikshit.recorder.main.Logger;
import com.prikshit.recorder.main.TmpData;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;


import static com.prikshit.recorder.constants.Constants.*;
/**
 * A {@link PreferenceActivity} that presents a set of application settings. On
 * handset devices, settings are presented as a single list. On tablets,
 * settings are split by category, with category headers shown to the left of
 * the list of settings.
 * <p/>
 * See <a href="http://developer.android.com/design/patterns/settings.html">
 * Android Design: Settings</a> for design guidelines and the <a
 * href="http://developer.android.com/guide/topics/ui/settings.html">Settings
 * API Guide</a> for more information on developing a Settings UI.
 */
public class Settings extends PreferenceActivity implements OnSharedPreferenceChangeListener{
    private final static String TAG = "settingsActivity";
    /**
     * Determines whether to always show the simplified settings UI, where
     * settings are presented in a single list. When false, settings are shown
     * as a master/detail two-pane view on tablets. When true, a single pane is
     * shown on tablets.
     */
    private static final boolean ALWAYS_SIMPLE_PREFS = false;
    private static Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setupSimplePreferencesScreen();
        addActionBar();
        // Next line may not be compatible with Gingerbread or lower versions.
        // Fixed but not tested
        /*
        LinearLayout root = (LinearLayout)findViewById(android.R.id.list).getParent().getParent().getParent();
        Toolbar bar = (Toolbar) LayoutInflater.from(this).inflate(R.layout.settings_appbar, root, false);
        root.addView(bar, 0); // insert at top
        bar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        */
    }

    public void addActionBar(){
         Toolbar bar;
         if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
             LinearLayout root = (LinearLayout) findViewById(android.R.id.list).getParent().getParent().getParent();
             bar = (Toolbar) LayoutInflater.from(this).inflate(R.layout.settings_appbar, root, false);
             root.addView(bar, 0); // insert at top
         }
         else {
            ViewGroup root = (ViewGroup) findViewById(android.R.id.content);
            ListView content = (ListView) root.getChildAt(0);
             root.removeAllViews();

             bar = (Toolbar) LayoutInflater.from(this).inflate(R.layout.settings_appbar, root, false);

             int height;
             TypedValue tv = new TypedValue();
             if (getTheme().resolveAttribute(R.attr.actionBarSize, tv, true)) {
                height = TypedValue.complexToDimensionPixelSize(tv.data, getResources().getDisplayMetrics());
             }else{
                height = bar.getHeight();
             }
             content.setPadding(0, height, 0, 0);
             root.addView(content);
             root.addView(bar);
        }
        bar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        context = getBaseContext();
    }

    @Override
    public void onResume(){
        super.onResume();
        // registering shared Prefs Listener
        PreferenceManager
                .getDefaultSharedPreferences(context)
                .registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause(){
        super.onPause();
        // unregister shared Prefs Listener
        PreferenceManager
                .getDefaultSharedPreferences(context)
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    /**
     * Shows the simplified settings UI if the device configuration if the
     * device configuration dictates that a simplified, single-pane UI should be
     * shown.
     */
    private void setupSimplePreferencesScreen() {
        if (!isSimplePreferences(this)) {
            return;
        }
        // Add preferences from general xml file.
        addPreferencesFromResource(R.xml.pref_general);

        // listener for resetLogFile Button
        final Preference resetLogButton = findPreference("resetLogButton");
        if (resetLogButton != null) {
            resetLogButton.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    //Reset the log File here.
                    File sdDirectory = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + Constants.DIRECTORY);
                    File logFile = new File(sdDirectory, Constants.LOGFILE_NAME);
                    FileWriter writer;
                    try {
                        if (!logFile.exists()) {
                            logFile.createNewFile();
                        }
                        writer = new FileWriter(logFile, false);
                        writer.write("");
                        writer.close();
                        Toast.makeText(context,"Logs Deleted",Toast.LENGTH_SHORT).show();
                    } catch (IOException e) {
                        Logger.e(TAG, "Unable to open FileWrite to logFile: " + e.getMessage());
                    }
                    return true;
                }
            });
        }

        final Preference uploadNowButton = findPreference("uploadNow");
        if(uploadNowButton != null){
            uploadNowButton.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    // start the uploader service now
                    context.startService(new Intent(context, Constants.UPLOADER_CLASS));
                    return true;
                }
            });
        }

        // Bind the summaries of EditText/List/Dialog/Ringtone preferences to
        // their values. When their values change, their summaries are updated
        // to reflect the new value, per the Android Design guidelines.
        bindPreferenceSummaryToValue(findPreference("checkStopInterval"));
        bindPreferenceSummaryToValue(findPreference("checkStartInterval"));
        bindPreferenceSummaryToValue(findPreference("speedThreshold"));
        bindPreferenceSummaryToValue(findPreference("lowBatteryLevel"));

        bindPreferenceSummaryToValue(findPreference("serverAddress"));
        bindPreferenceSummaryToValue(findPreference("upload_frequency"));
        bindPreferenceSummaryToValue(findPreference("min_upload_file_size"));
    }

    /**
     * Whenever there is a change to shared Preferences, this is called.
     * This is only registered after the setting has been opened because only then Shared Preferences will be changed
     * @param sharedPreferences
     * @param key
     */
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

        if(key.equals("loggingEnabled")){
            // no need to do anything in this case, as the logger checks for logging variable every time
            LOGGING_ENABLED = sharedPreferences.getBoolean(key, true);
            Log.i(TAG, key + " changed to "+ LOGGING_ENABLED);
        }

        else if (key.equals("checkStartInterval")){
            String value = sharedPreferences.getString(key,"30");
            if( value.isEmpty() ){
                value = "30";
            }
            if(Integer.parseInt(value)==0){
                CHECK_START_INTERVAL = 60*1000*30;
            }
            else{
                CHECK_START_INTERVAL = 60*1000*Integer.parseInt(value);
            }
            Log.i(TAG, key + " changed to "+ CHECK_STOP_INTERVAL);

            // update the start alarm if running (just start the alarm again)
            if(TmpData.isStartAlarmRunning()) {
                AlarmManagers.startAlarm(getBaseContext(), AUTO_START_RECORDING_CLASS);
            }
        }

        else if (key.equals("checkStopInterval")){
            String value = sharedPreferences.getString(key,"15");
            if( value.isEmpty() ){
                value = "15";
            }
            if(Integer.parseInt(value)==0){
                CHECK_STOP_INTERVAL = 60*1000*10;
            }
            else{
                CHECK_STOP_INTERVAL = 60*1000* Integer.parseInt(value);
            }
            CHECK_STOP_INTERVAL = 60*1000* Integer.parseInt(value);
            Log.i(TAG, key + " changed to "+ CHECK_STOP_INTERVAL);
            // update the start alarm if running (just start the alarm again)
            if(TmpData.isStopAlarmRunning()){
                AlarmManagers.startAlarm(getBaseContext(), AUTO_STOP_RECORDING_CLASS);
            }
        }

        else if (key.equals("speedThreshold")){
            String value = sharedPreferences.getString(key, "8.0f");
            if(value.isEmpty()){
                value = "8.0f";
            }
            if(Float.parseFloat(value)==0.0f){
                SPEED_THRESHOLD = 0.8f;
            }
            else
                SPEED_THRESHOLD = Float.parseFloat(value);
            Log.i(TAG, key + " changed to "+ SPEED_THRESHOLD);
            // no need to do anything here also, as this is checked again and again
        }

        else if(key.equals("notifications_auto_start_stop")){
            NOTIFICATION_ENABLED = sharedPreferences.getBoolean(key,false);
            Log.i(TAG, key + " changed to "+ NOTIFICATION_ENABLED);
        }

        else if(key.equals("lowBatteryLevel")){
            String value = sharedPreferences.getString(key,"");
            if(value.isEmpty() || Integer.parseInt(value) <= 5){
                BATTERY_SAVER_LEVEL = 30;
            }
            else {
                BATTERY_SAVER_LEVEL = Integer.parseInt(value);
            }
        }

        else if(key.equals("serverAddress")){
            String value = sharedPreferences.getString(key, "10.1.201.41");
            if(value.isEmpty()){
                value = "10.1.201.41";
            }
            SERVER_ADDRESS = value;
            Log.i(TAG, key + " changed to "+ SERVER_ADDRESS);
            // update the server Address variable
        }

        else if (key.equals("upload_frequency")){
            String value = sharedPreferences.getString(key,"60");
            if(!value.isEmpty()){
                if(Integer.parseInt(value)==0){
                    //do nothing here
                }
                else{
                    System.out.println("freq changed");
                    UPLOADER_INTERVAL = 60*1000*Integer.parseInt(value);
                    System.out.println("new freq "+ UPLOADER_INTERVAL);
                    AlarmManagers.startUploaderAlarm(context);
                }
            }
        }

        else if(key.equals("min_upload_file_size")){
            String value = sharedPreferences.getString(key,"100");
            if(!value.isEmpty()){
                if(Integer.parseInt(value)==0){
                    // do nothing here
                }
                else{
                    //upload the constant variable for SIZE LIMIT
                    MIN_UPLOAD_SIZE_LIMIT = Integer.parseInt(value)*1024*1024;
                    Log.i(TAG, "new size limit: " + MIN_UPLOAD_SIZE_LIMIT);
                }
            }
        }

        else{
            String value = sharedPreferences.getString(key,"");
            Log.i(TAG, key+" "+" changed "+value );
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onIsMultiPane() {
        return isXLargeTablet(this) && !isSimplePreferences(this);
    }

    /**
     * Helper method to determine if the device has an extra-large screen. For
     * example, 10" tablets are extra-large.
     */
    private static boolean isXLargeTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_XLARGE;
    }

    /**
     * Determines whether the simplified settings UI should be shown. This is
     * true if this is forced via {@link #ALWAYS_SIMPLE_PREFS}, or the device
     * doesn't have newer APIs like {@link PreferenceFragment}, or the device
     * doesn't have an extra-large screen. In these cases, a single-pane
     * "simplified" settings UI should be shown.
     */
    private static boolean isSimplePreferences(Context context) {
        return ALWAYS_SIMPLE_PREFS
                || Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB
                || !isXLargeTablet(context);
    }

    /**
     * A preference value change listener that updates the preference's summary
     * to reflect its new value.
     */
    private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {
            String stringValue = value.toString();

            if (preference instanceof ListPreference) {
                // For list preferences, look up the correct display value in
                // the preference's 'entries' list.
                ListPreference listPreference = (ListPreference) preference;
                int index = listPreference.findIndexOfValue(stringValue);

                // Set the summary to reflect the new value.
                preference.setSummary(
                        index >= 0
                                ? listPreference.getEntries()[index]
                                : null);

            }
            else {
                // For all other preferences, set the summary to the value's
                // simple string representation.
                preference.setSummary(stringValue);
            }
            return true;
        }

        /**
         * Binding notification preference to summary not of any use in our app
         * else if (preference instanceof RingtonePreference) {
         // For ringtone preferences, look up the correct display value
         // using RingtoneManager.
         if (TextUtils.isEmpty(stringValue)) {
         // Empty values correspond to 'silent' (no ringtone).
         preference.setSummary(R.string.pref_ringtone_silent);

         } else {
         Ringtone ringtone = RingtoneManager.getRingtone(
         preference.getContext(), Uri.parse(stringValue));

         if (ringtone == null) {
         // Clear the summary if there was a lookup error.
         preference.setSummary(null);
         } else {
         // Set the summary to reflect the new ringtone display
         // name.
         String name = ringtone.getTitle(preference.getContext());
         preference.setSummary(name);
         }
         }
         */
    };

    /**
     * Binds a preference's summary to its value. More specifically, when the
     * preference's value is changed, its summary (line of text below the
     * preference title) is updated to reflect the value. The summary is also
     * immediately updated upon calling this method. The exact display format is
     * dependent on the type of preference.
     *
     * @see #sBindPreferenceSummaryToValueListener
     */
    private static void bindPreferenceSummaryToValue(Preference preference) {
        // Set the listener to watch for value changes.
        preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

        // Trigger the listener immediately with the preference's
        // current value.
        sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                PreferenceManager
                        .getDefaultSharedPreferences(preference.getContext())
                        .getString(preference.getKey(), ""));
    }

    // following code is for HONEYCOMB API only (i.e. tablets)

    /**
     * This fragment shows general preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class GeneralPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_general);

            // Bind the summaries of EditText/List/Dialog/Ringtone preferences
            // to their values. When their values change, their summaries are
            // updated to reflect the new value, per the Android Design
            // guidelines.
            bindPreferenceSummaryToValue(findPreference("checkStopInterval"));
            bindPreferenceSummaryToValue(findPreference("checkStartInterval"));
            bindPreferenceSummaryToValue(findPreference("speedThreshold"));
            bindPreferenceSummaryToValue(findPreference("lowBatteryLevel"));

            bindPreferenceSummaryToValue(findPreference("serverAddress"));
            bindPreferenceSummaryToValue(findPreference("upload_frequency"));
            bindPreferenceSummaryToValue(findPreference("min_upload_file_size"));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void onBuildHeaders(List<Header> target) {
        if (!isSimplePreferences(this)) {
            loadHeadersFromResource(R.xml.pref_headers, target);
        }
    }
}
