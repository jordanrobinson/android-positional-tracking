package plotter;

import java.util.ArrayList;

public class Reading { //object representation of a database row from the app

	private int id;


	private int sensor;
	private long collectTime;
	private String device;
	private ArrayList<Double> readings;
	private boolean important;
	private int accuracy;

	public Reading(int id, int sensor, long collectTime, String device,
			ArrayList<Double> readings, boolean important, int accuracy) {
		this.id = id;
		this.sensor = sensor;
		this.collectTime = collectTime;
		this.device = device;
		this.readings = readings;
		this.important = important;
		this.accuracy = accuracy;
	}

	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public int getSensor() {
		return sensor;
	}
	public void setSensor(int sensor) {
		this.sensor = sensor;
	}
	public long getCollectTime() {
		return collectTime;
	}
	public void setCollectTime(long collectTime) {
		this.collectTime = collectTime;
	}
	public String getDevice() {
		return device;
	}
	public void setDevice(String device) {
		this.device = device;
	}
	public ArrayList<Double> getReadings() {
		return readings;
	}
	public void setReadings(ArrayList<Double> readings) {
		this.readings = readings;
	}
	public boolean isImportant() {
		return important;
	}
	public void setImportant(boolean important) {
		this.important = important;
	}
	public int getAccuracy() {
		return accuracy;
	}
	public void setAccuracy(int accuracy) {
		this.accuracy = accuracy;
	}
	
	@Override
	public String toString() {
		return "Reading [id=" + id + ", sensor=" + sensor + ", collectTime="
				+ collectTime + ", device=" + device + ", readings=" + readings
				+ ", important=" + important + ", accuracy=" + accuracy + "]\n";
	}

	@Override
	public boolean equals(Object obj) {
		Reading comparator = (Reading) obj;
		
		boolean sensor = this.getSensor() == comparator.getSensor();
		boolean collectTime = this.getCollectTime() == comparator.getCollectTime();
		boolean device = this.getDevice().equals(comparator.getDevice());
		boolean readings = true;
		for (int i = 0; i < this.getReadings().size(); i++) {
			if (! (this.getReadings().get(i).equals(comparator.getReadings().get(i)))) {
				readings = false;
			}
		}
		return (sensor && collectTime && device && readings);
	}


}
