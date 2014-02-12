package com.jordanrobinson.positionaltracker;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.PointStyle;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint.Align;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Criteria;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.util.LogPrinter;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class SensorFragment extends Fragment implements SensorEventListener {

	/**
	 * The fragment argument representing the section number for this
	 * fragment.
	 */
	private View rootView = null;
	public static final String FRIENDLY_NAME = "string";
	public static final String SENSOR_TYPE = "integer";
	SensorManager manager;
	SensorEvent currentEvent;

	//graphing capability
	XYSeries xSeries = new XYSeries("X");
	XYSeries ySeries = new XYSeries("Y");
	XYSeries zSeries = new XYSeries("Z");
	GraphicalView chart = null;
	int previousUpdate = -1;
	boolean importantPoint = false;
	ArrayList<String> pendingUpdates = new ArrayList<String>();

	MenuItem reconnect;
	MenuItem disconnect;
	MenuItem gps;
	public SensorFragment() {

	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		reconnect = menu.add("Connect");
		disconnect = menu.add("Disconnect");
		gps = menu.add("GPS");
		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {	
		if (item.equals(reconnect)) {
			Log.d("Connect", "attempting reconnect");
			new com.jordanrobinson.positionaltracker.ConnectToServerTask().execute();
		}
		else if (item.equals(disconnect)) {
			Log.d("Disconnect", "attempting disconnect");
			MainActivity.serverIn = null;
			MainActivity.serverOut = null;
		}
		else if (item.equals(gps)) {
			Log.d("GPS", "attempting GPS grab...");
			if (ContextHolder.locationManager == null) {
//				ContextHolder.locationManager = (LocationManager) ContextHolder.context.getSystemService(Context.LOCATION_SERVICE);
//				ContextHolder.locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 100, (LocationListener) ContextHolder.gpsContext); //every minute	
			}

			LogPrinter printer = new LogPrinter(1, "location dump");

			Criteria criteria = new Criteria();
			String provider = ContextHolder.locationManager.getBestProvider(criteria, false);
			Location location = ContextHolder.locationManager.getLastKnownLocation(provider);
			if (location != null) {
				Log.d("GPS", "using " + provider);
				location.dump(printer, "location at " + new Date() + ": ");
			}
			else {
				Log.d("GPS", "problem getting GPS dump");
			}
		}

		return true;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		rootView = inflater.inflate(R.layout.fragment_main, container, false);




		TextView outputTextView = (TextView) rootView.findViewById(R.id.section_label);
		outputTextView.setText("This device doesn't seem to have a " + getArguments().getString(FRIENDLY_NAME) 
				+ " sensor.");

		XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();

		dataset.addSeries(xSeries);
		dataset.addSeries(ySeries);
		dataset.addSeries(zSeries);

		XYSeriesRenderer xRenderer = new XYSeriesRenderer();
		xRenderer.setColor(Color.RED);
		xRenderer.setPointStyle(PointStyle.CIRCLE);
		xRenderer.setFillPoints(true);
		xRenderer.setLineWidth(1);
		xRenderer.setDisplayChartValues(false);

		XYSeriesRenderer yRenderer = new XYSeriesRenderer();
		yRenderer.setColor(Color.GREEN);
		yRenderer.setPointStyle(PointStyle.SQUARE);
		yRenderer.setFillPoints(true);
		yRenderer.setLineWidth(1);
		yRenderer.setDisplayChartValues(false);

		XYSeriesRenderer zRenderer = new XYSeriesRenderer();
		zRenderer.setColor(Color.BLUE);
		zRenderer.setPointStyle(PointStyle.TRIANGLE);
		zRenderer.setFillPoints(true);
		zRenderer.setLineWidth(1);
		zRenderer.setDisplayChartValues(false);

		XYMultipleSeriesRenderer multiRenderer = new XYMultipleSeriesRenderer();
		multiRenderer.setLabelsColor(Color.BLACK);
		multiRenderer.setAxesColor(Color.BLACK);
		multiRenderer.setMarginsColor(Color.WHITE);
		multiRenderer.setXLabelsColor(Color.BLACK);
		multiRenderer.setYLabelsColor(0, Color.BLACK);
		multiRenderer.setLabelsTextSize(12.0f);
		multiRenderer.setChartTitle("Sensor Output");
		multiRenderer.setXTitle("Seconds");
		multiRenderer.setYTitle("Values of Sensor");
		multiRenderer.setMargins(new int[]{ 80, 80, 80, 80 });
		multiRenderer.setYLabelsAlign(Align.LEFT);
		multiRenderer.setApplyBackgroundColor(true);
		multiRenderer.setBackgroundColor(Color.WHITE);
		multiRenderer.setXAxisMin(0);
		multiRenderer.setXAxisMax(60);
		multiRenderer.setYAxisMin(-15);
		multiRenderer.setYAxisMax(15);

		multiRenderer.addSeriesRenderer(xRenderer);
		multiRenderer.addSeriesRenderer(yRenderer);
		multiRenderer.addSeriesRenderer(zRenderer);

		LinearLayout layout = (LinearLayout) rootView.findViewById(R.id.chart_container);
		chart = ChartFactory.getLineChartView(getActivity().getBaseContext(), dataset, multiRenderer);


		layout.addView(chart);

		Button recordButton = (Button) rootView.findViewById(R.id.record_button);
		recordButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				importantPoint = true;	        	
			}
		});

		setHasOptionsMenu(true);

		return rootView;
	}

	public void initSensor(Context context) {
		manager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
		Sensor sensor = manager.getDefaultSensor(getArguments().getInt(SENSOR_TYPE));
		manager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);
		if (sensor == null) {
			Log.d("Sensor Type", "Sensor seems to be null");
		} else {			
			Log.d("Sensor Type", "Sensor " + getArguments().getString(FRIENDLY_NAME) + " init, using " 
					+ sensor.getVendor() + " implementation.");
		}
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
	}

	@Override
	public void onSensorChanged(SensorEvent event) {

		int importantPointInteger = 0;
		if (importantPoint) {
			importantPointInteger = 1;
		}

		if (rootView != null) {
			TextView outputTextView = (TextView) rootView.findViewById(R.id.section_label);
			String output = "";

			for (int i = 0; i < event.values.length; i++) {
				output = output + "Value " + i + ": " + event.values[i] + "\n";

			}
			outputTextView.setText(output);

		}
		String dbRecord = getArguments().getInt(SENSOR_TYPE) + "," + //sensor type
				event.timestamp + //collect time
				","+ MainActivity.deviceID + "," + //device id
				event.values[0] + "," + //reading one
				event.values[1] + "," + //reading two
				event.values[2] + "," //reading three
				+ importantPointInteger + "," //important flag
				+ event.accuracy; //accuracy


		importantPoint = false;

		pendingUpdates.add(dbRecord);

		executeEachSecond(event);

	}

	private void executeEachSecond(SensorEvent event) {
		Calendar c = Calendar.getInstance(); 
		int seconds = c.get(Calendar.SECOND);

		if (seconds > previousUpdate && xSeries != null) {
			previousUpdate = seconds;

			if (chart != null) {
				xSeries.add(seconds + 0d, event.values[0]);
				ySeries.add(seconds + 0d, event.values[1]);
				zSeries.add(seconds + 0d, event.values[2]);
				chart.repaint();
			}


		}
		writeToServer();

		if (seconds == 59) {
			xSeries.clear();
			ySeries.clear();
			zSeries.clear();
			previousUpdate = -1;
		}

	}


	public SensorEvent getCurrentEvent() {
		return currentEvent;
	}

	public void writeToServer() {		

		if (MainActivity.serverOut != null) {

			for (int i = 0; i < pendingUpdates.size(); i++) {
				//Log.d("sensor", getArguments().getString(FRIENDLY_NAME) + " - " + pendingUpdates.get(i));
				MainActivity.serverOut.println(pendingUpdates.get(i));
			}
			pendingUpdates.clear();

		}
		else {
			//Log.d("connection", getArguments().getString(FRIENDLY_NAME) + " - " + "couldn't connect to server");
		}
	}

}
