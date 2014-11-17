package com.example.test2;

import android.support.v7.app.ActionBarActivity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import java.io.BufferedReader;
import java.io.Console;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import android.support.v4.app.FragmentActivity;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.graphics.Point;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;

public class DisplayActivity extends FragmentActivity implements
GooglePlayServicesClient.ConnectionCallbacks,
GooglePlayServicesClient.OnConnectionFailedListener, LocationListener,
OnMapClickListener, OnMapLongClickListener, OnMarkerClickListener {

	// Google Map
	private static GoogleMap map = null;
	private ArrayList<LatLng> arrayPoints = new ArrayList<LatLng>();
	PolylineOptions polylineOptions;
	private static boolean checkClick = false;
	private static MenuItem mCreate;
	private static MenuItem mCancel;
    private static ArrayList<FenceObj> fenceList = new ArrayList<FenceObj>();
    public static final String BASE_URI = "http://csci587team7.cloudapp.net:8080/";
	
	public static final String PATH_NAME = "/fence/display";
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


	// Define an object that holds accuracy and frequency parameters
	LocationRequest mLocationRequest;
	LocationClient mLocationClient;
	boolean mUpdatesRequested;
	SharedPreferences mPrefs;
	Editor mEditor;
	
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_display);
		
		String UID = getIntent().getStringExtra("UID");
		//Toast.makeText(getApplicationContext(), UID, Toast.LENGTH_LONG).show();
		mLocationRequest = LocationRequest.create();
		// Use high accuracy
		mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
		// Set the update interval to 5 seconds
		mLocationRequest.setInterval(UPDATE_INTERVAL);
		// Set the fastest update interval to 1 second
		mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
		// Open the shared preferences
		mPrefs = getSharedPreferences("SharedPreferences", Context.MODE_PRIVATE);
		// Get a SharedPreferences editor
		mEditor = mPrefs.edit();
		/*
		 * Create a new location client, using the enclosing class to handle
		 * callbacks.
		 */
		mLocationClient = new LocationClient(this, this, this);
		// Start with updates turned off
		mUpdatesRequested = false;
		//arrayPoints = new ArrayList<LatLng>();
		SupportMapFragment fm = (SupportMapFragment) getSupportFragmentManager()
				.findFragmentById(R.id.mapD);
		map = fm.getMap(); // display zoom map

		map.setMyLocationEnabled(true);
		map.setOnMapClickListener(this);
		map.setOnMapLongClickListener(this);
		map.setOnMarkerClickListener(this);
		
		try {
			// Loading map
			initilizeMap();

		} catch (Exception e) {
			e.printStackTrace();
		}
		
		//getFenceData(UID);
		/*URL url;
		try {
			url = new URL("http://csci587team7.cloudapp.net:8080/587Service/rest/fence");
			new getFenceData().execute(url);
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
		
	}

	/**
	 * function to load map. If map is not created it will create it for you
	 * */
	@SuppressLint("NewApi")
	private void initilizeMap() {
		if (map == null) {
			map = ((MapFragment) getFragmentManager().findFragmentById(
					R.id.mapD)).getMap();

			// check if map is created successfully or not
			if (map == null) {
				Toast.makeText(getApplicationContext(),
						"Sorry! unable to create maps", Toast.LENGTH_SHORT)
						.show();
			} else {
				map.setMyLocationEnabled(true);
			}
		}
	}

		@Override
	protected void onPause() {
		// Save the current setting for updates
		mEditor.putBoolean("KEY_UPDATES_ON", mUpdatesRequested);
		mEditor.commit();
		super.onPause();
		
	}

	@Override
	protected void onStart() {
		super.onStart();
		mLocationClient.connect();
		URL url;
		try {
			arrayPoints.clear();
			map.clear();
			url = new URL("http://csci587team7.cloudapp.net:8080/587Service/rest/fence");
			new getFenceData().execute(url);
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	@Override
	protected void onStop() {
		// If the client is connected
		if (mLocationClient.isConnected()) {
		
			// removeLocationUpdates(this);
		}
		/*
		 * After disconnect() is called, the client is considered "dead".
		 */
		mLocationClient.disconnect();
		super.onStop();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		if (mPrefs.contains("KEY_UPDATES_ON")) {
			mUpdatesRequested = mPrefs.getBoolean("KEY_UPDATES_ON", false);

			// Otherwise, turn off location updates
		} else {
			mEditor.putBoolean("KEY_UPDATES_ON", false);
			mEditor.commit();
		}
		URL url;
		try {
			arrayPoints.clear();
			map.clear();
			url = new URL("http://csci587team7.cloudapp.net:8080/587Service/rest/fence");
			new getFenceData().execute(url);
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Override
	public void onLocationChanged(Location location) {
		// Report to the UI that the location was updated
		String msg = "Updated Location: "
				+ Double.toString(location.getLatitude()) + ","
				+ Double.toString(location.getLongitude());
		Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.display, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public boolean onMarkerClick(Marker arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void onMapLongClick(LatLng arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onMapClick(LatLng arg0) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void onConnectionFailed(ConnectionResult connectionResult) {
			if (connectionResult.hasResolution()) {
			try {
				// Start an Activity that tries to resolve the error
				connectionResult.startResolutionForResult(this,
						CONNECTION_FAILURE_RESOLUTION_REQUEST);

			} catch (IntentSender.SendIntentException e) {
				// Log the error
				e.printStackTrace();
			}
		} 		
	}

	@Override
	public void onConnected(Bundle arg0) {
		// TODO Auto-generated method stub

		if (mUpdatesRequested) {
			mLocationClient.requestLocationUpdates(mLocationRequest, this);
		}
		 Location location = mLocationClient.getLastLocation();
		    LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
		    CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 15);
		    map.animateCamera(cameraUpdate);
	}

	@Override
	public void onDisconnected() {
				Toast.makeText(this, "Disconnected. Please re-connect.",
						Toast.LENGTH_SHORT).show();
		
	}
	
	private class getFenceData extends AsyncTask<URL, Integer, String> {
	     protected String doInBackground(URL... urls) {
	
		//Make a call to servlet to get fence data in XML and parse the xml to get each fence
		
				try {
					
					//URL url = new URL("http://csci587team7.cloudapp.net:8080/587Service/rest/fence");
					
		               HttpURLConnection conn = (HttpURLConnection)urls[0].openConnection();
		                  conn.setReadTimeout(10000 /* milliseconds */);
		                  conn.setConnectTimeout(15000 /* milliseconds */);
		                  conn.setRequestMethod("GET");
		                  conn.setDoInput(true);
		                  conn.connect();
		            InputStream stream = conn.getInputStream();
		            
					//String xmlData = "<fences><fence><fenceid>3</fenceid><coordinates>226.0,150.0;254.0,164.0;240.0,191.0;212.0,176.0;226.0,150.0;</coordinates><expiry>2014-10-22</expiry><validity>1</validity><info>Road closed due to accident.</info><security_level>1</security_level></fence></fences>";
					XmlPullParserFactory xmlFactoryObject = XmlPullParserFactory.newInstance();
					XmlPullParser myParser = xmlFactoryObject.newPullParser();
					myParser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
				    myParser.setInput(stream, null);
				    parseXMLAndStoreIt(myParser);
				   
		            stream.close();
		            
				} catch (Exception e) {
					Log.d("Exception", e.toString());
				}
				return null;

			}
	     protected void onPostExecute(String res) {
	    	 displayData();
	     }
		}

	
	public void parseXMLAndStoreIt(XmlPullParser myParser) {

		int event;
		String text= null;
		
		String fenceId = new String();
		String expiry = new String();
		String coord = new String();
		String validity = new String();
		String info = new String();
		String sLevel = new String();
		try {
			event = myParser.getEventType();
			while (event != XmlPullParser.END_DOCUMENT) {
				String name=myParser.getName();
				switch (event){
				case XmlPullParser.START_TAG:
					break;
				case XmlPullParser.TEXT:
					text = myParser.getText();
					break;
				case XmlPullParser.END_TAG:
					if(name.equals("fenceid")){
						fenceId = text;
					}
					else if(name.equals("expiry")){ 	
						expiry = text;
					}
					else if(name.equals("coordinates")){ 	
						coord = text;
					}
					else if(name.equals("validity")){ 	
						validity = text;
					}
					else if(name.equals("info")){ 	
						info = text;
					}
					else if(name.equals("security_level")){ 	
						sLevel = text;
					}
					else if(name.equals("fence")){
						FenceObj obj = new FenceObj(fenceId,expiry,coord,validity,info,sLevel);
						fenceList.add(obj);
						fenceId =null;expiry=null;coord=null;validity=null;info=null;sLevel=null;
					}
					break;
				}		 
				event = myParser.next(); 
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	public void displayData() {

		//fenceList.get(0).addPolygon(map);
		for (int i = 0; i < fenceList.size(); i++) {
			//Toast.makeText(this,fenceList.get(i).toString(),Toast.LENGTH_LONG).show();
			System.out.println(fenceList.get(i).toString());
			fenceList.get(i).addPolygon(map);
		}
	}
}
	
