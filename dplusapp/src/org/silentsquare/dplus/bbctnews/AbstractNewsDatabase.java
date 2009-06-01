package org.silentsquare.dplus.bbctnews;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Logger;

public abstract class AbstractNewsDatabase implements NewsDatabase {
	
	private static final Logger logger = Logger.getLogger(AbstractNewsDatabase.class.getName()); 
	
	@Override
	public abstract List<News> query(List<float[]> waypoints);
	
	protected Comparator<News> latitudeComparator = new Comparator<News>() {
		@Override
		public int compare(News n1, News n2) {
			float f = n1.getLatitude() - n2.getLatitude();
			if (f < 0)
				return -1;
			else if (f > 0)
				return 1;
			else // f == 0
				return 0;
		}
	};
	
	protected Comparator<News> longitudeComparator = new Comparator<News>() {
		@Override
		public int compare(News n1, News n2) {
			float f = n1.getLongitude() - n2.getLongitude();
			if (f < 0)
				return -1;
			else if (f > 0)
				return 1;
			else // f == 0
				return 0;
		}
	};
	
	protected static class Rectangle {
		float bottom;
		float top;
		float left;
		float right;
		
		Rectangle(float bottom, float top, float left, float right) {
			this.bottom = bottom;
			this.top = top;
			this.left = left;
			this.right = right;
		}
	}
	
	/**
	 * Query implementation for a News List.
	 * @param waypoints
	 * @param newslist a News List sorted by latitude.
	 * @return
	 */
	protected List<News> lookUpInList(List<float[]> waypoints, List<News> newslist) {
		if (waypoints == null || waypoints.size() < 2 || newslist == null) {
			throw new IllegalArgumentException(
					"waypoints must have at least two elements and newslist cannot be null.");
		}
		
		float bottom = 90;
		float top = -90;
		float left = 180;
		float right = -180;
		for (float[] p : waypoints) {
			if (p[0] < bottom) 
				bottom = p[0];
			if (p[0] > top) 
				top = p[0];
			if (p[1] < left)
				left = p[1];
			if (p[1] > right)
				right = p[1];
		}
		
		List<News> cadidates = lookUpInList(new Rectangle(bottom, top, left, right), newslist);
		/*
		 * Must be sorted before passed to loolUpInList().
		 */
		Collections.sort(cadidates, latitudeComparator);
		
		List<News> results = new ArrayList<News>();
		for (int i= 0, j = 1; j < waypoints.size(); i++, j++) {
			results.addAll(lookUpInList(waypoints.get(i), waypoints.get(j), cadidates));
		}
		return results;
	}
	
	/**
	 * 
	 * @param startpoint
	 * @param endpoint
	 * @param newslist sorted by latitude.
	 * @return
	 */
	private List<News> lookUpInList(float[] startpoint, float[] endpoint, List<News> newslist) {
		float bottom, top, left, right;
		if (startpoint[0] < endpoint[0]) {
			bottom = startpoint[0];
			top = endpoint[0];
		} else {
			bottom = endpoint[0];
			top = startpoint[0];
		}
		if (startpoint[1] < endpoint[1]) {
			left = startpoint[1];
			right = endpoint[1];
		} else {
			left = endpoint[1];
			right = startpoint[1];
		}
		
		return lookUpInList(new Rectangle(bottom, top, left, right), newslist);
	}
	
	/**
	 * 
	 * @param rec
	 * @param newslist sorted by latitude.
	 * @return
	 */
	private List<News> lookUpInList(Rectangle rec, List<News> newslist) {
		int i = Collections.binarySearch(newslist, new News(rec.bottom, 0), latitudeComparator);
		if (i < 0)
			i = -i - 1;
		
		int j = Collections.binarySearch(newslist, new News(rec.top, 0), latitudeComparator);
		if (j < 0)
			j = -j - 1;
		
		List<News> templist = new ArrayList<News>(newslist.subList(i, j));
		Collections.sort(templist, longitudeComparator);
		
		i = Collections.binarySearch(templist, new News(0, rec.left), longitudeComparator);
		if (i < 0)
			i = -i - 1;
		
		j = Collections.binarySearch(templist, new News(0, rec.right), longitudeComparator);
		if (j < 0)
			j = -j - 1;
		
		return templist.subList(i, j);
	}
}
