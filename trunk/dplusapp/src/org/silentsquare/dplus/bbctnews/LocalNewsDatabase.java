package org.silentsquare.dplus.bbctnews;

import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;

/**
 * In memory, persistent database.
 * @author wjfang
 *
 */
public class LocalNewsDatabase extends AbstractNewsDatabase {
	
	private static final Logger logger = Logger.getLogger(LocalNewsDatabase.class);

	private NewsReader newsReader;
	
	public LocalNewsDatabase(long updatePeriod, NewsReader newsReader) {
		this.newsReader = newsReader;
		updateDatabase();
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
		logger.info("Finish updating news database: " + nl.size() + "news retrieved.");
		newsList = nl;
	}

}
