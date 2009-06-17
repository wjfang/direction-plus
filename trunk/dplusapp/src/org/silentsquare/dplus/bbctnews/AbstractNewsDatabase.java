package org.silentsquare.dplus.bbctnews;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Logger;

import org.silentsquare.dplus.bbctnews.Route.LatLng;
import org.silentsquare.dplus.bbctnews.Route.Step;

public abstract class AbstractNewsDatabase implements NewsDatabase {
	
	private static final Logger logger = Logger.getLogger(AbstractNewsDatabase.class.getName()); 
	
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
	 * @param waypoints must have more than 2 elements.
	 * @param newslist a News List sorted by latitude.
	 * @return
	 */
	protected List<News> lookUpInList(List<float[]> waypoints, List<News> newslist) {		
		List<News> cadidates = lookUpInList(calculateRectangle(waypoints), newslist);
		
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
	
	protected Rectangle calculateRectangle(List<float[]> waypoints) {
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
		
		return new Rectangle(bottom, top, left, right);
	}
	
	protected List<News> lookUpInList(Route route, List<News> newslist) {	
		List<News> cadidates = lookUpInList(calculateRectangle(route), newslist);
		
		/*
		 * Must be sorted before passed to loolUpInList().
		 */
		Collections.sort(cadidates, latitudeComparator);
		
		List<News> results = new ArrayList<News>();
		List<Step> steplist = route.getStepList();
		for (int i = 0; i < steplist.size(); i++) {
			Step step = steplist.get(i);
			float[] start = new float[]{step.getStart().getLatitude(), step.getStart().getLongitude()};
			float[] end;
			if (i < steplist.size() - 1) {
				Step step2 = steplist.get(i + 1);
				end = new float[]{step2.getStart().getLatitude(), step2.getStart().getLongitude()};
			} else {
				end = new float[]{route.getEnd().getLatitude(), route.getEnd().getLongitude()};
			}
			List<News> nl = lookUpInList(start, end, cadidates);
			if (step.isLongEnough() && !"".equals(step.guessRoad())) {
				List<News> nl2 = new ArrayList<News>();
				String road = step.guessRoad();
				road = road.replace('(', '.');
				int j = road.indexOf(')');
				if (j != -1)
					road = road.substring(0, j);
				String pattern = ".*\\b" + road + "\\b.*";
				for (News news : nl) {
					if (news.getTitle().matches(pattern)) {
						nl2.add(news);
						logger.info(news.getTitle() + " matches " + step.guessRoad());
					}
				}
				nl = nl2;
			}
			results.addAll(nl);
		}
		
		return results;
	}
	
	protected Rectangle calculateRectangle(Route route) {
		Rectangle rec = new Rectangle(90, -90, 180, -180);
		
		for (Step step : route.getStepList()) {
			LatLng ll = step.getStart();
			calculateRectangle(rec, ll);
		}
		
		LatLng ll = route.getEnd();
		calculateRectangle(rec, ll);
		
		return rec;
	}
	
	private void calculateRectangle(Rectangle rec, LatLng ll) {
		if (ll.getLatitude() < rec.bottom) 
			rec.bottom = ll.getLatitude();
		if (ll.getLatitude() > rec.top) 
			rec.top = ll.getLatitude();
		if (ll.getLongitude() < rec.left)
			rec.left = ll.getLongitude();
		if (ll.getLongitude() > rec.right)
			rec.right = ll.getLongitude();		
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
