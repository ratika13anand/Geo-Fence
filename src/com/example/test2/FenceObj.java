package com.example.test2;

import java.util.ArrayList;

import android.graphics.Color;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;

public class FenceObj {

	private String fenceId;
	private String expiry;
	private String coordinates;
	private String validity;
	private String info;
	private String sLevel;
	private String distance;
	
	public FenceObj(String fenceId,String expiry,String coordinates, String validity,String info, String sLevel, String distance)
	{
		this.fenceId = fenceId;
		this.expiry = expiry;
		this.coordinates = coordinates;
		this.validity = validity;
		this.info = info;
		this.sLevel = sLevel;
		if(distance!=null)
		   this.distance= distance;
		else
			this.distance="-1";
		
	}
	
	public String toString() {
	    return  "Fence Id: " + this.fenceId+ "," +
	    		"Expiry: " + this.expiry+ "," +
	    		"Coordinates: " + this.coordinates+ "," +
	    		"Validity: " + this.validity+ "," +
	    		"Info: " + this.info+ "," +
	    		"sLevel: " + this.sLevel ;
	}
	
	public void addPoly(GoogleMap map)
	{
		ArrayList<LatLng> arrayPoints = new ArrayList<LatLng>();
		String delims = "[;]";
		String[] tokens = this.coordinates.split(delims);
		//String test = "34.028472,-118.288533;34.027143,-118.284124;34.029268,-118.283866";
		//String[] tokens = test.split(delims);
		
		for (int i = 0; i < tokens.length; i++)
		{
			String[] geo = tokens[i].split(",");
			double lati = Double.parseDouble(geo[0]);
			double lngi = Double.parseDouble(geo[1]);
			arrayPoints.add(new LatLng(lati, lngi));
		}
		
		PolygonOptions polygonOptions = new PolygonOptions();
		polygonOptions.addAll(arrayPoints);
		switch(Integer.parseInt(this.sLevel))
		{
		case 1:polygonOptions.strokeColor(Color.parseColor("#FF0000")); // Red

		polygonOptions.fillColor(Color.argb(100,255,0,0));
			break;
		case 2:polygonOptions.strokeColor(Color.parseColor("#FF5500")); // Orange
		polygonOptions.fillColor(Color.argb(100,255,85,0));
			break;
		case 3:polygonOptions.strokeColor(Color.parseColor("#FFFF00")); // Yellow
		polygonOptions.fillColor(Color.argb(100,255,255,0));
			break;
		}
		
		polygonOptions.strokeWidth(5);
		Polygon polygon = map.addPolygon(polygonOptions);
		/*Marker marker = map.addMarker(new MarkerOptions()
        .position(arrayPoints.get(0))
        .title("Fence Details:")
        .snippet(this.info));
		marker.showInfoWindow();*/
		for(int i=0;i<arrayPoints.size();i++)
		{
			if(this.distance.equals("-1"))
			{
			map.addMarker(new MarkerOptions()
	        .position(arrayPoints.get(i))
	        .title("Fence Details")
	        .snippet(this.info));
			}
			else
				map.addMarker(new MarkerOptions()
		        .position(arrayPoints.get(i))
		        .title("Fence Details & Distance")
		        .snippet(this.info+"\nDistance: "+this.distance+" metres"));
				
		}
		System.out.println("Adding Polygon");
	}
}
