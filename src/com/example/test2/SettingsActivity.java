package com.example.test2;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.PreferenceActivity;

public class SettingsActivity extends PreferenceActivity implements
OnSharedPreferenceChangeListener {

	public static final String KEY_PREF_ALERT = "AlertOn";
	public static final String KEY_PREF_PING = "PingFrequency";
	public static final String KEY_PREF_VIBRATE = "Vibrate";
	public static final String KEY_PREF_RING = "Ring";
	@SuppressWarnings("deprecation")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		//setContentView(R.layout.activity_settings);
		addPreferencesFromResource(R.xml.settings);
		
	}
	
	@SuppressWarnings("deprecation")
	@Override
	protected void onResume() {
	    super.onResume();
	    // Set up a listener whenever a key changes
	    getPreferenceScreen().getSharedPreferences()
	            .registerOnSharedPreferenceChangeListener(this);
	}

	@SuppressWarnings("deprecation")
	@Override
	protected void onPause() {
	    super.onPause();
	    // Unregister the listener whenever a key changes
	    getPreferenceScreen().getSharedPreferences()
	            .unregisterOnSharedPreferenceChangeListener(this);
	}
	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		if (key.equals(KEY_PREF_ALERT)) {
			if( sharedPreferences.getBoolean(key,true))
				{
				// Alert On = true
				AlarmManager alarmManager=(AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
	            Intent intent = new Intent(getApplicationContext(), AlarmReceiver.class);
	            PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
	            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP,System.currentTimeMillis(),60000,pendingIntent);
				}
			else
			{
				AlarmManager alarmManager=(AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
            	Intent intent = new Intent(getApplicationContext(), AlarmReceiver.class);
            	PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            	alarmManager.cancel(pendingIntent);
			}
			// TODO Auto-generated method stub
		
		}

		else if (key.equals(KEY_PREF_PING)) {
			//Toast.makeText(getApplicationContext(), "Ping changed "+sharedPreferences.getString(key,"60000"), Toast.LENGTH_SHORT).show();
			AlarmManager alarmManager=(AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
        	Intent intent = new Intent(getApplicationContext(), AlarmReceiver.class);
        	PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        	alarmManager.cancel(pendingIntent);
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP,System.currentTimeMillis(),
            		Long.parseLong(sharedPreferences.getString(key,"60000")),pendingIntent);
		}
	}

	
}
