package com.elynn.bitdna.bitchain;


import android.annotation.TargetApi;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.SwitchPreference;
import android.provider.Settings;
import android.support.v7.app.ActionBar;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.RingtonePreference;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import com.elynn.bitdna.bitchain.Logic.API;
import com.elynn.bitdna.bitchain.Logic.LoginLogic;
import com.elynn.bitdna.bitchain.Model.PathConfiguration;
import com.elynn.bitdna.bitchain.Model.Response;
import com.elynn.bitdna.bitchain.Model.StatisGlobal;
import com.elynn.bitdna.bitchain.Model.UserLogin;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;

import android.provider.Settings.Secure;

import static java.security.AccessController.getContext;

/**
 * A {@link PreferenceActivity} that presents a set of application settings. On
 * handset devices, settings are presented as a single list. On tablets,
 * settings are split by category, with category headers shown to the left of
 * the list of settings.
 * <p>
 * See <a href="http://developer.android.com/design/patterns/settings.html">
 * Android Design: Settings</a> for design guidelines and the <a
 * href="http://developer.android.com/guide/topics/ui/settings.html">Settings
 * API Guide</a> for more information on developing a Settings UI.
 */
public class SettingsActivity extends AppCompatPreferenceActivity {

    /**
     * A preference value change listener that updates the preference's summary
     * to reflect its new value.
     */
    private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {
            Log.d("debug", "onPreferenceChange " + preference.toString() );
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

            } else if (preference instanceof RingtonePreference) {
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

            } else {
                // For all other preferences, set the summary to the value's
                // simple string representation.
                preference.setSummary(stringValue);
            }
            return true;
        }
    };

    /**
     * Helper method to determine if the device has an extra-large screen. For
     * example, 10" tablets are extra-large.
     */
    private static boolean isXLargeTablet(Context context) {
        Log.d("debug", "isXLargeTablet ");
        return (context.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_XLARGE;
    }

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
        Log.d("debug", "bindPreferenceSummaryToValue ");
        // Set the listener to watch for value changes.
        preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

        // Trigger the listener immediately with the preference's
        // current value.
        sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                PreferenceManager
                        .getDefaultSharedPreferences(preference.getContext())
                        .getString(preference.getKey(), ""));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("debug", "onCreate ");
        super.onCreate(savedInstanceState);
        setupActionBar();
    }

    /**
     * Set up the {@link android.app.ActionBar}, if the API is available.
     */
    private void setupActionBar() {
        Log.d("debug", "setupActionBar ");
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            // Show the Up button in the action bar.
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.d("debug", "onOptionsItemSelected");
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onIsMultiPane() {
        return isXLargeTablet(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void onBuildHeaders(List<Header> target) {
        Log.d("debug", "onBuildHeaders ");
        loadHeadersFromResource(R.xml.pref_headers, target);
    }

    /**
     * This method stops fragment injection in malicious applications.
     * Make sure to deny any unknown fragments here.
     */
    protected boolean isValidFragment(String fragmentName) {
        return PreferenceFragment.class.getName().equals(fragmentName)
                || GeneralPreferenceFragment.class.getName().equals(fragmentName)
                || DataSyncPreferenceFragment.class.getName().equals(fragmentName)
                || NotificationPreferenceFragment.class.getName().equals(fragmentName);
    }

    /**
     * This fragment shows general preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class GeneralPreferenceFragment extends PreferenceFragment {
        private String isGoogleOTPenabled="false";
        private String isFingerEnabled="false";
        private Response response;
        SwitchPreference googleotp_switch;
        SwitchPreference fingerprint_switch;
        EditTextPreference txtShareKey;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_general);
            setHasOptionsMenu(true);
            Log.d("debug", "Setting General Click");

            googleotp_switch = (SwitchPreference) findPreference("googleotp_switch");
            fingerprint_switch = (SwitchPreference) findPreference("fingerprint_switch");
            txtShareKey = (EditTextPreference) findPreference("txtShareKey");

            googleotp_switch.setChecked(UserLogin.enable);
            fingerprint_switch.setChecked(UserLogin.isFingerPrintEnabled);

            txtShareKey.setEnabled(UserLogin.enable);
            txtShareKey.setText(UserLogin.google2FAsecret);

            googleotp_switch.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {

                @Override
                public boolean onPreferenceChange(Preference arg0, Object isdataTrafficEnabled) {
                    boolean isDataTrafficOn = ((Boolean) isdataTrafficEnabled).booleanValue();
                    Log.d("debug", "googleotp_switch changed to " + isDataTrafficOn);
                    EnableGoogleOTPTask(isDataTrafficOn);
                    return true;
                }
            });
            fingerprint_switch.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {

                @Override
                public boolean onPreferenceChange(Preference arg0, Object isdataTrafficEnabled) {
                    boolean isDataTrafficOn = ((Boolean) isdataTrafficEnabled).booleanValue();
                    //String id = Settings.Secure.getString(getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID);
                    Log.d("debug", "fingerprint_switch changed to " + isDataTrafficOn);
                    EnableFingerPrintTask(isDataTrafficOn);
                    return true;
                }
            });

            txtShareKey.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Log.d("debug", "txtShareKey setOnPreferenceClickListener" +preference.getKey());
                    if (preference.getKey().equals("txtShareKey")){
                        txtShareKey.setText(UserLogin.google2FAsecret);
                    }
                    //clipboard.setPrimaryClip(clip);
                    return false;
                }
            });
            txtShareKey.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    Log.d("debug", "txtShareKey setOnPreferenceClickListener");
                    txtShareKey.setText(UserLogin.google2FAsecret);
                    ClipData clip = ClipData.newPlainText("Shared Key",UserLogin.google2FAsecret);
                    return false;
                }
            });

            // Bind the summaries of EditText/List/Dialog/Ringtone preferences
            // to their values. When their values change, their summaries are
            // updated to reflect the new value, per the Android Design
            // guidelines.
            //bindPreferenceSummaryToValue(findPreference("googleotp_switch"));
            //bindPreferenceSummaryToValue(findPreference("fingerprint_switch"));
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            Log.d("debug", "General id Click " + id);
            if (id == android.R.id.home) {
                startActivity(new Intent(getActivity(), SettingsActivity.class));
                return true;
            }
            return super.onOptionsItemSelected(item);
        }

        private void EnableGoogleOTPTask(Boolean isDataTrafficOn){
            isGoogleOTPenabled=isDataTrafficOn?"true":"false";
            AsyncTask<Void, Void, Boolean> task = new AsyncTask<Void, Void, Boolean>() {

                @Override
                protected void onPreExecute() {
                    super.onPreExecute();
                    Log.e("debug", "onPreExecute");
                }

                @Override
                protected Boolean doInBackground(Void... params) {
                    // TODO: attempt authentication against a network service.
                    Log.d("debug", "Start background Task");
                    Response result=new Response();
                    String url = PathConfiguration.GENERATESECRETKEYS+ "?email=" + UserLogin.email + "&enable="+isGoogleOTPenabled;
                    Log.d("debug", "url = " + url);
                    API task=new API();
                    try {
                        result = task.DownloadContent(url);

                        if (result.Code==200){
                            JSONObject jsonObj = new JSONObject(result.Response);
                            UserLogin.google2FAsecret = jsonObj.getString("secretkey");
                            UserLogin.enable = jsonObj.getInt("isEnabled")==1;
                            UserLogin.QrCodeUrl = jsonObj.getString("QrCodeUrl");
                            response=result;
                            Log.d("debug", "Success GENERATESECRETKEYS");
                        }else{
                            Log.d("debug", "GENERATESECRETKEYS Failed");
                            return false;
                        }
                    } catch (IOException IOEx){
                        Log.d("debug", "IOException = " + IOEx);
                        return false;
                    } catch (JSONException e){
                        Log.d("debug", "JSONException = " + e);
                        return false;
                    }
                    // TODO: register the new account here.
                    return true;
                }

                @Override
                protected void onPostExecute(final Boolean success) {
                    Log.d("debug", "on Post Execute");

                    if (success) {
                        if (UserLogin.enable) {
                            Toast.makeText(getActivity(), "Google Authenticator Enabled",
                                    Toast.LENGTH_LONG).show();
                            txtShareKey.setText(UserLogin.google2FAsecret);
                        }else {
                            Toast.makeText(getActivity(), "Google Authenticator Disabled",
                                    Toast.LENGTH_LONG).show();
                        }
                        txtShareKey.setEnabled(UserLogin.enable);
                    }else{
                        Toast.makeText(getActivity(), "Failed Connection",
                                Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                protected void onCancelled() {
                    Log.d("debug", "On canceled");
                }
            };

            if(Build.VERSION.SDK_INT >= 11)
                task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            else
                task.execute();
        }

        private void EnableFingerPrintTask(Boolean isDataTrafficOn){
            isFingerEnabled=isDataTrafficOn?"true":"false";

            AsyncTask<Void, Void, Boolean> task = new AsyncTask<Void, Void, Boolean>() {

                @Override
                protected void onPreExecute() {
                    super.onPreExecute();
                    Log.e("debug", "onPreExecute");
                }

                @Override
                protected Boolean doInBackground(Void... params) {
                    // TODO: attempt authentication against a network service.
                    Log.d("debug", "Start background Task");
                    Response result=new Response();
                    String url = PathConfiguration.ENABLEFINGERPRINT+ "?email=" + UserLogin.email + "&enable="+isFingerEnabled +"&deviceid="+UserLogin.deviceId;
                    Log.d("debug", "url = " + url);
                    API task=new API();
                    try {
                        result = task.DownloadContent(url);

                        if (result.Code==200){
                            response=result;
                            JSONObject jsonObj = new JSONObject(result.Response);
                            UserLogin.isFingerPrintEnabled = jsonObj.getInt("isFingerPrintEnabled")==1;
                            Log.d("debug", "Success ENABLEFINGERPRINT");
                        }else{
                            Log.d("debug", "ENABLEFINGERPRINT Failed");
                            return false;
                        }
                    } catch (IOException IOEx){
                        Log.d("debug", "IOException = " + IOEx);
                        return false;
                    } catch (JSONException e){
                        Log.d("debug", "JSONException = " + e);
                        return false;
                    }
                    // TODO: register the new account here.
                    return true;
                }

                @Override
                protected void onPostExecute(final Boolean success) {
                    Log.d("debug", "on Post Execute");

                    SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    if (success) {
                        if (UserLogin.isFingerPrintEnabled) {
                            editor.putString(StatisGlobal.email, UserLogin.email);
                            editor.putInt(StatisGlobal.userid, UserLogin.userId);
                            editor.putString(StatisGlobal.password, UserLogin.password);
                            editor.putString(StatisGlobal.device_ID, UserLogin.deviceId);
                            Toast.makeText(getActivity(), "Fingerprint is enabled",
                                    Toast.LENGTH_LONG).show();

                        }else {
                            editor.putString(StatisGlobal.email, "");
                            editor.putInt(StatisGlobal.userid, 0);
                            editor.putString(StatisGlobal.password,"");
                            editor.putString(StatisGlobal.device_ID, "");
                            Toast.makeText(getActivity(), "Fingerprint is disabled",
                                    Toast.LENGTH_LONG).show();
                        }
                    }else{
                        editor.putString(StatisGlobal.email, "");
                        editor.putInt(StatisGlobal.userid, 0);
                        editor.putString(StatisGlobal.password,"");
                        editor.putString(StatisGlobal.device_ID, "");
                        Toast.makeText(getActivity(), "Failed Connection",
                                Toast.LENGTH_LONG).show();
                    }
                    editor.commit();
                }

                @Override
                protected void onCancelled() {
                    Log.d("debug", "On canceled");
                }
            };

            if(Build.VERSION.SDK_INT >= 11)
                task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            else
                task.execute();
        }
    }

    /**
     * This fragment shows notification preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class NotificationPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            Log.d("debug", "NotificationPreferenceFragment onCreate ");
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_notification);
            setHasOptionsMenu(true);

            // Bind the summaries of EditText/List/Dialog/Ringtone preferences
            // to their values. When their values change, their summaries are
            // updated to reflect the new value, per the Android Design
            // guidelines.
            bindPreferenceSummaryToValue(findPreference("notifications_new_message_ringtone"));
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            Log.d("debug", "NotificationPreferenceFragment onOptionsItemSelected ");
            int id = item.getItemId();
            if (id == android.R.id.home) {
                startActivity(new Intent(getActivity(), SettingsActivity.class));
                return true;
            }
            return super.onOptionsItemSelected(item);
        }
    }

    /**
     * This fragment shows data and sync preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class DataSyncPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            Log.d("debug", "DataSyncPreferenceFragment onCreate ");
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_data_sync);
            setHasOptionsMenu(true);

            // Bind the summaries of EditText/List/Dialog/Ringtone preferences
            // to their values. When their values change, their summaries are
            // updated to reflect the new value, per the Android Design
            // guidelines.
            bindPreferenceSummaryToValue(findPreference("sync_frequency"));
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            Log.d("debug", "DataSyncPreferenceFragment onOptionsItemSelected ");
            int id = item.getItemId();
            if (id == android.R.id.home) {
                startActivity(new Intent(getActivity(), SettingsActivity.class));
                return true;
            }
            return super.onOptionsItemSelected(item);
        }
    }
}
