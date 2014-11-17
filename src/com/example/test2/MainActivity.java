package com.example.test2;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.GoogleMap;
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
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
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
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Spinner;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.maps.MapFragment;

@SuppressLint("NewApi")
public class MainActivity extends FragmentActivity implements
		GooglePlayServicesClient.ConnectionCallbacks,
		GooglePlayServicesClient.OnConnectionFailedListener, LocationListener,
		OnMapClickListener, OnMapLongClickListener, OnMarkerClickListener {

	// Google Map
	private static GoogleMap googleMap = null;
	private ArrayList<LatLng> arrayPoints = null;
	PolylineOptions polylineOptions;
	private static boolean checkClick = false;
	private static MenuItem mSubmit;
	private static MenuItem mCancel;

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
	public static final String BASE_URI = "http://csci587team7.cloudapp.net:8080/587Service/rest/insert/";
	

	public static String info, secLevel,expiry;

	// Define an object that holds accuracy and frequency parameters
	LocationRequest mLocationRequest;
	LocationClient mLocationClient;
	boolean mUpdatesRequested;
	SharedPreferences mPrefs;
	Editor mEditor;

	/*
	 * Called by Location Services if the connection to the location client
	 * drops because of an error.
	 */
	@Override
	public void onDisconnected() {
		// Display the connection status
		Toast.makeText(this, "Disconnected. Please re-connect.",
				Toast.LENGTH_SHORT).show();
	}

	/*
	 * Called by Location Services if the attempt to Location Services fails.
	 */
	@Override
	public void onConnectionFailed(ConnectionResult connectionResult) {
		/*
		 * Google Play services can resolve some errors it detects. If the error
		 * has a resolution, try sending an Intent to start a Google Play
		 * services activity that can resolve error.
		 */
		if (connectionResult.hasResolution()) {
			try {
				// Start an Activity that tries to resolve the error
				connectionResult.startResolutionForResult(this,
						CONNECTION_FAILURE_RESOLUTION_REQUEST);
				/*
				 * Thrown if Google Play services canceled the original
				 * PendingIntent
				 */
			} catch (IntentSender.SendIntentException e) {
				// Log the error
				e.printStackTrace();
			}
		} else {
			/*
			 * If no resolution is available, display a dialog to the user with
			 * the error.
			 */
			// showErrorDialog(connectionResult.getErrorCode());
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Create the LocationRequest object
		String UID = getIntent().getStringExtra("UID");
		

		
		checkClick = true;
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

		setContentView(R.layout.activity_main);
		arrayPoints = new ArrayList<LatLng>();
		SupportMapFragment fm = (SupportMapFragment) getSupportFragmentManager()
				.findFragmentById(R.id.map);
		googleMap = fm.getMap(); // display zoom map

		googleMap.setMyLocationEnabled(true);
		googleMap.setOnMapClickListener(this);
		googleMap.setOnMapLongClickListener(this);
		googleMap.setOnMarkerClickListener(this);

		try {
			// Loading map
			initilizeMap();

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	@Override
	public void onMapClick(LatLng point) {
		if (checkClick == true) {
			googleMap.addMarker(new MarkerOptions().position(point).icon(
					BitmapDescriptorFactory.fromResource(R.drawable.pt1)));
			arrayPoints.add(point);
			//Toast.makeText(getApplicationContext(), point.toString(), Toast.LENGTH_LONG).show();
		}
	}

	@Override
	public void onMapLongClick(LatLng point) {
		// googleMap.clear();
		// arrayPoints.clear();
		// checkClick = false;
	}

	public void countPolygonPoints() {
		if (arrayPoints.size() >= 3) {
			checkClick = false;
			mSubmit.setEnabled(true);
			PolygonOptions polygonOptions = new PolygonOptions();
			polygonOptions.addAll(arrayPoints);
			polygonOptions.strokeColor(Color.BLUE);
			polygonOptions.strokeWidth(5);
			polygonOptions.fillColor(Color.argb(100,126,247,245));
			Polygon polygon = googleMap.addPolygon(polygonOptions);
			
			//Toast.makeText(this, arrayPoints.toString(), Toast.LENGTH_SHORT).show();
			
		}
	}

	@Override
	public boolean onMarkerClick(Marker marker) {
		// TODO Auto-generated method stub
		System.out.println("Marker lat long=" + marker.getPosition());
		System.out.println("First postion check" + arrayPoints.get(0));
		System.out
				.println("**********All arrayPoints***********" + arrayPoints);
		if (arrayPoints.get(0).equals(marker.getPosition())) {
			System.out.println("********First Point choose************");
			countPolygonPoints();
			
			//sendForUpdate(pointsData);
		}
		return false;
	}

	protected String convertPolygonPoints() {
		StringBuffer buff = new StringBuffer();
		String temp = new String();
		int i;
		for(i=0;i<arrayPoints.size()-1;i++)
		{
			temp = arrayPoints.get(i).toString();
			buff.append(temp.substring(temp.indexOf('(')+1,temp.indexOf(')')-1)+',');
		}
		temp = arrayPoints.get(i).toString();
		buff.append(temp.substring(temp.indexOf('(')+1,temp.indexOf(')')-1));
		return buff.toString();
	}
	protected void sendForUpdate(final String arrayPoints) {
		// Send the polygon coordinates for updates
		new Thread(new Runnable() {
			public void run() {
				String id = "fences_sequence.nextval";
				try {
					//URL url = new URL("http://csci587team7.cloudapp.net:8080/587Service/rest/insert/5,34.032442,-118.291698,34.029090,-118.291677,34.028885,-118.288383,34.033153,-118.288834,2015-1-20%2020:00:00,1,This%20is%20a%20test%20fence,2");
					expiry = expiry.replace(" ", "%20");
					info = info.replace(" ", "%20");
				    String text = BASE_URI+id+","+arrayPoints+","+expiry+",1,"+info+","+secLevel;
					URL url = new URL(text);
					Log.d("URL", url.toString());
					HttpURLConnection conn = (HttpURLConnection)url.openConnection();
					conn.setRequestMethod("GET");
	                conn.setInstanceFollowRedirects(false);
	                conn.connect();
	                InputStream stream = conn.getInputStream();
	                stream.close();
	                
	                
	          	} catch (Exception e) {
					Log.d("URLException", e.toString());
				}
			}
		}).start();
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
	}

	/**
	 * function to load map. If map is not created it will create it for you
	 * */
	@SuppressLint("NewApi")
	private void initilizeMap() {
		if (googleMap == null) {
			googleMap = ((MapFragment) getFragmentManager().findFragmentById(
					R.id.map)).getMap();

			// check if map is created successfully or not
			if (googleMap == null) {
				Toast.makeText(getApplicationContext(),
						"Sorry! unable to create maps", Toast.LENGTH_SHORT)
						.show();
			} else {
				googleMap.setMyLocationEnabled(true);
			}
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		initilizeMap();
		/*
		 * Get any previous setting for location updates Gets "false" if an
		 * error occurs
		 */
		if (mPrefs.contains("KEY_UPDATES_ON")) {
			mUpdatesRequested = mPrefs.getBoolean("KEY_UPDATES_ON", false);

			// Otherwise, turn off location updates
		} else {
			mEditor.putBoolean("KEY_UPDATES_ON", false);
			mEditor.commit();
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

	/*
	 * Called by Location Services when the request to connect the client
	 * finishes successfully. At this point, you can request the current
	 * location or start periodic updates
	 */
	@Override
	public void onConnected(Bundle dataBundle) {

		if (mUpdatesRequested) {
			mLocationClient.requestLocationUpdates(mLocationRequest, this);
		}
		 Location location = mLocationClient.getLastLocation();
		    LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
		    CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 15);
		    googleMap.animateCamera(cameraUpdate);
	}

	/*
	 * Called when the Activity is no longer visible at all. Stop updates and
	 * disconnect.
	 */
	@Override
	protected void onStop() {
		// If the client is connected
		if (mLocationClient.isConnected()) {
			/*
			 * Remove location updates for a listener. The current Activity is
			 * the listener, so the argument is "this".
			 */
			// removeLocationUpdates(this);
		}
		/*
		 * After disconnect() is called, the client is considered "dead".
		 */
		mLocationClient.disconnect();
		super.onStop();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);

		mSubmit = menu.getItem(0);
		mCancel = menu.getItem(1);
		if (checkClick == true) {
			mSubmit.setEnabled(false);
		} else {
			mSubmit.setEnabled(true);
		}
		return true;
	}

	
	public void getDetails()
	{
		final ContextThemeWrapper wrapper = new ContextThemeWrapper(this, android.R.style.Theme_Holo);
		final LayoutInflater layoutInflater = (LayoutInflater) wrapper.getSystemService(LAYOUT_INFLATER_SERVICE);
		//AlertDialog.Builder builder = new AlertDialog.Builder(wrapper);
		//LayoutInflater layoutInflater = LayoutInflater.from(getApplicationContext());
		
		View promptView = layoutInflater.inflate(R.layout.prompt,null);

		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(wrapper);
		alertDialogBuilder.setView(promptView);
		alertDialogBuilder.setTitle("Fence Details");
		// set prompts.xml to be the layout file
		
		final Spinner spinner = (Spinner) promptView.findViewById(R.id.secLevel);
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
				this, R.array.secLevelArr,
				android.R.layout.simple_spinner_item);
		// Specify the layout to use when the list of choices appears
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		// Apply the adapter to the spinner
		spinner.setAdapter(adapter);
		final EditText text = (EditText) promptView.findViewById(R.id.descV);
		
		
		// setup a dialog window
		alertDialogBuilder
				.setCancelable(false)
				.setPositiveButton("NEXT", new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								// get user input and set it to result
								info = text.getText().toString();
								secLevel = spinner.getSelectedItem().toString().substring(0,1);
								
								View dateView = layoutInflater.inflate(R.layout.date,null);
								final DatePicker picker = (DatePicker) dateView.findViewById(R.id.datePicker);
								AlertDialog.Builder alertDialogBuilder1 = new AlertDialog.Builder(wrapper);
								alertDialogBuilder1.setView(dateView);
								alertDialogBuilder1.setTitle("Fence Expiry Date");
								
								alertDialogBuilder1
								.setCancelable(false)
								.setPositiveButton("NEXT", new DialogInterface.OnClickListener() {
											public void onClick(DialogInterface dialog, int id) {
												
												View timeView = layoutInflater.inflate(R.layout.time,null);
								final TimePicker tpicker = (TimePicker) timeView.findViewById(R.id.timePicker);
								
								AlertDialog.Builder alertDialogBuilder2 = new AlertDialog.Builder(wrapper);
								alertDialogBuilder2.setView(timeView);
								alertDialogBuilder2.setTitle("Fence Expiry Time");
								
								alertDialogBuilder2
								.setCancelable(false)
								.setPositiveButton("OK", new DialogInterface.OnClickListener() {
											public void onClick(DialogInterface dialog, int id) {
																							
								String pointsData = convertPolygonPoints();
								checkClick = true;
								mSubmit.setEnabled(false);
								googleMap.clear();
								arrayPoints.clear();
								int month = picker.getMonth()+1;
								expiry = picker.getYear()+"-"+month+"-"+picker.getDayOfMonth()+
										" "+tpicker.getCurrentHour()+":"+tpicker.getCurrentMinute()+":00";
								//Toast.makeText(getApplicationContext(), expiry, Toast.LENGTH_SHORT).show();
								//Toast.makeText(getApplicationContext(), pointsData, Toast.LENGTH_LONG).show();
								//Toast.makeText(getApplicationContext(), info, Toast.LENGTH_LONG).show();
								//Toast.makeText(getApplicationContext(), secLevel, Toast.LENGTH_LONG).show();
								sendForUpdate(pointsData);
											}
								})
								.setNegativeButton("Cancel",
										new DialogInterface.OnClickListener() {
											public void onClick(DialogInterface dialog,	int id) {
												dialog.cancel();
											}
										});
								AlertDialog alertTime = alertDialogBuilder2.create();

								alertTime.show();
											}
								})
								.setNegativeButton("Cancel",
										new DialogInterface.OnClickListener() {
											public void onClick(DialogInterface dialog,	int id) {
												dialog.cancel();
											}
										});
								
								AlertDialog alertDate = alertDialogBuilder1.create();

								alertDate.show();
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
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.submit) {
			//checkClick = true;
			//item.setEnabled(false);
			getDetails();
		}
		if (id == R.id.cancel) {
			
			mSubmit.setEnabled(false);
			checkClick = true;
			googleMap.clear();
			arrayPoints.clear();
		}

		return super.onOptionsItemSelected(item);
	}
}
