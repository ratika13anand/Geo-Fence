package com.example.test2;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter;

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
import com.google.android.gms.maps.model.PolylineOptions;

import android.support.v4.app.FragmentActivity;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.location.Location;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

public class DisplayActivity extends FragmentActivity implements
GooglePlayServicesClient.ConnectionCallbacks,
GooglePlayServicesClient.OnConnectionFailedListener, LocationListener,
OnMapClickListener, OnMapLongClickListener, OnMarkerClickListener {

	// Google Map
	private static GoogleMap map = null;
	private ArrayList<LatLng> arrayPoints = new ArrayList<LatLng>();
	PolylineOptions polylineOptions;
	private static boolean checkpoint = false;
	private static boolean clickpoint = false;
	
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
	private static Menu sMenu ;
	
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_display);
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
		SupportMapFragment fm = (SupportMapFragment) getSupportFragmentManager()
				.findFragmentById(R.id.mapD);
		map = fm.getMap(); // display zoom map

		map.setMyLocationEnabled(true);
		map.setOnMapClickListener(this);
		map.setOnMapLongClickListener(this);
		map.setOnMarkerClickListener(this);
		map.setInfoWindowAdapter(new InfoWindowAdapter() {
			 
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
		
		getFenceDisplay();
		
	}
	
	protected void getFenceDisplay()
	{
		
		mLocationClient.connect();
		URL url;
		try {
			arrayPoints.clear();
			fenceList.clear();
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

		//Toast.makeText(getApplicationContext(), "in resume", Toast.LENGTH_SHORT).show();
		if (mPrefs.contains("KEY_UPDATES_ON")) {
			mUpdatesRequested = mPrefs.getBoolean("KEY_UPDATES_ON", false);

			// Otherwise, turn off location updates
		} else {
			mEditor.putBoolean("KEY_UPDATES_ON", false);
			mEditor.commit();
		}
		if(sMenu!=null && sMenu.getItem(1).isChecked())
		getFenceDisplay();
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
		sMenu = menu; 
		sMenu.getItem(1).setCheckable(true);
		sMenu.getItem(1).setChecked(true);
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
		if (id == R.id.refresh) {
			item.setCheckable(true); 
			map.clear();
			checkpoint =false;
			clickpoint=false;
	 		sMenu.getItem(2).setChecked(false); 
	 		sMenu.getItem(3).setChecked(false); 
	 		if(!item.isChecked()) 
	 		item.setChecked(true);
	 		Toast.makeText(getApplicationContext(), "Fetching Fences", 
						Toast.LENGTH_LONG).show();
			getFenceDisplay();
		}
		if (id == R.id.point) { 
	 		item.setCheckable(true); 
	 		sMenu.getItem(1).setChecked(false); 
	 		sMenu.getItem(3).setChecked(false); 
	 		if(!item.isChecked()) 
	 		item.setChecked(true); 
	 		map.clear(); 
	 		checkpoint = true; 
	 		clickpoint = true; 
	 		Toast.makeText(getApplicationContext(), "Click Map To Drop Marker", 
	 						Toast.LENGTH_LONG).show(); 
	 					} 
	 			if (id == R.id.range) { 
	 					item.setCheckable(true); 
	 					sMenu.getItem(1).setChecked(false); 
	 					sMenu.getItem(2).setChecked(false); 
	 					 
	 				item.setChecked(true); 
	 					createDialog(); 
	 						map.clear(); 
	 					} 
		return super.onOptionsItemSelected(item);
	}
	private void createDialog() { 
 					// TODO Auto-generated method stub 
 					final ContextThemeWrapper wrapper = new ContextThemeWrapper(this, android.R.style.Theme_Holo); 
 					final LayoutInflater layoutInflater = (LayoutInflater) wrapper.getSystemService(LAYOUT_INFLATER_SERVICE); 
 					 
 					View promptView = layoutInflater.inflate(R.layout.userinput, null); 
 			 
 					AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(wrapper); 
 					alertDialogBuilder.setView(promptView); 
 					alertDialogBuilder.setTitle("Enter Radius (Yards)"); 
 				final NumberPicker np = (NumberPicker) promptView 
 							.findViewById(R.id.numberPicker); 
 					
 				    final String str[] = {"0","50","100","150","200","250","300",
 				    		"350","400","450","500","550","600",
 				    		"650","700","750","800","850","900","950","1000"};
 				    np.setMaxValue(str.length-1); 
				    np.setMinValue(0); 
 				    np.setDisplayedValues(str); 				  
 					np.setWrapSelectorWheel(true);
 					// setup a dialog window 
 					alertDialogBuilder 
 						.setCancelable(false) 
 						.setPositiveButton("OK", new DialogInterface.OnClickListener() { 
 									public void onClick(DialogInterface dialog, int id) { 
 										// get user input and set it to result 
 										executeRangeQuery(Integer.parseInt(str[np.getValue()])); 
 									} 
 								}) 
 						.setNegativeButton("Cancel", 
 								new DialogInterface.OnClickListener() { 
 									public void onClick(DialogInterface dialog,	int id) { 
 										dialog.cancel(); 
 									} 
 								}); 
 					// create an alert dialog 
 					AlertDialog alertD = alertDialogBuilder.create(); 
 			 
 					alertD.show(); 
 			 
 				} 
 			 
 				protected void executeRangeQuery(int range) { 
 				// TODO Auto-generated method stub 
 					try { 
 						arrayPoints.clear(); 
 						fenceList.clear();
 						Location location = mLocationClient.getLastLocation(); 
 						String loc = Double.toString(location.getLatitude()) + ","+ Double.toString(location.getLongitude()) + "," + range ; 
 						URL url = new URL( 
 								"http://csci587team7.cloudapp.net:8080/587Service/rest/fence/range/" 
 										+ loc); 
 						System.out.println(url); 
 						Toast.makeText(getApplicationContext(), "Fetching Fences within "+range+" yard(s)", 
 						Toast.LENGTH_LONG).show(); 
 						new getRangeData().execute(url); 
 					} catch (MalformedURLException e) { 
 						// TODO Auto-generated catch block 
 						e.printStackTrace(); 
 				} 
 				 
 			} 
 		 
	
	@Override
	public boolean onMarkerClick(Marker marker) {
		// TODO Auto-generated method stub
		if (clickpoint == true) {
			URL url;
			String temp;
			StringBuffer buf = new StringBuffer();
			LatLng coordinate = marker.getPosition();
			temp = coordinate.toString();
			buf.append(temp.substring(temp.indexOf('(') + 1,
					temp.indexOf(')') - 1));
			try {
				arrayPoints.clear();
				fenceList.clear();
				map.clear();
				url = new URL(
						"http://csci587team7.cloudapp.net:8080/587Service/rest/fence/point/"
								+ buf.toString());
				System.out.println(url);
				Toast.makeText(getApplicationContext(), "Fetching Fences within 500 yards",
				Toast.LENGTH_LONG).show();
				new getFenceData().execute(url);
				clickpoint = false;
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			if (marker.isInfoWindowShown())
				marker.hideInfoWindow();
			else
				marker.showInfoWindow();
		}
		return false;
	}

	@Override
	public void onMapLongClick(LatLng arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onMapClick(LatLng point) {
		// TODO Auto-generated method stub
		if (checkpoint == true) {
			map.clear();
			map.addMarker(new MarkerOptions().position(point).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN)).draggable(true));
			//checkpoint = false;
			clickpoint=true;
			Toast.makeText(getApplicationContext(), "Drag Map Marker To Desired Location.\nClick Marker To Fetch Fences Within 500 yards.",
			Toast.LENGTH_SHORT).show();
			
		}
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

	private class getRangeData extends AsyncTask<URL, Integer, String> {
	     protected String doInBackground(URL... urls) {
			//Make a call to servlet to get fence data in XML and parse the xml to get each fence
					try {
					HttpURLConnection conn = (HttpURLConnection)urls[0].openConnection();
		                  conn.setRequestMethod("GET");
		                  conn.setDoInput(true);
		                  conn.connect();
		            InputStream stream = conn.getInputStream();
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
	    	
	    	 displayRange();
	    	 
	     }
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
						FenceObj obj = new FenceObj(fenceId,expiry,coord,validity,info,sLevel,null);
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
        map.clear();
        if(fenceList.size()==0)
		Toast.makeText(getApplicationContext(), "No Valid Fence found!", Toast.LENGTH_SHORT).show();
        
		for (int i = 0; i < fenceList.size(); i++) {
			System.out.println(fenceList.get(i).toString());
			fenceList.get(i).addPoly(map);
		}
	}
	
	public void displayRange() {

        map.clear();
        if(fenceList.size()==0)
		Toast.makeText(getApplicationContext(), "No Valid Fence found!", Toast.LENGTH_SHORT).show();
        
		for (int i = 0; i < fenceList.size(); i++) {
			System.out.println(fenceList.get(i).toString());
			fenceList.get(i).addPoly(map);
		}
		
	}
}
	
