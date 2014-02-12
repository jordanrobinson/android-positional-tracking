package com.jordanrobinson.positionaltracker;

import java.util.Set;

import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;

public class GPSFragment extends Fragment implements LocationListener {	

	//private LocationManager locationManager;
	private String provider;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//setContentView(R.layout.gps_main);
		//latituteField = (TextView) findViewById(R.id.TextView02);
		//longitudeField = (TextView) findViewById(R.id.TextView04);

		// Get the location manager

	}

	public void initGPS() {
		Log.d("GPS", "init GPS...");
		ContextHolder.locationManager = (LocationManager) ContextHolder.context.getSystemService(Context.LOCATION_SERVICE);
		Criteria criteria = new Criteria();
		provider = ContextHolder.locationManager.getBestProvider(criteria, false);
		Location location = ContextHolder.locationManager.getLastKnownLocation(provider);			
		ContextHolder.locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1, this);
		if (location != null) {
			onLocationChanged(location);			
		}
		Log.d("GPS", "GPS init success");
	}

	@Override
	public void onResume() {
		super.onResume();
	}

	@Override
	public void onPause() {
		super.onPause();
		//if (locationManager != null) {			
		//	locationManager.removeUpdates(this);
		//}
	}

	@Override
	public void onProviderEnabled(String provider) {
		Log.d("GPS", "Provider enabled: " + provider);
	}

	@Override
	public void onProviderDisabled(String provider) {
		Log.d("GPS", "Provider disabled: " + provider);
	}

	@Override
	public void onLocationChanged(Location location) {
		final Bundle extras = location.getExtras();
		final Set<String> keySet = extras.keySet();
		StringBuilder sb = new StringBuilder();

		for (final String key: keySet) {

			sb.append(key);
			sb.append(":");
			sb.append(extras.get(key));
			sb.append(" ");
		}

		Log.d("GPS", "GPS Lat: " + location.getLatitude());
		Log.d("GPS", "GPS Long: " + location.getLongitude());
		Log.d("GPS", "GPS Provider: " + location.getProvider());
		Log.d("GPS", "GPS Time: " + location.getTime());
		Log.d("GPS", "GPS Altitude: " + location.getAltitude());
		Log.d("GPS", "GPS Speed: " + location.getSpeed());
		Log.d("GPS", "GPS Bearing: " + location.getBearing());
		Log.d("GPS", "GPS Accuracy: " + location.getAccuracy());
		Log.d("GPS", "GPS Extras: " + sb.toString()); 

		String dbRecord = location.getLatitude() + "," +
				location.getLongitude() + "," +
				location.getProvider() + "," +
				location.getTime() + "," +
				location.getAltitude() + "," +
				location.getSpeed() + "," +
				location.getBearing() + "," +
				location.getAccuracy() + "," +
				sb.toString();

		writeToServer(dbRecord);


	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
	}

	public void writeToServer(String dbRecord) {		

		if (MainActivity.serverOut != null) {
			Log.d("sensor", "GPS" + " - " + dbRecord);
			MainActivity.serverOut.println(dbRecord);
		}
		else {
			//Log.d("connection", getArguments().getString(FRIENDLY_NAME) + " - " + "couldn't connect to server");
		}
	}

}
