package org.silentsquare.dplus.bbctnews;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.logging.Logger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class CoordinateFinder {
	
	private static final Logger logger = Logger.getLogger(CoordinateFinder.class.getName());
	
	public static final String ENDPOINT_URL_PREFIX = 
		"http://maps.google.com/maps/geo?output=json&oe=utf8&sensor=false&q=";
	
	private int optimalPause = 16; // in milliseconds
	
	private String apiKey;
	
	public String getApiKey() {
		return apiKey;
	}
	
	public void setApiKey(String apiKey) {
		this.apiKey = apiKey;
	}
	
	public CoordinateFinder() {
		
	}
	
	private String generateUrl(String searchString) {
		try {
			searchString = URLEncoder.encode(searchString, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			/*
			 * should not happen.
			 */
		}
		return ENDPOINT_URL_PREFIX + searchString + "&key=" + apiKey;
	}
	
	private JSONObject call(String searchString) {
		URLConnection uc;
		try {
			uc = new URL(generateUrl(searchString)).openConnection();
		} catch (MalformedURLException e) {
			logger.severe(e.getMessage());
			return null;
		} catch (IOException e) {
			logger.severe(e.getMessage());
			return null;
		}
        HttpURLConnection connection = (HttpURLConnection) uc;
        connection.setDoOutput(true);
        try {
			connection.setRequestMethod("GET");
		} catch (ProtocolException e) {
			logger.severe(e.getMessage());
			return null;
		}
        try {
			connection.connect();
		} catch (IOException e) {
			logger.severe(e.getMessage());
			return null;
		}
        
        InputStream inputStream = null;
        try {
            inputStream = connection.getInputStream();
        } catch (IOException e) {
            inputStream = connection.getErrorStream();
        }
        
        StringBuffer buffer = new StringBuffer();
        String line;
        BufferedReader rd = new BufferedReader(new InputStreamReader(inputStream));
        try {
			while ((line = rd.readLine()) != null) {
			    buffer.append(line);
			}
		} catch (IOException e) {
			logger.severe(e.getMessage());
			return null;
		}        
        
        int start = buffer.indexOf("{");
        int end = buffer.lastIndexOf("}") + 1;
        try {
			return new JSONObject(buffer.substring(start, end));
		} catch (JSONException e) {
			logger.severe(e + "\n" + buffer);
			return null;
		}
	}
	
	public Coordinate search(String location) {
		return search(location, 0);
	}
	
	private Coordinate search(String location, int pause) {
		if (pause > 0)
			pause(pause);
		
		JSONObject response = call(location);
		if (response == null)
			return null;
		logger.fine(response.toString());
		
		try {
			JSONObject status = response.getJSONObject("Status");
			int code = status.getInt("code");
			switch (code) {
				case 200:
					/*
					 * G_GEO_SUCCESS: No errors occurred; the address was successfully parsed 
					 * and its geocode was returned. 
					 */
					if (pause > 1)
						optimalPause = (pause + optimalPause) / 3;
					break;
					
				case 500: 
					/*
					 * G_GEO_SERVER_ERROR: A geocoding or directions request could not be 
					 * successfully processed, yet the exact reason for the failure is unknown.
					 */
					logger.warning(location + ": G_GEO_SERVER_ERROR");
					return null;
					
				case 601: 
					/*
					 * G_GEO_MISSING_QUERY: An empty address was specified in the HTTP q parameter.
					 */
					logger.warning(location + ": G_GEO_MISSING_QUERY");
					return null;
				
				case 602: 
					/*
					 * G_GEO_UNKNOWN_ADDRESS: No corresponding geographic location could be found 
					 * for the specified address, possibly because the address is relatively new, 
					 * or because it may be incorrect.
					 */
					logger.warning(location + ": G_GEO_UNKNOWN_ADDRESS");
					return null;
					
				case 603: 
					/*
					 * G_GEO_UNAVAILABLE_ADDRESS: The geocode for the given address or the route for 
					 * the given directions query cannot be returned due to legal or contractual reasons.
					 */
					logger.warning(location + ": G_GEO_UNAVAILABLE_ADDRESS");
					return null;
					
				case 610: 
					/*
					 * G_GEO_BAD_KEY: The given key is either invalid or does not 
					 * match the domain for which it was given.
					 */
					logger.warning(location + ": G_GEO_BAD_KEY");
					return null;
					
				case 620: 
					/*
					 * G_GEO_TOO_MANY_QUERIES: The given key has gone over the requests limit 
					 * in the 24 hour period or has submitted too many requests in too short 
					 * a period of time. If you're sending multiple requests in parallel or 
					 * in a tight loop, use a timer or pause in your code to make sure you don't 
					 * send the requests too quickly. 
					 */
					logger.warning(location + ": G_GEO_TOO_MANY_QUERIES: " + pause);
					if (pause <= 0)
						pause = optimalPause;
					else {
						pause *= 2;
					}
					return search(location, pause);
				
				default: 
					logger.warning(location + ": unknown code: " + code);
					return null;
			}
		} catch (JSONException e) {
			logger.severe(e + "\n" + location + "\n" + response);
			return null;
		}
		
		try {
			JSONArray placemark = response.getJSONArray("Placemark");
			JSONObject result = placemark.getJSONObject(0);
			JSONObject point = result.getJSONObject("Point");
			JSONArray coordinates = point.getJSONArray("coordinates");
			float lat = (float) coordinates.getDouble(1);
			float lng = (float) coordinates.getDouble(0);;
			logger.fine(lat + ", " + lng);
			return new Coordinate(lat, lng);
		} catch (JSONException e) {
			logger.severe(e + "\n" + location + "\n" + response);
			return null;
		}	
	}
	
	private void pause(int pause) {
		synchronized (this) {
			try {
				this.wait(pause);
			} catch (InterruptedException e) {
				logger.severe(e.getMessage());
			}	
		}		
	}

	public static class Coordinate {
		private float latitude;
		private float longitude;
		
		public Coordinate(float latitude, float longitude) {
			this.latitude = latitude;
			this.longitude = longitude;
		}
		
		public float getLatitude() {
			return latitude;
		}
		
		public float getLongitude() {
			return longitude;
		}
	}
	
	/**
	 * 
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		CoordinateFinder finder = new CoordinateFinder();
		Coordinate c;
//		c = finder.search("SO16 7PD");
//		c = finder.search("A379 Teignmouth");
//		c = finder.search("Southampton");
		c = finder.search("Portsmouth");
//		c = finder.search("Portland");
		System.out.println(c.getLatitude() + ", " + c.longitude);
	}
}
