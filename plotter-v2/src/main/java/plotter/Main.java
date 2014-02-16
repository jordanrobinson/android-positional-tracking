package plotter;

import java.awt.FileDialog;
import java.awt.Frame;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.Scanner;

import org.jzy3d.analysis.AbstractAnalysis;
import org.jzy3d.chart.factories.AWTChartComponentFactory;
import org.jzy3d.colors.Color;
import org.jzy3d.maths.Coord3d;
import org.jzy3d.plot3d.primitives.Scatter;
import org.jzy3d.plot3d.rendering.canvas.Quality;

public class Main extends AbstractAnalysis {

	Statement statement;
	Connection connection;

	public static volatile ArrayList<String> points = new ArrayList<String>();
	final static int PORT_NUMBER = 30303;
	final static String DB_URL = "jdbc:mysql://192.168.1.129:3306/positional";
	private final String DRIVER_CLASS = "org.mariadb.jdbc.Driver";

	private float x = 0.0f;
	private float y = 0.0f;
	private float z = 0.0f;

	private static final int TYPE_ACCELEROMETER = 1;
	private static final int TYPE_MAGNETIC_FIELD = 2;
	private static final int TYPE_ORIENTATION = 3;
	private static final int TYPE_GYROSCOPE = 4;
	private static final int TYPE_LIGHT = 5;
	private static final int TYPE_PRESSURE = 6;
	private static final int TYPE_TEMPERATURE = 7;
	private static final int TYPE_PROXIMITY = 8;
	private static final int TYPE_GRAVITY = 9;
	private static final int TYPE_LINEAR_ACCELERATION = 10;
	private static final int TYPE_ROTATION_VECTOR = 11;
	private static final int TYPE_RELATIVE_HUMIDITY = 12;
	private static final int TYPE_AMBIENT_TEMPERATURE = 13;
	private static final int TYPE_MAGNETIC_FIELD_UNCALIBRATED = 14;
	private static final int TYPE_GAME_ROTATION_VECTOR = 15;
	private static final int TYPE_GYROSCOPE_UNCALIBRATED = 16;
	private static final int TYPE_SIGNIFICANT_MOTION = 17;

	private static final float ALPHA = 0.2f;
	private static final float SIZE = 10.0f;

	private static final Color ACCELEROMETER_COLOR = new Color(0.7f, 0.1f, 0.1f, ALPHA);
	private static final Color GYROSCOPE_COLOR = new Color(0.1f, 0.7f, 0.1f, ALPHA);
	private static final Color GRAVITY_COLOR = new Color(0.7f, 0.7f, 0.7f, ALPHA);
	private static final Color ACCELERATION_COLOR = new Color(0.7f, 0.1f, 0.7f, ALPHA);

	private Coord3d[] coords;
	private Color[] colors;

	private boolean cumulativeMode = false;




	public static void main(String[] args) throws Exception {

		Main main = new Main();

		main.printAllDataSeparated();

		//System.out.println(main.rawReadingsOutput(main.getReadings()));
		//System.out.println();
		//System.out.println();



		//
		//		AnalysisLauncher.open(main);
		//
		//
		//
		//		main.init();

		//		while (true) {			
		//			main.init();
		//			System.out.println("waiting at " + new Date());
		//			Thread.sleep(5 * 1500); //wait a minute for the next input
		//		}

	}

	public void printAllDataSeparated() {
		Main main = new Main();
		ArrayList<Reading> readings = main.removeDupes(main.getReadings());

		ArrayList<ArrayList<Reading>> allReadings = new ArrayList<ArrayList<Reading>>();

		int[] types = {TYPE_ACCELEROMETER,
				TYPE_MAGNETIC_FIELD,
				TYPE_ORIENTATION,
				TYPE_GYROSCOPE,
				TYPE_LIGHT,
				TYPE_PRESSURE,
				TYPE_TEMPERATURE,
				TYPE_PROXIMITY,
				TYPE_GRAVITY,
				TYPE_LINEAR_ACCELERATION,
				TYPE_ROTATION_VECTOR,
				TYPE_RELATIVE_HUMIDITY,
				TYPE_AMBIENT_TEMPERATURE,
				TYPE_MAGNETIC_FIELD_UNCALIBRATED,
				TYPE_GAME_ROTATION_VECTOR,
				TYPE_GYROSCOPE_UNCALIBRATED,
				TYPE_SIGNIFICANT_MOTION}; 

		String[] stringTypes = {"TYPE_ACCELEROMETER","TYPE_MAGNETIC_FIELD","TYPE_ORIENTATION","TYPE_GYROSCOPE",
				"TYPE_LIGHT","TYPE_PRESSURE","TYPE_TEMPERATURE","TYPE_PROXIMITY","TYPE_GRAVITY",
				"TYPE_LINEAR_ACCELERATION","TYPE_ROTATION_VECTOR","TYPE_RELATIVE_HUMIDITY",
				"TYPE_AMBIENT_TEMPERATURE","TYPE_MAGNETIC_FIELD_UNCALIBRATED","TYPE_GAME_ROTATION_VECTOR",
				"TYPE_GYROSCOPE_UNCALIBRATED","TYPE_SIGNIFICANT_MOTION"};

		String headings = "";
		
		cumulativeMode = false;


		for (int i = 0; i < types.length; i++) {
			ArrayList<Reading> currentReadings = filterReadings(readings, types[i]);

			if (currentReadings.size() > 0) {

				headings += stringTypes[i];
				headings += ",X,Y,Z,";

				if (cumulativeMode) {
					allReadings.add(cumulativeReadingOutput(filterReadings(readings, types[i]), types[i]));	
				}
				else {
					allReadings.add(filterReadings(readings, types[i]));
				}

			}
		}

		System.out.println(headings);

		System.out.println(CSVOutput(allReadings));


	}

	public void init() throws Exception {

		if (chart == null) {
			this.chart = AWTChartComponentFactory.chart(Quality.Advanced, "newt");
			cumulativeMode = true;
			if (cumulativeMode) {
				inputFromDbCumulative();
			}
			else {
				inputFromDb();
			}
		}
		//		else {
		//			int diff = inputFromDb(); //should set the points and colours to the latest from the database
		//			System.out.println("diff found of " + diff + " adding points");
		//
		//			for (int i = diff; i > 0; i--) {
		//				Point point = new Point(coords[coords.length - i], colors[colors.length - i], SIZE);
		//				this.chart.getScene().getGraph().add(point);
		//			}
		//		}






		//		this.chart.getScene().getGraph().add(test);

		//		inputCSV();



		//inputFromDb();




		//		new Thread()
		//		{
		//			public void run() {
		//				inputFromDevice();
		//			}
		//		}.start();

		//inputCSV();
	}

	public void buildScatter(Coord3d[] points, Color[] colors) {

		Scatter scatter = new Scatter(points, colors);
		scatter.setWidth(SIZE);

		chart.getScene().add(scatter);
	}

	public ArrayList<Reading> removeDupes(ArrayList<Reading> input) {
		Iterator<Reading> iterator = input.iterator();

		ArrayList<Reading> ret = new ArrayList<Reading>();

		while (iterator.hasNext()) {

			Reading x = (Reading) iterator.next();			
			if (!ret.contains(x)) {
				ret.add(x);
			}
		}
		return ret;
	}

	public ArrayList<Reading> filterReadings(ArrayList<Reading> readings, int type) {
		ArrayList<Reading> ret = new ArrayList<Reading>();
		for (int i = 0; i < readings.size(); i++) {
			if (readings.get(i).getSensor() == type) {
				ret.add(readings.get(i));
			}
		}
		return ret;
	}

	public ArrayList<Reading> getReadings() {

		ArrayList<Reading> ret = new ArrayList<Reading>();

		try {

			Class.forName(DRIVER_CLASS);

			connection = DriverManager.getConnection(DB_URL, "root", "password");

			statement = connection.createStatement();

			//String query = "SELECT * FROM positional.positionalData WHERE collect_time > UNIX_TIMESTAMP(2013-11-11);";
			String query = "SELECT * FROM positional.positionalData ORDER BY collect_time ASC;";

			ResultSet results = statement.executeQuery(query);

			while (results.next()) {
				int id = results.getInt("id");
				int sensor = results.getInt("sensor");
				long collectTime = results.getLong("collect_time");
				String device = results.getString("device");

				ArrayList<Double> readings = new ArrayList<Double>();

				readings.add(results.getDouble("reading_one"));
				readings.add(results.getDouble("reading_two"));
				readings.add(results.getDouble("reading_three"));

				boolean important = results.getBoolean("important");

				int accuracy = results.getInt("accuracy");
				ret.add(new Reading(id, sensor, collectTime, device, readings, important, accuracy));
			}

		} catch (ClassNotFoundException e1) {
			e1.printStackTrace();
		} catch (SQLException e1) {
			e1.printStackTrace();
		}

		return ret;
	}

	public String rawReadingsOutput(ArrayList<Reading> readings) {
		String ret = "";
		for (int i = 0; i < readings.size(); i++) {
			ret += readings.get(i).toString();
		}
		return ret;
	}

	public String cumulativeTextOutput(ArrayList<Reading> readings, int type) {
		String ret = "";

		Double cumulativeX = 0.0D;
		Double cumulativeY = 0.0D;
		Double cumulativeZ = 0.0D;

		for (int i = 0; i < readings.size(); i++) {
			if (readings.get(i).getSensor() == type) {

				Calendar calendar = Calendar.getInstance();
				calendar.setTime(new Date(readings.get(i).getCollectTime() / 1000000)); //converts to millis from nanos
				int minutes = calendar.get(Calendar.MINUTE);
				int seconds = calendar.get(Calendar.SECOND);
				int millis = calendar.get(Calendar.MILLISECOND);

				String dateFormat = "";

				if (minutes < 10 && seconds < 10) {
					dateFormat = "0" + minutes + ":0" + seconds + "." + millis + "";
				}
				else if (minutes < 10) { 
					dateFormat = "0" + minutes + ":" + seconds + "." + millis + "";
				}
				else if (seconds < 10) {
					dateFormat = "" + minutes + ":0" + seconds + "." + millis + "";
				}
				else {
					dateFormat = "" + minutes + ":" + seconds + "." + millis + "";
				}

				ret += dateFormat; 
				cumulativeX += readings.get(i).getReadings().get(0);
				cumulativeY += readings.get(i).getReadings().get(1);
				cumulativeZ += readings.get(i).getReadings().get(2);

				ret += "," + doubleFormatter(cumulativeX);
				ret += " ," + doubleFormatter(cumulativeY);
				ret += " ," + doubleFormatter(cumulativeZ) + "\n";
			}

		}

		return ret;
	}

	public ArrayList<Reading> cumulativeReadingOutput(ArrayList<Reading> readings, int type) {

		Double cumulativeX = 0.0D;
		Double cumulativeY = 0.0D;
		Double cumulativeZ = 0.0D;

		for (int i = 0; i < readings.size(); i++) {
			if (readings.get(i).getSensor() == type) {

				cumulativeX += readings.get(i).getReadings().get(0);
				cumulativeY += readings.get(i).getReadings().get(1);
				cumulativeZ += readings.get(i).getReadings().get(2);

				ArrayList<Double> newValues = new ArrayList<Double>();

				newValues.add(cumulativeX);
				newValues.add(cumulativeY);
				newValues.add(cumulativeZ);

				readings.get(i).setReadings(newValues);
			}
		}

		return readings;
	}

	public String CSVOutput(ArrayList<ArrayList<Reading>> readingsArrays) {
		String ret = "";
		Calendar calendar = Calendar.getInstance();


		int size = 0;
		for (int j = 0; j < readingsArrays.size(); j++) {
			if (readingsArrays.get(j).size() > size) {
				size = readingsArrays.get(j).size();
			}
		}

		for (int i = 0; i < size; i++) {
			for (int j = 0; j < readingsArrays.size(); j++) {
				int currentSize = readingsArrays.get(j).size();

				if (i < currentSize) {
					calendar.setTime(new Date(readingsArrays.get(j).get(i).getCollectTime() / 1000000)); //converts to millis from nanos
					int minutes = calendar.get(Calendar.MINUTE);
					int seconds = calendar.get(Calendar.SECOND);
					int millis = calendar.get(Calendar.MILLISECOND);

					String dateFormat = "";

					if (minutes < 10 && seconds < 10) {
						dateFormat = "0" + minutes + ":0" + seconds + "." + millis + "";
					}
					else if (minutes < 10) { 
						dateFormat = "0" + minutes + ":" + seconds + "." + millis + "";
					}
					else if (seconds < 10) {
						dateFormat = "" + minutes + ":0" + seconds + "." + millis + "";
					}
					else {
						dateFormat = "" + minutes + ":" + seconds + "." + millis + "";
					}

					ret += dateFormat; 
					ret += "," + doubleFormatter(readingsArrays.get(j).get(i).getReadings().get(0));
					ret += "," + doubleFormatter(readingsArrays.get(j).get(i).getReadings().get(1));
					ret += "," + doubleFormatter(readingsArrays.get(j).get(i).getReadings().get(2));
				}
				else {
					ret += ",,,";
				}
				ret += ",";
			}
			ret += "\n";
		}

		return ret;
	}

	public String doubleFormatter(Double doub) {
		String ret = "";
		if (doub >= 0.0D) {
			ret = String.format("+%1.10f", doub);
		}
		else {
			ret = String.format("%1.10f", doub);
		}
		return ret;
	}

	public int inputFromDbCumulative() {

		ArrayList<Coord3d> coordList = new ArrayList<Coord3d>();
		ArrayList<Color> colorList = new ArrayList<Color>();

		try {


			Class.forName(DRIVER_CLASS);

			connection = DriverManager.getConnection(DB_URL, "root", "password");

			statement = connection.createStatement();

			//String query = "SELECT * FROM positional.positionalData WHERE collect_time > UNIX_TIMESTAMP(2013-11-11);";
			String query = "SELECT * FROM positional.positionalData ORDER BY collect_time ASC;";

			ResultSet results = statement.executeQuery(query);



			double cumulativeX = 0.0f;
			double cumulativeY = 0.0f;
			double cumulativeZ = 0.0f;			

			long collectTime = 0;

			while (results.next()) {
				//moduleID = "" + firstResults.getInt("ID");

				switch (results.getInt("sensor")) {
				case TYPE_ACCELEROMETER: 					// 1




					break;
				case TYPE_MAGNETIC_FIELD: 					// 2
					break;
				case TYPE_ORIENTATION: 						// 3
					break;
				case TYPE_GYROSCOPE: 						// 4

					long currentCumulativeTime = results.getLong("collect_time");

					if (collectTime == 0) {
						collectTime = currentCumulativeTime;
					}
					double changeX = results.getDouble("reading_one") - cumulativeX;
					double changeY = results.getDouble("reading_two") - cumulativeY;
					double changeZ = results.getDouble("reading_three") - cumulativeZ;

					if (currentCumulativeTime - collectTime < 1000) {

						cumulativeX = cumulativeX + changeX;
						cumulativeY = cumulativeY + changeY;
						cumulativeZ = cumulativeZ + changeZ;
						System.out.println(currentCumulativeTime - collectTime);
					}
					else {						
						if (cumulativeX != 0.0f || cumulativeY != 0.0f || cumulativeZ != 0.0f) {
							coordList.add(new Coord3d(cumulativeX,
									cumulativeY,
									cumulativeZ));
							colorList.add(GYROSCOPE_COLOR);
						}

						cumulativeX = cumulativeX + changeX;
						cumulativeY = cumulativeY + changeY;
						cumulativeZ = cumulativeZ + changeZ;
						collectTime = currentCumulativeTime;

						System.out.println(true);
					}


					//					coordList.add(new Coord3d(results.getDouble("reading_one"),
					//							results.getDouble("reading_two"),
					//							results.getDouble("reading_three")));
					//					colorList.add(GYROSCOPE_COLOR);
					break;
				case TYPE_LIGHT: 							// 5
					break;
				case TYPE_PRESSURE: 						// 6
					break;
				case TYPE_TEMPERATURE:						// 7
					break;
				case TYPE_PROXIMITY: 						// 8
					break;
				case TYPE_GRAVITY: 							// 9
					break;
					//					coordList.add(new Coord3d(results.getDouble("reading_one"),
					//							results.getDouble("reading_two"),
					//							results.getDouble("reading_three")));
					//					colorList.add(GRAVITY_COLOR);
					//					break;
				case TYPE_LINEAR_ACCELERATION: 				// 10
					break;
				case TYPE_ROTATION_VECTOR:					// 11
					break;
				case TYPE_RELATIVE_HUMIDITY: 				// 12
					break;
				case TYPE_AMBIENT_TEMPERATURE:				// 13
					break;
				case TYPE_MAGNETIC_FIELD_UNCALIBRATED:		// 14
					break;
				case TYPE_GAME_ROTATION_VECTOR:				// 15
					break;
				case TYPE_GYROSCOPE_UNCALIBRATED: 			// 16
					break;
				case TYPE_SIGNIFICANT_MOTION:				// 17
					break;

				default:
					break;
				}


			}

			Coord3d[] coordsOutput = new Coord3d[coordList.size()];
			Color[] colorOutput = new Color[colorList.size()];

			for (int i = 0; i < coordList.size(); i++) {
				coordsOutput[i] = coordList.get(i);
				colorOutput[i] = colorList.get(i);
			}


			if (coords == null || colors == null) {
				coords = coordsOutput;
				colors = colorOutput;
				buildScatter(coordsOutput, colorOutput);
				return 0;
			}
			else {
				int diff = coordsOutput.length - coords.length;

				coords = coordsOutput;
				colors = colorOutput;

				return diff;
			}


		} catch (ClassNotFoundException e1) {
			e1.printStackTrace();
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
		return 0;
	}


	public int inputFromDb() {

		ArrayList<Coord3d> coordList = new ArrayList<Coord3d>();
		ArrayList<Color> colorList = new ArrayList<Color>();

		try {

			Class.forName(DRIVER_CLASS);

			connection = DriverManager.getConnection(DB_URL, "root", "password");

			statement = connection.createStatement();

			//String query = "SELECT * FROM positional.positionalData WHERE collect_time > UNIX_TIMESTAMP(2013-11-11);";
			String query = "SELECT * FROM positional.positionalData;";

			ResultSet results = statement.executeQuery(query);
			while (results.next()) {
				//moduleID = "" + firstResults.getInt("ID");

				switch (results.getInt("sensor")) {
				case TYPE_ACCELEROMETER: 					// 1
					coordList.add(new Coord3d(results.getDouble("reading_one"),
							results.getDouble("reading_two"),
							results.getDouble("reading_three")));
					colorList.add(ACCELEROMETER_COLOR);
					break;
				case TYPE_MAGNETIC_FIELD: 					// 2
					break;
				case TYPE_ORIENTATION: 						// 3
					break;
				case TYPE_GYROSCOPE: 						// 4
					coordList.add(new Coord3d(results.getDouble("reading_one"),
							results.getDouble("reading_two"),
							results.getDouble("reading_three")));
					colorList.add(GYROSCOPE_COLOR);
					break;
				case TYPE_LIGHT: 							// 5
					break;
				case TYPE_PRESSURE: 						// 6
					break;
				case TYPE_TEMPERATURE:						// 7
					break;
				case TYPE_PROXIMITY: 						// 8
					break;
				case TYPE_GRAVITY: 							// 9
					coordList.add(new Coord3d(results.getDouble("reading_one"),
							results.getDouble("reading_two"),
							results.getDouble("reading_three")));
					colorList.add(GRAVITY_COLOR);
					break;
				case TYPE_LINEAR_ACCELERATION: 				// 10
					coordList.add(new Coord3d(results.getDouble("reading_one"),
							results.getDouble("reading_two"),
							results.getDouble("reading_three")));
					colorList.add(ACCELERATION_COLOR);
					break;
				case TYPE_ROTATION_VECTOR:					// 11
					break;
				case TYPE_RELATIVE_HUMIDITY: 				// 12
					break;
				case TYPE_AMBIENT_TEMPERATURE:				// 13
					break;
				case TYPE_MAGNETIC_FIELD_UNCALIBRATED:		// 14
					break;
				case TYPE_GAME_ROTATION_VECTOR:				// 15
					break;
				case TYPE_GYROSCOPE_UNCALIBRATED: 			// 16
					break;
				case TYPE_SIGNIFICANT_MOTION:				// 17
					break;

				default:
					break;
				}


			}

			Coord3d[] coordsOutput = new Coord3d[coordList.size()];
			Color[] colorOutput = new Color[colorList.size()];

			for (int i = 0; i < coordList.size(); i++) {
				coordsOutput[i] = coordList.get(i);
				colorOutput[i] = colorList.get(i);
			}


			if (coords == null || colors == null) {
				coords = coordsOutput;
				colors = colorOutput;
				buildScatter(coordsOutput, colorOutput);
				return 0;
			}
			else {
				int diff = coordsOutput.length - coords.length;

				coords = coordsOutput;
				colors = colorOutput;

				return diff;
			}


		} catch (ClassNotFoundException e1) {
			e1.printStackTrace();
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
		return 0;
	}


	public void inputFromDevice() {

		ArrayList<Coord3d> coordList = new ArrayList<Coord3d>();
		ArrayList<Color> colorList = new ArrayList<Color>();

		try {

			System.out.println(new Date());
			Thread.sleep(5 * 10000); //wait a minute for the next input

			for (int i = 0; i < points.size(); i++) {
				try {
					if (i > 10000) {
						continue;
					}

					i++; //auto id

					if ("1".equals(points.get(i))) {
						i++; //sensor number
						i++; //collect time
						i++; //device ID

						x = new Float(points.get(i++)); //reading x
						y = new Float(points.get(i++)); //reading y
						z = new Float(points.get(i)); //reading z
						coordList.add(new Coord3d(x, y, z));
						colorList.add(new Color(0.0f, 0.0f, 0.8f, 0.25f));
					}
					else {
						i++;
						i++;
						i++;
						i++;
						i++;
					}


					i++;
					i++;

					System.out.println("" + x + " " + y + " " + z);
				}
				catch (NoSuchElementException e) {
					break;
				}
			}

			Coord3d[] coordsOutput = new Coord3d[coordList.size()];
			Color[] colorOutput = new Color[colorList.size()];

			for (int i = 0; i < coordList.size(); i++) {
				coordsOutput[i] = coordList.get(i);
				colorOutput[i] = colorList.get(i);

			}

			buildScatter(coordsOutput, colorOutput);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public static String[] plotterListener() {

		String inputLine;

		try { 
			ServerSocket serverSocket = new ServerSocket(PORT_NUMBER);
			Socket clientSocket = serverSocket.accept();
			PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
			BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));


			System.out.println("Connection established to device");

			//			out.println("Connected to Positional Tracker server " + VERSION);


			// Initiate conversation with client
			while ((inputLine = in.readLine()) != null) {
				//System.out.println(inputLine);
				out.println("ack");
				if (inputLine.equals("exit")) {
					break;
				} 
				else if (inputLine.equals("hello")) {
					System.out.println("Communication established to device");
				}
				else {
					addToPoints(inputLine.split(","));
				}
			}

			in.close();
			out.close();
			serverSocket.close();


		} catch (IOException e) {
			System.out.println("Exception caught when trying to listen on port "
					+ PORT_NUMBER + " or listening for a connection");
			System.out.println(e.getMessage());
			System.exit(1);
		} 
		catch (ArrayIndexOutOfBoundsException e) {
			System.out.println("Exception caught when parsing input");
			System.out.println(e.getMessage());
		}


		return null;

	}


	public void inputCSV() {

		int size = 50000;

		float a;

		Coord3d[] points = new Coord3d[size];
		Color[] colors = new Color[size];


		Random r = new Random();
		r.setSeed(0);

		for(int i = 0; i < size; i++){
			//	        x = r.nextFloat() - 0.5f;
			//	        y = r.nextFloat() - 0.5f;
			//	        z = r.nextFloat() - 0.5f;
			x = y = z = 0.0f;
			points[i] = new Coord3d(x, y, z);
			a = 0.0f;
			colors[i] = new Color(x, y, z, a);
		}



		Frame frame = new Frame();
		FileDialog fd = new FileDialog(frame, "Please Load a File"); //setting up the file dialog		
		String textLine = null;
		File pointsFile = null;

		fd.setVisible(true);
		String fileName = fd.getFile();

		if (fileName != null) {
			pointsFile = new File(fd.getDirectory(),fileName);
			if (!fileName.endsWith(".csv") || !pointsFile.exists() || !pointsFile.canRead()) { //checks if the input file doesn't exist, or is not readable or is not a valid input file
				System.err.println("file doesn't seem to be valid");
				return;
			}
		}
		else {
			System.err.println("selection cancelled");
			return;
		}

		Scanner fileScanner = null;
		try {
			fileScanner = new Scanner(pointsFile);
		} catch (FileNotFoundException e) {
			System.err.println("file not found");
			e.printStackTrace();
			return;
		}
		System.out.println("file selected: "+fd.getDirectory()+fileName+"\n"); //outputs file path and name for testing purposes
		while (fileScanner.hasNextLine()) {
			textLine = fileScanner.nextLine().trim();
			Scanner lineScanner = new Scanner (textLine);
			lineScanner.useDelimiter(",");

			if (textLine.matches("^//+.*|^$")) { //checks if the line starts with // OR if the line is empty (blank line)
				//if so do nothing and let the line be skipped
			}		
			else {
				for (int i = 0; i < size; i++) {
					try {
						x = lineScanner.nextFloat();
						y = lineScanner.nextFloat();
						z = lineScanner.nextFloat();

						System.out.println("" + x + " " + y + " " + z);
						points[i] = new Coord3d(x, y, z);
						a = 0.25f;
						colors[i] = new Color(1.0f, 0.0f, 0.0f, a);
					}
					catch (NoSuchElementException e) {
						break;
					}
				}
			}
			lineScanner.close();
		}
		fileScanner.close();

		buildScatter(points, colors);

	}

	public static void addToPoints(String[] input) {
		for (int i = 0; i < input.length; i++) {
			points.add(input[i]);
		}

	}
}
