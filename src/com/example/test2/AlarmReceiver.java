package com.example.test2;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.model.LatLng;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import android.support.v4.app.NotificationCompat;

public class AlarmReceiver extends BroadcastReceiver implements
GooglePlayServicesClient.ConnectionCallbacks,
GooglePlayServicesClient.OnConnectionFailedListener, LocationListener
{   

	private static Context sContext = null;
	   private int NOTIFICATION = 1;
	   LocationRequest mLocationRequest;
		LocationClient mLocationClient;
		boolean mUpdatesRequested;
		SharedPreferences mPrefs;
		Editor mEditor;

		private static String lat;
		private static String lng;
		private NotificationManager mNM;
		private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
		// Milliseconds per second
		private static final int MILLISECONDS_PER_SECOND = 100;
		// Update frequency in seconds
		public static final int UPDATE_INTERVAL_IN_SECONDS = 1;
		// Update frequency in milliseconds
		private static final long UPDATE_INTERVAL = MILLISECONDS_PER_SECOND
				* UPDATE_INTERVAL_IN_SECONDS;
		// The fastest update frequency, in seconds
		private static final int FASTEST_INTERVAL_IN_SECONDS = 1;
		// A fast frequency ceiling in milliseconds
		private static final long FASTEST_INTERVAL = MILLISECONDS_PER_SECOND
				* FASTEST_INTERVAL_IN_SECONDS;
@Override
public void onReceive(Context context, Intent intent) {
	// TODO Auto-generated method stub
	sContext = context;
	setLocation();
	Toast.makeText(context, "Repeating", Toast.LENGTH_SHORT).show();
	pingServerAtIntervals();
}

protected void setLocation()
{
	mLocationRequest = LocationRequest.create();
	// Use high accuracy
	mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
	// Set the update interval to 5 seconds
	mLocationRequest.setInterval(UPDATE_INTERVAL);
	// Set the fastest update interval to 1 second
	mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
	/*
	 * Create a new location client, using the enclosing class to handle
	 * callbacks.
	 */
	mLocationClient = new LocationClient(sContext, this, this);
	mUpdatesRequested =true;
	mLocationClient.connect();
}
protected void pingServerAtIntervals() {

	//String curloc = lat.toString() + "," + lng.toString();
	String curloc = new String();
	if(lat!=null && lng!=null)
	{
		curloc = lat + "," + lng;
		try {
			URL url = new URL(
					"http://csci587team7.cloudapp.net:8080/587Service/rest/overlapfence/currentloc/"+curloc);
			new getOverlapData().execute(url);
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	else
	{
		
    // do nothing asclientisnt connected yet. Will be connected in next 60 secs 
	//curloc = "34.04258109391567,-118.2993968948721";
	}
	

}

private static String getStringFromInputStream(InputStream is) {
	 
	BufferedReader br = null;
	StringBuilder sb = new StringBuilder();

	String line;
	try {

		br = new BufferedReader(new InputStreamReader(is));
		while ((line = br.readLine()) != null) {
			sb.append(line);
		}

	} catch (IOException e) {
		e.printStackTrace();
	} finally {
		if (br != null) {
			try {
				br.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	return sb.toString();

}
private class getOverlapData extends AsyncTask<URL, Integer, String> {
	protected String doInBackground(URL... urls) {
	try {

			HttpURLConnection conn = (HttpURLConnection) urls[0]
					.openConnection();
			conn.setRequestMethod("GET");
			conn.setDoInput(true);
			conn.connect();
			InputStream stream = conn.getInputStream();
			String result = getStringFromInputStream(stream);
		    
		    if(!result.equals("<fences></fences>"))
		    {
		    return result;	
		    }
			

		} catch (Exception e) {
			Log.d("Exception", e.toString());
		}
		return null;
	}

	@SuppressWarnings("deprecation")
	protected void onPostExecute(String res) {
		if(res!=null)
		{
			//Toast.makeText(sContext, "Intersect Found"+res, Toast.LENGTH_SHORT).show();
			mNM = (NotificationManager)sContext.getSystemService("notification");
	        // Display a notification about us starting.  We put an icon in the status bar.
	        showNotification(res);
		}
		//else
			//Toast.makeText(sContext, "No Intersect Found", Toast.LENGTH_SHORT).show();
	}
}

 private void showNotification(String res) {
        // In this sample, we'll use the same text for the ticker and the expanded notification
        CharSequence text = "Geo-Fences(s) found less than 20 meters away. View Details before proceeding.";

        // Set the icon, scrolling text and timestamp
        Notification notification = new Notification(R.drawable.test, text,
                System.currentTimeMillis());

        Intent newIntent = new Intent(sContext, OverlapActivity.class);
        notification.defaults |= Notification.DEFAULT_SOUND;
        notification.defaults |= Notification.DEFAULT_VIBRATE;
	    newIntent.putExtra("xml", res);
	    newIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        // The PendingIntent to launch our activity if the user selects this notification
        PendingIntent contentIntent = PendingIntent.getActivity(sContext, 0,
               newIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        
        // Set the info for the views that show in the notification panel.
        notification.setLatestEventInfo(sContext, "Geo-Fence Alert!",
                       text, contentIntent);

        // Send the notification.
        mNM.notify(NOTIFICATION, notification);
    }

@Override
public void onLocationChanged(Location arg0) {
	// TODO Auto-generated method stub
	
}

@Override
public void onConnectionFailed(ConnectionResult connectionResult) {
	// TODO Auto-generated method stub
	
}

@Override
public void onConnected(Bundle arg0) {
	// TODO Auto-generated method stub
	mLocationClient.requestLocationUpdates(mLocationRequest, this);
	Location location = mLocationClient.getLastLocation();
    lat = String.valueOf(location.getLatitude());
    lng = String.valueOf(location.getLongitude());	
}

@Override
public void onDisconnected() {
	// TODO Auto-generated method stub
	
}


}