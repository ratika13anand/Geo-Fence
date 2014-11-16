package com.example.test2;

import java.util.ArrayList;

import android.graphics.Color;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;

public class FenceObj {

	private String fenceId;
	private String expiry;
	private String coordinates;
	private String validity;
	private String info;
	private String sLevel;
	
	public FenceObj(String fenceId,String expiry,String coordinates, String validity,String info, String sLevel)
	{
		this.fenceId = fenceId;
		this.expiry = expiry;
		this.coordinates = coordinates;
		this.validity = validity;
		this.info = info;
		this.sLevel = sLevel;
		
	}
	
	public String toString() {
	    return  "Fence Id: " + this.fenceId+ "," +
	    		"Expiry: " + this.expiry+ "," +
	    		"Coordinates: " + this.coordinates+ "," +
	    		"Validity: " + this.validity+ "," +
	    		"Info: " + this.info+ "," +
	    		"sLevel: " + this.sLevel ;
	}
	
	public void addPolygon(GoogleMap map)
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
		polygonOptions.strokeColor(Color.BLUE);
		polygonOptions.strokeWidth(5);
		polygonOptions.fillColor(Color.argb(100,126,247,245));
		Polygon polygon = map.addPolygon(polygonOptions);
		System.out.println("Adding Polygon");
	}
}
