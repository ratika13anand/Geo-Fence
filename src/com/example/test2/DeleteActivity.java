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
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.PolylineOptions;

import android.support.v4.app.FragmentActivity;
import android.text.Html;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.location.Location;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class DeleteActivity extends FragmentActivity implements
		GooglePlayServicesClient.ConnectionCallbacks,
		GooglePlayServicesClient.OnConnectionFailedListener, LocationListener,
		OnMapClickListener, OnMapLongClickListener, OnMarkerClickListener {

	// Google Map
	private static GoogleMap map = null;
	private ArrayList<LatLng> arrayPoints = new ArrayList<LatLng>();
	PolylineOptions polylineOptions;
	private StringBuilder fence_ids = new StringBuilder();
	private static ArrayList<FenceObj> fenceList = new ArrayList<FenceObj>();

	private static ArrayList<FenceObj> overlapList = new ArrayList<FenceObj>();
	public static final String BASE_URI = "http://csci587team7.cloudapp.net:8080/587Service/rest/";

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
	private static Menu sMenu;

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
				View v = getLayoutInflater().inflate(
						R.layout.info_window_layout, null);

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

	protected void getFenceDisplay() {
		mLocationClient.connect();
		URL url;
		try {
			arrayPoints.clear();
			fenceList.clear();
			url = new URL(
					"http://csci587team7.cloudapp.net:8080/587Service/rest/fence");
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
		getMenuInflater().inflate(R.menu.delete, menu);
		sMenu = menu;

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
		if (id == R.id.submit) {

			askConfirmation();
			sMenu.getItem(0).setEnabled(false);

			overlapList.clear();
		}

		return super.onOptionsItemSelected(item);
	}

	@SuppressWarnings("deprecation")
	public void askConfirmation() {

		AlertDialog builder = new AlertDialog.Builder(DeleteActivity.this)
				.create();
		builder.setTitle("Confirm Deletion?");
		builder.setButton("Yes", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				// User clicked OK button.Delete the Fence
				String text1 = "http://csci587team7.cloudapp.net:8080/587Service/rest/delete/"
						+ fence_ids;
				URL url1;
				try {
					url1 = new URL(text1);
					new del_this().execute(url1);
				} catch (MalformedURLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				dialog.dismiss();
			}
		});
		builder.setButton2("Cancel", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				// User cancelled the dialog
				getFenceDisplay();
				dialog.dismiss();
			}
		});
		builder.show();
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
	public void onMapLongClick(LatLng point) {
		// TODO Auto-generated method stub
		checkFence(point);
	}

	@Override
	public void onMapClick(LatLng point) {
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
		LatLng latLng = new LatLng(location.getLatitude(),
				location.getLongitude());
		CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng,
				15);
		map.animateCamera(cameraUpdate);
	}

	@Override
	public void onDisconnected() {
		Toast.makeText(this, "Disconnected. Please re-connect.",
				Toast.LENGTH_SHORT).show();

	}

	private class getFenceData extends AsyncTask<URL, Integer, String> {
		protected String doInBackground(URL... urls) {

			// Make a call to servlet to get fence data in XML and parse the xml
			// to get each fence

			try {

				HttpURLConnection conn = (HttpURLConnection) urls[0]
						.openConnection();
				conn.setRequestMethod("GET");
				conn.setDoInput(true);
				conn.connect();
				InputStream stream = conn.getInputStream();
				XmlPullParserFactory xmlFactoryObject = XmlPullParserFactory
						.newInstance();
				XmlPullParser myParser = xmlFactoryObject.newPullParser();
				myParser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES,
						false);
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

		fenceList.clear();
		int event;
		String text = null;

		String fenceId = new String();
		String expiry = new String();
		String coord = new String();
		String validity = new String();
		String info = new String();
		String sLevel = new String();
		try {
			event = myParser.getEventType();
			while (event != XmlPullParser.END_DOCUMENT) {
				String name = myParser.getName();
				switch (event) {
				case XmlPullParser.START_TAG:
					break;
				case XmlPullParser.TEXT:
					text = myParser.getText();
					break;
				case XmlPullParser.END_TAG:
					if (name.equals("fenceid")) {
						fenceId = text;
					} else if (name.equals("expiry")) {
						expiry = text;
					} else if (name.equals("coordinates")) {
						coord = text;
					} else if (name.equals("validity")) {
						validity = text;
					} else if (name.equals("info")) {
						info = text;
					} else if (name.equals("security_level")) {
						sLevel = text;
					} else if (name.equals("fence")) {
						FenceObj obj = new FenceObj(fenceId, expiry, coord,
								validity, info, sLevel, null);
						fenceList.add(obj);
						fenceId = null;
						expiry = null;
						coord = null;
						validity = null;
						info = null;
						sLevel = null;
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

		if (fenceList.size() == 0)
			Toast.makeText(getApplicationContext(), "No Valid Fence found!",
					Toast.LENGTH_SHORT).show();

		for (int i = 0; i < fenceList.size(); i++) {
			System.out.println(fenceList.get(i).toString());
			fenceList.get(i).addPoly(map);
		}
	}

	public void highlightFences() {

		if (overlapList.size() == 0)
			Toast.makeText(getApplicationContext(), "No Fence Selected!",
					Toast.LENGTH_SHORT).show();

		for (int i = 0; i < overlapList.size(); i++) {
			System.out.println(overlapList.get(i).toString());
			overlapList.get(i).highlightPoly(map);
		}
	}

	protected void checkFence(LatLng point) {
		System.out.println(point);
		String text = BASE_URI + "overlapfence/currentloc/" + point.latitude
				+ "," + point.longitude;
		try {
			URL url = new URL(text);
			new findOverlap().execute(url);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private class findOverlap extends AsyncTask<URL, Integer, String> {
		ProgressDialog progress = new ProgressDialog(DeleteActivity.this);

		protected void onPreExecute() {
			this.progress = ProgressDialog
					.show(DeleteActivity.this, null, Html
							.fromHtml("<big>Highlighting Selection...</big>"),
							true);
		}

		protected String doInBackground(URL... urls) {
			try {
				HttpURLConnection conn = (HttpURLConnection) urls[0]
						.openConnection();
				conn.setReadTimeout(10000 /* milliseconds */);
				conn.setConnectTimeout(15000 /* milliseconds */);
				conn.setRequestMethod("GET");
				conn.setDoInput(true);
				conn.connect();
				InputStream stream = conn.getInputStream();
				XmlPullParserFactory xmlFactoryObject = XmlPullParserFactory
						.newInstance();
				XmlPullParser myParser = xmlFactoryObject.newPullParser();
				myParser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES,
						false);
				myParser.setInput(stream, null);
				String res = checkOverlap(myParser);
				stream.close();
				return res;

			} catch (Exception e) {
				Log.d("Exception", e.toString());
				return "false";
			}

		}

		protected void onPostExecute(String res) {
			progress.dismiss();
			if (res.equals("true")) {
				highlightFences();
				sMenu.getItem(0).setEnabled(true);
			} else
				Toast.makeText(getApplicationContext(), "No Fences Selected!",
						Toast.LENGTH_SHORT).show();
		}
	}

	public String checkOverlap(XmlPullParser myParser) {

		String text = null;

		String fenceId = new String();
		String expiry = new String();
		String coord = new String();
		String validity = new String();
		String info = new String();
		String sLevel = new String();
		String distance = new String();
		int overlap_no = 0;
		int event;
		try {
			event = myParser.getEventType();
			while (event != XmlPullParser.END_DOCUMENT) {
				String name = myParser.getName();
				switch (event) {
				case XmlPullParser.START_TAG:
					break;
				case XmlPullParser.TEXT:
					text = myParser.getText();
					break;
				case XmlPullParser.END_TAG:
					if (name.equals("fenceid")) {

						fenceId = text;
					} else if (name.equals("expiry")) {

						expiry = text;
					} else if (name.equals("coordinates")) {
						coord = text;
					} else if (name.equals("validity")) {
						validity = text;
					} else if (name.equals("info")) {
						info = text;
					} else if (name.equals("security_level")) {
						sLevel = text;
					} else if (name.equals("distance")) {
						distance = text;
					} else if (name.equals("fence")) {
						overlap_no++;
						if (overlapList.size() == 0) {

							fence_ids.append(fenceId);
						} else
							fence_ids.append("," + fenceId);

						FenceObj obj = new FenceObj(fenceId, expiry, coord,
								validity, info, sLevel, distance);
						overlapList.add(obj);

						fenceId = null;
						expiry = null;
						coord = null;
						validity = null;
						info = null;
						sLevel = null;
						distance = null;
					}
					break;
				}
				event = myParser.next();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		if (overlap_no != 0)
			return "true";
		else
			return "false";
	}

	private class del_this extends AsyncTask<URL, Integer, String> {
		ProgressDialog progress = new ProgressDialog(DeleteActivity.this);

		protected void onPreExecute() {
			this.progress = ProgressDialog.show(DeleteActivity.this, null,
					Html.fromHtml("<big>Deleting Fence(s)...</big>"), true);
		}

		protected String doInBackground(URL... urls) {
			try {

				HttpURLConnection conn = (HttpURLConnection) urls[0]
						.openConnection();
				conn.setReadTimeout(10000 /* milliseconds */);
				conn.setConnectTimeout(15000 /* milliseconds */);
				conn.setRequestMethod("GET");
				conn.setDoInput(true);
				conn.connect();
				InputStream stream = conn.getInputStream();
				XmlPullParserFactory xmlFactoryObject = XmlPullParserFactory
						.newInstance();
				XmlPullParser myParser = xmlFactoryObject.newPullParser();
				myParser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES,
						false);
				myParser.setInput(stream, null);
				String res = checkdelstatus(myParser);
				stream.close();
				return res;

			} catch (Exception e) {
				e.printStackTrace();
				return "false";
			}

		}

		protected void onPostExecute(String res) {
			getFenceDisplay();
			overlapList.clear();
			fence_ids.setLength(0);
			progress.dismiss();
			if (res.equals("true")) {
				// Toast.makeText(getApplicationContext(), "Fences deleted",
				// Toast.LENGTH_SHORT).show();
			} else
				Toast.makeText(getApplicationContext(),
						"Unable to delete selected fences!", Toast.LENGTH_SHORT)
						.show();
		}

	}

	public String checkdelstatus(XmlPullParser myParser) {
		int status;
		String mode = "";
		String text = "";
		String event_status = "";
		String test = myParser.toString();
		System.out.println(test);
		try {
			status = myParser.getEventType();
			while (status != XmlPullParser.END_DOCUMENT) {
				String name = myParser.getName();
				switch (status) {
				case XmlPullParser.START_TAG:
					break;
				case XmlPullParser.TEXT:
					text = myParser.getText();
					break;
				case XmlPullParser.END_TAG:
					if (name.equals("status")) {
						event_status = text;
					}

					break;
				}
				status = myParser.next();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (event_status.trim().equalsIgnoreCase("error")) {
			// tried to login but failed.Hence normal app with just display
			return "false";
		} else {
			return "true";
		}
	}

}
