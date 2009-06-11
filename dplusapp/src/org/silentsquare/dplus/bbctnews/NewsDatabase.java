package org.silentsquare.dplus.bbctnews;

import java.util.List;

public interface NewsDatabase {
	
	/**
	 * 
	 * @param waypoints a list of points of latitude and longitude.
	 * @return
	 */
	public List<News> query(List<float[]> waypoints);
	
	/**
	 * Update news database.
	 */
	public void update();
	
	/**
	 * 
	 * @return the current status of the news database.
	 */
	public List<StatusPart> monitor();
	
	public static class StatusPart {
		public String title;
		public List<StatusEntry> content;
		
		public StatusPart(String t, List<StatusEntry> c) {
			this.title = t;
			this.content = c;
		}
	}
	
	public static class StatusEntry {
		public String key;
		public String value;
		
		public StatusEntry(String k, String v) {
			this.key = k;
			this.value = v;
		}
	}
}
