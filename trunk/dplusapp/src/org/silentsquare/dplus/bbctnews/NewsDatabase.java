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
	
}
