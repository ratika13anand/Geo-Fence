package com.example.test2;

	import android.support.v7.app.ActionBarActivity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.Console;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

	import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter;

	import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
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
import android.content.Intent;
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
import android.widget.TextView;
import android.widget.Toast;

	public class OverlapActivity extends FragmentActivity implements
	GooglePlayServicesClient.ConnectionCallbacks,
	GooglePlayServicesClient.OnConnectionFailedListener, LocationListener,
	OnMapClickListener, OnMapLongClickListener, OnMarkerClickListener {

		// Google Map
		private static GoogleMap omap = null;
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
		private String xml = new String();
		
		
		
		@Override
		protected void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			setContentView(R.layout.activity_overlap);
			//Toast.makeText(getApplicationContext(), "in create overlap",Toast.LENGTH_SHORT).show();
			xml = getIntent().getStringExtra("xml");
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
					.findFragmentById(R.id.mapO);
			omap = fm.getMap(); // display zoom map

			omap.setMyLocationEnabled(true);
			omap.setOnMapClickListener(this);
			omap.setOnMapLongClickListener(this);
			omap.setOnMarkerClickListener(this);
			omap.setInfoWindowAdapter(new InfoWindowAdapter() {
				 
	            // Use default InfoWindow frame
	            @Override
	            public View getInfoWindow(Marker marker) {
	            	return null;                 
	            }
	 
	            // Defines the contents of the InfoWindow
	            @Override
	            public View getInfoContents(Marker marker) {
	             	
	             	 // Getting view from the layout file info_window_layout
	                View v = getLayoutInflater().inflate(R.layout.info_window_layout, null);
	 
	                // Getting the position from the marker
	                String snippet = marker.getSnippet();
	                String title = marker.getTitle();
	                // Getting reference to the TextView to set longitude
	                TextView titleV = (TextView) v.findViewById(R.id.titleV);
	                
	                // Setting the latitude
	                titleV.setText(title);
	                TextView snippetV = (TextView) v.findViewById(R.id.snippetV);
	 
	                // Setting the latitude
	                snippetV.setText(snippet);
	  
	                // Returning the view containing InfoWindow contents
	                return v;
	            }
	        });
			
			try {
				// Loading map
				initilizeMap();

			} catch (Exception e) {
				e.printStackTrace();
			}			
		}

		/**
		 * function to load map. If map is not created it will create it for you
		 * */
		@SuppressLint("NewApi")
		private void initilizeMap() {
			if (omap == null) {
				omap = ((MapFragment) getFragmentManager().findFragmentById(
						R.id.mapO)).getMap();

				// check if map is created successfully or not
				if (omap == null) {
					Toast.makeText(getApplicationContext(),
							"Sorry! unable to create maps", Toast.LENGTH_SHORT)
							.show();
				} else {
					omap.setMyLocationEnabled(true);
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
			xml = getIntent().getStringExtra("xml");
			//Toast.makeText(getApplicationContext(), "in start overlap",Toast.LENGTH_SHORT).show();
			getFenceDisplay(xml);
		}
		
		@Override
		protected void onNewIntent(Intent intent) {
			
			xml = intent.getStringExtra("xml");
			mLocationClient.connect();
			//Toast.makeText(getApplicationContext(), "in on new overlap",Toast.LENGTH_SHORT).show();
			getFenceDisplay(xml);
			
		}
		
		protected void getFenceDisplay(String xml)
		{

			XmlPullParserFactory xmlFactoryObject;
			try {
				xmlFactoryObject = XmlPullParserFactory.newInstance();
			
			XmlPullParser myParser = xmlFactoryObject.newPullParser();
			myParser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
			InputStream stream = new ByteArrayInputStream(xml.getBytes("UTF-8"));
		    myParser.setInput(stream, null);
		    parseXMLAndStoreIt(myParser);
		    displayData();
		    stream.close();
			} catch (XmlPullParserException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
		@Override
		protected void onStop() {
			// If the client is connected
			if (mLocationClient.isConnected()) {

			}

			mLocationClient.disconnect();
			super.onStop();
		}
		
		@Override
		protected void onResume() {
			super.onResume();
			mLocationClient.connect();
			initilizeMap();
			//Toast.makeText(getApplicationContext(), "in resume overlap", Toast.LENGTH_SHORT).show();
			if (mPrefs.contains("KEY_UPDATES_ON")) {
				mUpdatesRequested = mPrefs.getBoolean("KEY_UPDATES_ON", false);

				// Otherwise, turn off location updates
			} else {
				mEditor.putBoolean("KEY_UPDATES_ON", false);
				mEditor.commit();
			}
			xml = getIntent().getStringExtra("xml");
			getFenceDisplay(xml);
		}
		
		@Override
		public void onLocationChanged(Location location) {
			// Report to the UI that the location was updated
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
				startActivityForResult(i, 1);
	        }
			return super.onOptionsItemSelected(item);
		}

		@Override
		public boolean onMarkerClick(Marker marker) {
			// TODO Auto-generated method stub
			if (marker.isInfoWindowShown())
			    marker.hideInfoWindow();
			else
				marker.showInfoWindow();
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
			    omap.animateCamera(cameraUpdate);
		}

		@Override
		public void onDisconnected() {
					Toast.makeText(this, "Disconnected. Please re-connect.",
							Toast.LENGTH_SHORT).show();
			
		}
		
		public void parseXMLAndStoreIt(XmlPullParser myParser) {

			fenceList.clear();
			int event;
			String text= null;
			
			String fenceId = new String();
			String expiry = new String();
			String coord = new String();
			String validity = new String();
			String info = new String();
			String sLevel = new String();
			String distance = new String();
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
						else if(name.equals("distance")){ 	
							distance = text;
						}
						else if(name.equals("fence")){
							FenceObj obj = new FenceObj(fenceId,expiry,coord,validity,info,sLevel,distance);
							fenceList.add(obj);
							fenceId =null;expiry=null;coord=null;validity=null;info=null;sLevel=null;distance=null;
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
	        omap.clear();
			//Toast.makeText(getApplicationContext(), "list size"+fenceList.size(), Toast.LENGTH_SHORT).show();
			for (int i = 0; i < fenceList.size(); i++) {
				//Toast.makeText(this,fenceList.get(i).toString(),Toast.LENGTH_LONG).show();
				System.out.println(fenceList.get(i).toString());
				fenceList.get(i).addPoly(omap);
			}
		}
	}
		
