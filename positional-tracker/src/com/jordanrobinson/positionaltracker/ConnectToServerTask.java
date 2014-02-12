package com.jordanrobinson.positionaltracker;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import android.os.AsyncTask;
import android.util.Log;

public class ConnectToServerTask extends AsyncTask<String, Void, Void> {
	
	

	@Override
	protected Void doInBackground(String... params) {
		
		try { //setup connection to server
			
			if (MainActivity.serverOut == null && MainActivity.serverIn == null) {
				Socket socket = new Socket(MainActivity.SERVER_IP, 30303);
				MainActivity.serverOut = new PrintWriter(socket.getOutputStream(), true);
				MainActivity.serverIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				
				MainActivity.serverOut.println("hello");
			}
			
			
		} catch (IOException e) {
			Log.e("Server Connection", "Could not connect to server");
			MainActivity.serverIn = null;
			MainActivity.serverOut = null;
			e.printStackTrace();
		}
		
		return null;
	}
	
}
