package org.silentsquare.dplus.google;

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

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class CoordinateFinder {
	
	private static final Logger logger = Logger.getLogger(CoordinateFinder.class);
	
	public static final String ENDPOINT_URL_PREFIX = 
		"http://maps.google.com/maps/geo?output=json&oe=utf8&sensor=false&" +
		"key=ABQIAAAAj1DqbVODIwfxFozz52vGCBRPYLDdswWYK9xsZ4JkfD5EIBXtmBR4w43TwAtsEfHquR61hzgrmd2xQg&q=";
	
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
		return ENDPOINT_URL_PREFIX + searchString;
	}
	
	private JSONObject _search(String searchString) {
		URLConnection uc;
		try {
			uc = new URL(generateUrl(searchString)).openConnection();
		} catch (MalformedURLException e) {
			logger.error(e);
			return null;
		} catch (IOException e) {
			logger.error(e);
			return null;
		}
        HttpURLConnection connection = (HttpURLConnection) uc;
        connection.setDoOutput(true);
        try {
			connection.setRequestMethod("GET");
		} catch (ProtocolException e) {
			logger.error(e);
			return null;
		}
        try {
			connection.connect();
		} catch (IOException e) {
			logger.error(e);
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
			logger.error(e);
			return null;
		}        
        
        int start = buffer.indexOf("{");
        int end = buffer.lastIndexOf("}") + 1;
        try {
			return new JSONObject(buffer.substring(start, end));
		} catch (JSONException e) {
			logger.error(e + "\n" + buffer);
			return null;
		}
	}
	
	public Coordinate search(String searchString) {
		JSONObject response = _search(searchString);
		if (response == null)
			return null;
		logger.debug(response);
		
		try {
			JSONArray placemark = response.getJSONArray("Placemark");
			JSONObject result = placemark.getJSONObject(0);
			JSONObject point = result.getJSONObject("Point");
			JSONArray coordinates = point.getJSONArray("coordinates");
			float lat = (float) coordinates.getDouble(1);
			float lng = (float) coordinates.getDouble(0);;
			logger.info(lat + ", " + lng);
			return new Coordinate(lat, lng);
		} catch (JSONException e) {
			logger.error(e + "\n" + searchString + "\n" + response);
			return null;
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
//		new CoordinateFinder().search("SO16 7PD");
//		new CoordinateFinder().search("A379 Teignmouth");
		new CoordinateFinder().search("Southampton");
//		new CoordinateFinder().search("Portland");
	}

	/*
	 * {"responseData":{"viewport":{"center":{"lng":"-1.401712","lat":"50.938024"},
	 * "ne":{"lng":"-1.3985645","lat":"50.94117"},
	 * "sw":{"lng":"-1.4048594","lat":"50.934875"},
	 * "span":{"lng":"0.006295","lat":"0.006295"}},
	 * "cursor":{"moreResultsUrl":"http://www.google.com/local?oe=utf8&ie=utf8&num=4&mrt=yp%2Cloc&sll=37.779160%2C-122.420090&start=0&hl=en&q=SO16+7PD"},
	 * "results":[{"region":"","streetAddress":"Southampton SO16 7PD","titleNoFormatting":"Southampton SO16 7PD",
	 * "staticMapUrl":"http://mt.google.com/mapdata?cc=us&tstyp=5&Point=b&Point.latitude_e6=50938024&Point.longitude_e6=-1401712&Point.iconid=15&Point=e&w=150&h=100&zl=4",
	 * "listingType":"local","addressLines":["Southampton SO16 7PD","UK"],"lng":"-1.401712",
	 * "url":"http://www.google.com/maps?source=uds&q=SO16+7PD","country":"GB","city":"Southampton",
	 * "GsearchResultClass":"GlocalSearch","maxAge":604800,"addressLookupResult":"/maps","title":"Southampton SO16 7PD",
	 * "postalCode":"SO16","ddUrlToHere":"http://www.google.com/maps?source=uds&daddr=Southampton+SO16+7PD%2C+Southampton+%28Southampton+SO16+7PD%29+%4050.938024%2C-1.401712&iwstate1=dir%3Ato",
	 * "ddUrl":"http://www.google.com/maps?source=uds&daddr=Southampton+SO16+7PD%2C+Southampton+%28Southampton+SO16+7PD%29+%4050.938024%2C-1.401712&saddr",
	 * "ddUrlFromHere":"http://www.google.com/maps?source=uds&saddr=Southampton+SO16+7PD%2C+Southampton+%28Southampton+SO16+7PD%29+%4050.938024%2C-1.401712&iwstate1=dir%3Afrom",
	 * "accuracy":"5","lat":"50.938024"}]},"responseDetails":null,"responseStatus":200}
	 */
}
