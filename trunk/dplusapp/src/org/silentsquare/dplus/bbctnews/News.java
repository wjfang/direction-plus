package org.silentsquare.dplus.bbctnews;

import java.io.Serializable;

import org.apache.log4j.Logger;

public class News implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1293108909386690486L;

	private static final Logger logger = Logger.getLogger(News.class);
	
	public static final int VERY_SEVERE	= 6;
	public static final int SEVERE		= 5;
	public static final int MEDIUM		= 4;
	public static final int SLIGHT 		= 3;
	public static final int LOW_IMPACT	= 2;
	public static final int CLEARED		= 1;
	public static final int UNKNOWN		= 0;
	
	private String title;
	private String description;
	private String link;
	
	private String location;
	private int degree;
	private float latitude		= -90; // South Pole: no travel news will happen in South Pole. 
	private float longitude		= 180; // International Date Line: no travel news should happen there.
	
	public News() {
		
	}

	public News(float latitude, float longitude) {
		this.latitude = latitude;
		this.longitude = longitude;
	}
	
	public News(String title, String description, String link) {
		this.title = title;
		this.description = description;
		this.link = link;
	}

	public final String getTitle() {
		return title;
	}

	public final void setTitle(String title) {
		this.title = title;		
	}

	public final String getDescription() {
		return description;
	}

	public final void setDescription(String description) {
		this.description = description;
	}

	public final String getLink() {
		return link;
	}

	public final void setLink(String link) {
		this.link = link;
	}
	
	public final String getLocation() {
		return location;
	}

	public final void setLocation(String location) {
		this.location = location;
	}

	public final int getDegree() {
		return degree;
	}

	public final void setDegree(int degree) {
		this.degree = degree;
	}

	public final float getLatitude() {
		return latitude;
	}

	public final void setLatitude(float latitude) {
		this.latitude = latitude;
	}

	public final float getLongitude() {
		return longitude;
	}

	public final void setLongitude(float longitude) {
		this.longitude = longitude;
	}
	
	public final void parseTitle() {
		int i = title.indexOf('[');
		int j = title.indexOf(']');
		int k = title.indexOf(',');
		
		if (i == -1 || j == -1 || k == -1)
			return;
		
		String s = title.substring(i + 1, j);
		
		/*
		 * Very Severe
		 * Severe
		 * Medium
		 * Slight
		 * Low Impact
		 * Cleared
		 */
		char c = s.charAt(2);
		switch (c) {
			case 'r':
				degree = VERY_SEVERE;
				break;
			
			case 'v':
				degree = SEVERE;
				break;
				
			case 'd':
				degree = MEDIUM;
				break;
			
			case 'i':
				degree = SLIGHT;
				break;
			
			case 'w':
				degree = LOW_IMPACT;
				break;
				
			case 'e':
				degree = CLEARED;
				break;
			
			default:
				logger.error(s);
		}
		
		location = title.substring(j + 1, k).trim();
		logger.debug("degree = " + degree + ", location = " + location + ", title = " + title);
	}

	@Override
	public String toString() {
		return "\n<news>\n" 
			+ "\t<title>" + this.title + "</title>\n"
			+ "\t<description>" + this.description + "</description>\n"
			+ "\t<link>" + this.link + "</link>\n"
			+ "\t<location>" + this.location + "</location>\n"
			+ "\t<degree>" + this.degree + "</degree>\n"
			+ "\t<latitude>" + this.latitude + "</latitude>\n"
			+ "\t<longitude>" + this.longitude + "</longitude>\n"
			+ "</news>\n";			
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof News)) return false;
		News n = (News) obj;
		
		return (title == null ? n.title == null : title.equals(n.title)) 
			&& (description == null ? n.description == null : description.equals(n.description))
			&& (link == null ? n.link == null : link.equals(n.link))
			&& (location == null ? n.location == null : location.equals(n.location))
			&& degree == n.degree
			&& latitude == n.latitude
			&& longitude == n.longitude;		
	}
	
	private volatile int hashCode;
	
	@Override
	public int hashCode() {
		int result = hashCode;
		if (result == 0) {
			result = 17;
			result = 31 * result + title.hashCode();
			result = 31 * result + Float.floatToIntBits(latitude);
			result = 31 * result + Float.floatToIntBits(longitude);
			hashCode = result;
		}
		return result;
	}
}
