package com.example.test2;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class Login extends Activity {

	public static final String BASE_URI = "http://csci587team7.cloudapp.net:8080/587Service/rest/authenticate/";

    public static final String PREFS_LOGIN_USERNAME_KEY = "USERNAME" ;
    public static final String PREFS_LOGIN_PASSWORD_KEY = "PASSWORD" ;
    
    private static String uname;
    private static String pswd;
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		String loggedInUserName = PrefUtils.getFromPrefs(Login.this,
				PREFS_LOGIN_USERNAME_KEY, null);
		String loggedInUserPassword = PrefUtils.getFromPrefs(Login.this,
				PREFS_LOGIN_PASSWORD_KEY, null);

		if (loggedInUserName != null && loggedInUserPassword != null)
		{
			Intent intent = new Intent(this, HomeActivity.class);
		    intent.putExtra("UID", loggedInUserName);
		    intent.putExtra("lstatus","valid");
			startActivity(intent);
			finish();
		}
		else
		{
		setContentView(R.layout.activity_login);
		}
	}

	private class getLogin extends AsyncTask<URL, Integer, String> {
		ProgressDialog progress = new ProgressDialog(Login.this);
		protected void onPreExecute(){
			this.progress = ProgressDialog.show(Login.this, null, Html.fromHtml("<big>Signing In...</big>"), true);
		}
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
				    checkLogin(myParser);
				   
		            stream.close();
		            
				} catch (Exception e) {
					Log.d("Exception", e.toString());
				}
				return null;

			}
	     protected void onPostExecute(String res) {
	    	 progress.dismiss();
	    	
	    	 return;
	     }
		}



	public void login(View view) 
	{
		EditText edit_user=(EditText)findViewById(R.id.uname);
    	uname=edit_user.getText().toString();
    	EditText edit_pwd=(EditText)findViewById(R.id.pswd);
		pswd =edit_pwd.getText().toString();
		if("".equals(pswd.trim()) || "".equals(uname.trim()))
		{
			runOnUiThread(new Runnable() {
		        public void run()
		        {
		            Toast.makeText(getApplicationContext(),"Enter User Name and Password", Toast.LENGTH_LONG).show();
		        }
		    });
			return;
		}
		try {	
			String text = BASE_URI+uname+","+pswd;
			//text="http://csci587team7.cloudapp.net:8080/587Service/rest/authenticate/abc,1234";
			URL url = new URL(text);
			new getLogin().execute(url);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	public void skip(View view)
	{
		//skips user login.Hence invalid user.Just display fences
		Intent intent = new Intent(this, DisplayActivity.class);
	    intent.putExtra("lstatus","invalid");
		startActivity(intent);
		
	}

public void checkLogin(XmlPullParser myParser) {
	int status;
	String mode="";
	String text="";
	String event_status="";
	String test=myParser.toString();
	System.out.println(test);
	try {
		status = myParser.getEventType();
		while (status != XmlPullParser.END_DOCUMENT) {
			String name=myParser.getName();
			switch (status){
			case XmlPullParser.START_TAG:
				break;
			case XmlPullParser.TEXT:
				text = myParser.getText();
				break;
			case XmlPullParser.END_TAG:
				if(name.equals("status")){
					 event_status= text;
				}
				
				break;
			}
			status = myParser.next();
		}
	}
	catch(Exception e)
	{
		e.printStackTrace();
	}
		if(event_status.trim().equalsIgnoreCase("Invalid"))
		{				
			//tried to login but failed.Hence normal app with just display
			    runOnUiThread(new Runnable() {
			        public void run()
			        {
			            Toast.makeText(getApplicationContext(),"Invalid Username or Pasword!", Toast.LENGTH_LONG).show();
			        }
			    });
			    
			 
			}
		else
		{
			//valid user.Hence give him right to modify/create fences 
			PrefUtils.saveToPrefs(Login.this, PREFS_LOGIN_USERNAME_KEY, uname);
			PrefUtils.saveToPrefs(Login.this, PREFS_LOGIN_PASSWORD_KEY, pswd);
			Intent intent = new Intent(this, HomeActivity.class);
		    intent.putExtra("UID", uname);
		    intent.putExtra("lstatus","valid");
			startActivity(intent);
			finish();
		}
	}


}

