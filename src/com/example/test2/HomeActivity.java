package com.example.test2;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.RadioButton;
import android.widget.Toast;

public class HomeActivity extends Activity {

	//private static Button create, display;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_home);
		
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
	
	public void onEnableAlerts(View view)
	{
		boolean checked = ((RadioButton)view).isChecked();
	    
	    // Check which radio button was clicked
	    switch(view.getId()) {
	        case R.id.enableA:
	            if (checked)
	            {   Toast.makeText(getApplicationContext(), "Enable", Toast.LENGTH_SHORT).show();
	            AlarmManager alarmManager=(AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
	            Intent intent = new Intent(getApplicationContext(), AlarmReceiver.class);
	            PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
	            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP,System.currentTimeMillis(),60000,
	                                                                                  pendingIntent);
	               /* Intent serviceIntent = new Intent(this,AlertService.class);
	            	startService(serviceIntent);	*/            	
	            }
	           
	            break;
	        case R.id.disableA:
	            if (checked)
	            {
	            	Toast.makeText(getApplicationContext(), "Disable", Toast.LENGTH_SHORT).show();
	            	AlarmManager alarmManager=(AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
	            	Intent intent = new Intent(getApplicationContext(), AlarmReceiver.class);
	            	PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, intent, 0);
	            	alarmManager.cancel(pendingIntent);
	            }
	          
	            break;
	    }
		
	}
}

