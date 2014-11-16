package com.example.test2;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

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
}

