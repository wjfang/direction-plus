package org.silentsquare.dplus.bbctnews;

import java.util.Collections;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.log4j.Logger;

/**
 * In memory, persistent database.
 * @author wjfang
 *
 */
public class LocalNewsDatabase extends AbstractNewsDatabase {
	
	private static final Logger logger = Logger.getLogger(LocalNewsDatabase.class);

	private Timer timer = new Timer("Updating Travel News Database");
	
	private NewsReader newsReader;
	
	public LocalNewsDatabase(long updatePeriod, NewsReader newsReader) {
		this.newsReader = newsReader;
		
		timer.scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				updateDatabase();				
			}}, 
			0, updatePeriod * 1000);
	}
	
	private volatile List<News> newsList = Collections.EMPTY_LIST;
	
	@Override
	public List<News> query(List<float[]> waypoints) {
		List<News> nl = newsList;
		return lookUpInList(waypoints, nl);
	}

	private void updateDatabase() {
		logger.info("Start updating news database ...");
		List<News> nl = newsReader.read();
		/*
		 * Sort by latitude
		 */
		Collections.sort(nl, this.latitudeComparator);
		logger.info("Finish updating news database: " + nl.size() + "news retrieved.");
		newsList = nl;
	}

}
