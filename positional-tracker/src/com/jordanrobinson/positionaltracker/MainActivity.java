package com.jordanrobinson.positionaltracker;

import java.io.BufferedReader;
import java.io.PrintWriter;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.FragmentTransaction;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings.Secure;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Menu;
public class MainActivity extends FragmentActivity implements ActionBar.TabListener {
	
	public static BufferedReader serverIn = null;
	public static PrintWriter serverOut = null;
	public static String deviceID;
	public static final String SERVER_IP = "192.168.1.129";
	public static final boolean CONNECT_ON_START = false;

	/**
	 * The {@link android.support.v4.view.PagerAdapter} that will provide
	 * fragments for each of the sections. We use a
	 * {@link android.support.v4.app.FragmentPagerAdapter} derivative, which
	 * will keep every loaded fragment in memory. If this becomes too memory
	 * intensive, it may be best to switch to a
	 * {@link android.support.v4.app.FragmentStatePagerAdapter}.
	 */
	SectionsPagerAdapter mSectionsPagerAdapter;
	SensorManager manager;

	/**
	 * The {@link ViewPager} that will host the section contents.
	 */
	ViewPager mViewPager;
	LocationManager locManager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		deviceID = Secure.getString(this.getContentResolver(), Secure.ANDROID_ID);
		
		if (CONNECT_ON_START) {
			new ConnectToServerTask().execute();			
		}
		
		setContentView(R.layout.activity_main);
		
		ContextHolder.context = this; //hold the context externally so the fragments can init using this

		// Set up the action bar.
		final ActionBar actionBar = getActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

		// Create the adapter that will return a fragment for each of the three
		// primary sections of the app.
		mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

		// Set up the ViewPager with the sections adapter.
		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setAdapter(mSectionsPagerAdapter);
		
		// When swiping between different sections, select the corresponding
		// tab. We can also use ActionBar.Tab#select() to do this if we have
		// a reference to the Tab.
		mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
			@Override
			public void onPageSelected(int position) {
				actionBar.setSelectedNavigationItem(position);
			}
		});

		// For each of the sections in the app, add a tab to the action bar.
		for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
			// Create a tab with text corresponding to the page title defined by
			// the adapter. Also specify this Activity object, which implements
			// the TabListener interface, as the callback (listener) for when
			// this tab is selected.
			actionBar.addTab(
					actionBar.newTab()
					.setText(mSectionsPagerAdapter.getPageTitle(i))
					.setTabListener(this));
			
			//by getting the item, we force creation, which forces an init
			if (mSectionsPagerAdapter.getItem(i) instanceof SensorFragment) {
				SensorFragment frag = ((SensorFragment) mSectionsPagerAdapter.getItem(i));
				frag.initSensor(ContextHolder.context);
			}
			else if (mSectionsPagerAdapter.getItem(i) instanceof SensorFragment) {
				GPSFragment frag = ((GPSFragment) mSectionsPagerAdapter.getItem(i));
				frag.initGPS();
			}
			
		}
	}
	
	@Override
	public void onResume() {

		super.onResume();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {

		mViewPager.setCurrentItem(tab.getPosition());

		Log.d("tabChanged", "tab " + tab.getText() + " selected which is a " + tab.getClass());
	}

	@Override
	public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
	}

	@Override
	public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
	}

	/**
	 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
	 * one of the sections/tabs/pages.
	 */
	public class SectionsPagerAdapter extends FragmentPagerAdapter {

		final String accelerometerTabTitle = "Accelerometer";
		final String ambientTemperatureTabTitle = "Temperature";
		final String gravityTabTitle = "Gravity";
		final String lightTabTitle = "Light";
		final String gyroscopeTabTitle = "Gyroscope";
		final String accelerationTabTitle = "Acceleration";
		final String magneticFieldTabTitle = "Magnetic Field";
		final String pressureTabTitle = "Pressure";
		final String proximityTabTitle = "Proximity";
		final String relativeHumidityTabTitle = "Relative Humidity";
		final String rotationVectorTabTitle = "Rotation Vector";
		final String significantMotionTabTitle = "Significant Motion";
		final String gpsTabTitle = "GPS"; //currently not used
		
		final String allTabTitle = "All Sensors"; //currently not used
		
		
		final String[] tabTypes = {accelerometerTabTitle, ambientTemperatureTabTitle, gravityTabTitle, lightTabTitle,
				gyroscopeTabTitle, accelerationTabTitle, magneticFieldTabTitle, pressureTabTitle, proximityTabTitle,
				relativeHumidityTabTitle, rotationVectorTabTitle, significantMotionTabTitle, gpsTabTitle};

		public SectionsPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@SuppressLint("InlinedApi")
		@Override
		public Fragment getItem(int position) {
			// getItem is called to instantiate the fragment for the given page.
			// Return a DummySectionFragment (defined as a static inner class
			// below) with the page number as its lone argument.

			Fragment fragment = null;

			Bundle args = new Bundle();

			switch (position) {
			case 0:
				fragment = new SensorFragment();
				args.putString(SensorFragment.FRIENDLY_NAME, "Accelerometer");
				args.putInt(SensorFragment.SENSOR_TYPE, Sensor.TYPE_ACCELEROMETER);
				fragment.setArguments(args);
				((SensorFragment) fragment).initSensor(ContextHolder.context);
				break;
			case 1:
				fragment = new SensorFragment();
				args.putString(SensorFragment.FRIENDLY_NAME, "Temperature");
				args.putInt(SensorFragment.SENSOR_TYPE, Sensor.TYPE_AMBIENT_TEMPERATURE);
				fragment.setArguments(args);
				((SensorFragment) fragment).initSensor(ContextHolder.context);
				break;
			case 2:
				fragment = new SensorFragment();
				args.putString(SensorFragment.FRIENDLY_NAME, "Gravity");
				args.putInt(SensorFragment.SENSOR_TYPE, Sensor.TYPE_GRAVITY);
				fragment.setArguments(args);
				((SensorFragment) fragment).initSensor(ContextHolder.context);
				break;
			case 3:
				fragment = new SensorFragment();
				args.putString(SensorFragment.FRIENDLY_NAME, "Light");
				args.putInt(SensorFragment.SENSOR_TYPE, Sensor.TYPE_LIGHT);
				fragment.setArguments(args);
				((SensorFragment) fragment).initSensor(ContextHolder.context);
				break;
			case 4:
				fragment = new SensorFragment();
				args.putString(SensorFragment.FRIENDLY_NAME, "Gyroscope");
				args.putInt(SensorFragment.SENSOR_TYPE, Sensor.TYPE_GYROSCOPE);
				fragment.setArguments(args);
				((SensorFragment) fragment).initSensor(ContextHolder.context);
				break;
			case 5:
				fragment = new SensorFragment();
				args.putString(SensorFragment.FRIENDLY_NAME, "Acceleration");
				args.putInt(SensorFragment.SENSOR_TYPE, Sensor.TYPE_LINEAR_ACCELERATION);
				fragment.setArguments(args);
				((SensorFragment) fragment).initSensor(ContextHolder.context);
				break;
			case 6:
				fragment = new SensorFragment();
				args.putString(SensorFragment.FRIENDLY_NAME, "Magnetic Field");
				args.putInt(SensorFragment.SENSOR_TYPE, Sensor.TYPE_MAGNETIC_FIELD);
				fragment.setArguments(args);
				((SensorFragment) fragment).initSensor(ContextHolder.context);
				break;
			case 7:
				fragment = new SensorFragment();
				args.putString(SensorFragment.FRIENDLY_NAME, "Pressure");
				args.putInt(SensorFragment.SENSOR_TYPE, Sensor.TYPE_PRESSURE);
				fragment.setArguments(args);
				((SensorFragment) fragment).initSensor(ContextHolder.context);
				break;
			case 8:
				fragment = new SensorFragment();
				args.putString(SensorFragment.FRIENDLY_NAME, "Proximity");
				args.putInt(SensorFragment.SENSOR_TYPE, Sensor.TYPE_PROXIMITY);
				fragment.setArguments(args);
				((SensorFragment) fragment).initSensor(ContextHolder.context);
				break;
			case 9:
				fragment = new SensorFragment();
				args.putString(SensorFragment.FRIENDLY_NAME, "Relative Humidity");
				args.putInt(SensorFragment.SENSOR_TYPE, Sensor.TYPE_RELATIVE_HUMIDITY);
				fragment.setArguments(args);
				((SensorFragment) fragment).initSensor(ContextHolder.context);
				break;
			case 10:
				fragment = new SensorFragment();
				args.putString(SensorFragment.FRIENDLY_NAME, "Rotation Vector");
				args.putInt(SensorFragment.SENSOR_TYPE, Sensor.TYPE_ROTATION_VECTOR);
				fragment.setArguments(args);
				((SensorFragment) fragment).initSensor(ContextHolder.context);
				break;
			case 11:
				fragment = new SensorFragment();
				args.putString(SensorFragment.FRIENDLY_NAME, "Significant Motion");
				args.putInt(SensorFragment.SENSOR_TYPE, Sensor.TYPE_SIGNIFICANT_MOTION);
				fragment.setArguments(args);
				((SensorFragment) fragment).initSensor(ContextHolder.context);				
				break;
			case 12:
				fragment = new GPSFragment();
				args.putString(SensorFragment.FRIENDLY_NAME, "GPS");
//				args.putInt(SensorFragment.SENSOR_TYPE, Sensor.TYPE_ALL);
				fragment.setArguments(args);
				((GPSFragment) fragment).initGPS();
				break;
			default:
				break;
			}

			
			
			return fragment;
		}

		@Override
		public int getCount() {
			return tabTypes.length;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			return tabTypes[position];
		}
	}
	
	public LocationManager getLocationManager() {
		return locManager;
	}
}
