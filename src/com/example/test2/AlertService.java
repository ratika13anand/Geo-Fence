package com.example.test2;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;
import com.google.android.gms.maps.model.LatLng;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

@SuppressLint("NewApi")
public class AlertService extends Service {

	private static LatLng lat = null;
	private static LatLng lng = null;
	private NotificationManager mNM;

    // Unique Identification Number for the Notification.
    // We use it on Notification start, and to cancel it.
    private int NOTIFICATION = 1;

    public class LocalBinder extends Binder {
        AlertService getService() {
            return AlertService.this;
        }
    }
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
		Toast.makeText(getApplicationContext(), "in service start",
				Toast.LENGTH_SHORT).show();

		pingServerAtIntervals();
		return START_STICKY;
	}

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		Toast.makeText(getApplicationContext(), "in service create",
				Toast.LENGTH_SHORT).show();

		// start a separate thread and start listening to your network object
	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

	protected void pingServerAtIntervals() {

		//String curloc = lat.toString() + "," + lng.toString();
		String curloc = "34.04258109391567,-118.2993968948721";
		try {
			URL url = new URL(
					"http://csci587team7.cloudapp.net:8080/587Service/rest/overlapfence/currentloc/"+curloc);
			new getOverlapData().execute(url);
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
				Toast.makeText(getApplicationContext(), "Intersect Found"+res, Toast.LENGTH_SHORT).show();
				mNM = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
		        // Display a notification about us starting.  We put an icon in the status bar.
		        showNotification(res);
			}
			else
				Toast.makeText(getApplicationContext(), "No Intersect Found", Toast.LENGTH_SHORT).show();
		}
	}
	
	 private void showNotification(String res) {
	        // In this sample, we'll use the same text for the ticker and the expanded notification
	        CharSequence text = "Geo-Fences(s) found less than 20 meters away. View Details before proceeding.";

	        // Set the icon, scrolling text and timestamp
	        Notification notification = new Notification(R.drawable.test, text,
	                System.currentTimeMillis());

	        Intent newIntent = new Intent(this, OverlapActivity.class);

		    newIntent.putExtra("xml", res);
		    newIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP);
	        // The PendingIntent to launch our activity if the user selects this notification
	        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
	               newIntent, 0);
	        // Set the info for the views that show in the notification panel.
	        notification.setLatestEventInfo(this, "Geo-Fence Alert!",
	                       text, contentIntent);

	        // Send the notification.
	        mNM.notify(NOTIFICATION, notification);
	    }


}