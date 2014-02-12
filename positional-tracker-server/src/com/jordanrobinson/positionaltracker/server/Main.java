package com.jordanrobinson.positionaltracker.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

public class Main {

	final static int PORT_NUMBER = 30303;
	final static String VERSION = "v0.01";
	final static String DB_USERNAME = "root";
	final static String DB_PASSWORD = "password";
	final static String DB_HOST = "ada-server";
	final static int DB_PORT = 3306;
	final static String DB_NAME = "positional";
	static Connection CONNECTION = null;
	
	static boolean debug_output;


	final static String TEST_DATA = "1," + new java.sql.Date(0) + ",123,1.0,2.1,3.4";


	public static void main(String[] args) {
		
		for (int i = 0; i < args.length; i++) {
			System.out.println("arg found=" + args[i]);
		}
		
		if (args.length > 0) {
			if (args[0] != null && args[0].equalsIgnoreCase("d")) {
				debug_output = true;
			}			
		}
		
		databaseListener();

	}

	public static void databaseListener() {

		System.out.println("Starting up server... Version " + VERSION);

		System.out.println("Attempting database connection");
		System.out.println("port is " + PORT_NUMBER);
		if (debug_output) {
			System.out.println("Enabling debug output");
		}

		try {

			Class.forName("org.mariadb.jdbc.Driver");
			Properties connectionProps = new Properties();
			connectionProps.put("user", DB_USERNAME);
			connectionProps.put("password", DB_PASSWORD);

			CONNECTION = DriverManager.getConnection(
					"jdbc:mysql://" + DB_HOST +	":" + DB_PORT + "/",
					connectionProps);
			System.out.println("Connected to database");


		} catch (ClassNotFoundException e) {
			System.out.println("Exception caught when trying to load database driver");
			e.printStackTrace();
		} catch (SQLException e) {
			System.out.println("Exception caught when trying to connect to database");
			e.printStackTrace();
		}

		System.out.println("Waiting for device to connect");

		//restart server if socket closes
		while(waitForConnect()) {
			System.out.println("Socket closed, waiting for another device to connect");
			//do nothing
		}
	}
	
	public static String[] plotterListener() {
		
		String inputLine;

		try (ServerSocket serverSocket = new ServerSocket(PORT_NUMBER);
				Socket clientSocket = serverSocket.accept();
				PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
				BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));) { 


			System.out.println("Connection established to device");

			out.println("Connected to Positional Tracker server " + VERSION);


			// Initiate conversation with client
			while ((inputLine = in.readLine()) != null) {
				System.out.println(inputLine);
				out.println("ack");
				if (inputLine.equals("exit")) {
					break;
				} 
				else if (inputLine.equals("hello")) {
					System.out.println("Communication established to device");
				}
				else {
//					plotter.Main
//					inputLine.split(",");

				}
			}
			
			in.close();
			out.close();
			

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

	public static boolean waitForConnect() {
		String inputLine;

		try (ServerSocket serverSocket = new ServerSocket(PORT_NUMBER);
				Socket clientSocket = serverSocket.accept();
				PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
				BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));) {


			System.out.println("Connection established to device");

			out.println("Connected to Positional Tracker server " + VERSION);


			// Initiate conversation with client
			while ((inputLine = in.readLine()) != null) {
				System.out.println(inputLine);
				out.println("ack");
				if (inputLine.equals("exit")) {
					break;
				} 
				else if (inputLine.equals("hello")) {
					System.out.println("Communication established to device");
				}
				else {
					String[] dbInput = inputLine.split(",");
					//String[] testInput = TEST_DATA.split(",");
					
					String insertQuery = "";
					
					if (dbInput.length == 8) {
						insertQuery = "INSERT INTO positional.positionalData(" +
								"id, sensor, collect_time, device, reading_one, reading_two, reading_three, important, accuracy" +
								") VALUES (" + 
								"NULL,"  //auto id
								+ dbInput[0] + "," //sensor number
								+ "'" + dbInput[1] + "'," //collect time
								+ "'" + dbInput[2] + "'," //device ID (if applicable)
								+ dbInput[3] + "," //reading one 
								+ dbInput[4] + "," //reading two
								+ dbInput[5] + "," //reading three
								+ dbInput[6] + "," //important
								+ dbInput[7] + ")"; //accuracy						
					}
					else if (dbInput.length == 9) {
						insertQuery = "INSERT INTO positional.gpsData(" +
								"id, latitude, longitude, provider, collect_time, altitude, speed, bearing, accuracy, extra_info" +
								") VALUES (" + 
								"NULL,"  //auto id
								+ dbInput[0] + "," //latitude
								+ dbInput[1] + "," //longitude
								+ "'" + dbInput[2] + "'," //provider
								+ "'" + dbInput[3] + "'," //time 
								+ dbInput[4] + "," //altitude
								+ dbInput[5] + "," //speed
								+ dbInput[6] + "," //bearing
								+ dbInput[7] + "," //accuracy
								+ "'" + dbInput[8] + "')"; //accuracy	
					}

					if (debug_output) {
						System.out.println("insert query is - " + insertQuery);						
					}

					Statement statement = CONNECTION.createStatement();
					statement.executeUpdate(insertQuery);
				}

			}
			in.close();
			out.close();
		} catch (IOException e) {
			System.out.println("Exception caught when trying to listen on port "
					+ PORT_NUMBER + " or listening for a connection");
			System.out.println(e.getMessage());
			System.exit(1);
		} 
		catch (SQLException e) {
			System.out.println("Exception caught when trying to connect to database");
			System.out.println(e.getMessage());
			System.exit(1);
		}
		catch (ArrayIndexOutOfBoundsException e) {
			System.out.println("Exception caught when parsing input");
			System.out.println(e.getMessage());
		}

		return true;

	}



}