package com.example.test2;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

public class HomeActivity extends Activity {

	//private static Button create, display;

	private static final int RESULT_SETTINGS = 1;
	public static final String KEY_PREF_ALERT = "AlertOn";
	public static final String KEY_PREF_PING = "PingFrequency";
	public static final String KEY_PREF_VIBRATE = "Vibrate";
	public static final String KEY_PREF_RING = "Ring";

    public static final String PREFS_LOGIN_USERNAME_KEY = "USERNAME" ;
    public static final String PREFS_LOGIN_PASSWORD_KEY = "PASSWORD" ;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_home);
		String status = getIntent().getStringExtra("lstatus");
		String uname = new String();
		if(status.equals("valid"))
		{
		uname = getIntent().getStringExtra("UID");
		TextView foot = (TextView)findViewById(R.id.logged);
		foot.setText("Signed in as "+uname);
		
		}		
	}	
	public void createF(View view)
	{
		Intent intent = new Intent(this, MainActivity.class);
		String user_id = "test";   // Need to Use GoogleAuthUtil.getToken() method here
	    intent.putExtra("UID", user_id);
		startActivity(intent);
	}	
	public void displayF(View view)
	{
		Intent intent = new Intent(this, DisplayActivity.class);
		String user_id = "test";  // Need to Use GoogleAuthUtil.getToken() method here
	    intent.putExtra("UID", user_id);
		startActivity(intent);
	}
	public void deleteF(View view)
	{
		Intent intent = new Intent(this, DeleteActivity.class);
		String user_id = "test";  // Need to Use GoogleAuthUtil.getToken() method here
	    intent.putExtra("UID", user_id);
		startActivity(intent);
	}
	public void logoutF(View view)
	{
		PrefUtils.saveToPrefs(this, PREFS_LOGIN_USERNAME_KEY, null);
		PrefUtils.saveToPrefs(this, PREFS_LOGIN_PASSWORD_KEY, null);
		Intent intent = new Intent(this, Login.class);
		startActivity(intent);
	}	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.overlap, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if(id == R.id.action_settings)
		{
			Intent i = new Intent(this, SettingsActivity.class);
			startActivityForResult(i, RESULT_SETTINGS);
        
			SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
			String vibrate = "hey"+sharedPref.getBoolean(SettingsActivity.KEY_PREF_VIBRATE, false);
			String sound = "hey"+sharedPref.getBoolean(SettingsActivity.KEY_PREF_RING, false);
			
			Toast.makeText(getApplicationContext(), "HOME vibrate is "+vibrate, Toast.LENGTH_SHORT).show();
			Toast.makeText(getApplicationContext(), "FOME sound is "+sound, Toast.LENGTH_SHORT).show();
		}
		return true;
	}
	
}

