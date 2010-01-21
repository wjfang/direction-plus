package org.silentsquare.dplus.bbctnews;

import java.io.Serializable;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

@PersistenceCapable(identityType = IdentityType.APPLICATION)
public class News implements Serializable {
	
	private static final long serialVersionUID = 1293108909386690486L;

	public static final int VERY_SEVERE	= 6;
	public static final int SEVERE		= 5;
	public static final int MEDIUM		= 4;
	public static final int SLIGHT 		= 3;
	public static final int LOW_IMPACT	= 2;
	public static final int CLEARED		= 1;
	public static final int UNKNOWN		= 0;
	
	public static final float INITIAL_LATITUDE	= -90; // South Pole: no travel news will happen in South Pole. 
	public static final float INITIAL_LONGITUDE	= 180; // International Date Line: no travel news should happen there.
	
	@PrimaryKey
	@Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
	private Long id;
	
	/*
	 * url of the feed
	 */
	private String url;
	
	/*
	 * title, description and link directly come from the feed.
	 * String must be less than 500 characters. Otherwise have to use Text.
	 */
	private String title;
	private String description;
	private String link;	
	
	/*
	 * True if this news is obsolete. 
	 * obsolete is set to false when a news is created.
	 */
	private boolean obsolete;
	
	/*
	 * System.currentTimeMillis() when this news is created.
	 */
	private long createTime;
	
	/*
	 * System.currentTimeMillis() when this news is updated.
	 */
	private long updateTime; 
	
	/*
	 * location, degree, latitude, and longitude are calculated from title.
	 */
	private String location;
	private int degree;
	private float latitude		= INITIAL_LATITUDE; 
	private float longitude		= INITIAL_LONGITUDE;
	
	public News() {
		
	}

	News(float latitude, float longitude) {
		this.latitude = latitude;
		this.longitude = longitude;
	}
	
	public News(String url, String title, String description, String link) {
		this.url = url;
		this.title = title;
		this.description = description.length() >= 450 ? description.substring(0, 450) : description;
		this.link = link;
		this.createTime = System.currentTimeMillis();
		this.updateTime = this.createTime;
	}

	public Long getId() {
		return id;
	}
	
	public void setId(Long id) {
		this.id = id;
	}
	
	public String getUrl() {
		return url;
	}
	
	public void setUrl(String url) {
		this.url = url;
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
	
	public boolean isObsolete() {
		return obsolete;
	}
	
	public void setObsolete(boolean obsolete) {
		this.obsolete = obsolete;
	}
	
	public long getCreateTime() {
		return createTime;
	}
	
	public void setCreateTime(long createTime) {
		this.createTime = createTime;
	}
	
	public long getUpdateTime() {
		return updateTime;
	}
	
	public void setUpdateTime(long updateTime) {
		this.updateTime = updateTime;
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
				// TODO
		}
		
		location = title.substring(j + 1, k).trim();
	}

	@Override
	public String toString() {
		return "\n<news>\n" 
			+ "\t<url>" + this.url + "</url>\n"
			+ "\t<title>" + this.title + "</title>\n"
			+ "\t<description>" + this.description + "</description>\n"
			+ "\t<link>" + this.link + "</link>\n"
			+ "\t<location>" + this.location + "</location>\n"
			+ "\t<degree>" + this.degree + "</degree>\n"
			+ "\t<latitude>" + this.latitude + "</latitude>\n"
			+ "\t<longitude>" + this.longitude + "</longitude>\n"
			+ "\t<obsolete>" + this.obsolete + "</obsolete>\n"
			+ "\t<createTime>" + this.createTime + "</createTime>\n"
			+ "\t<updateTime>" + this.updateTime + "</updateTime>\n"			
			+ "</news>\n";			
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof News)) return false;
		News n = (News) obj;
		
		return (url == null ? n.url == null : url.equals(n.url)) 
			&& (title == null ? n.title == null : title.equals(n.title)) 
			&& (description == null ? n.description == null : description.equals(n.description))
			&& (link == null ? n.link == null : link.equals(n.link));		
	}
	
	private volatile int hashCode;
	
	@Override
	public int hashCode() {
		int result = hashCode;
		if (result == 0) {
			result = 17;
			result = 31 * result + url.hashCode();
			result = 31 * result + title.hashCode();
			result = 31 * result + description.hashCode();
			result = 31 * result + link.hashCode();
			hashCode = result;
		}
		return result;
	}
}
