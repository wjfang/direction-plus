package org.silentsquare.dplus.bbctnews;

import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

/**
 * In memory, persistent database.
 * @author wjfang
 *
 */
public class LocalNewsDatabase extends AbstractNewsDatabase {
	
	private static final Logger logger = Logger.getLogger(LocalNewsDatabase.class.getName());

	protected NewsReader newsReader;
	
	public NewsReader getNewsReader() {
		return newsReader;
	}
	
	public void setNewsReader(NewsReader newsReader) {
		this.newsReader = newsReader;
	}
	
	public LocalNewsDatabase() {
		
	}
	
	protected volatile List<News> newsList = Collections.EMPTY_LIST;
	
	@Override
	public List<News> query(List<float[]> waypoints) {
		List<News> nl = newsList;
		return lookUpInList(waypoints, nl);
	}

	private volatile boolean updating;
	
	@Override
	public void update() {
		if (updating)
			return;
		
		updating = true;
		logger.info("Start updating news database ...");
		List<News> nl = newsReader.read();
		logger.info("Finish updating news database: " + nl.size() + " news retrieved.");
		newsList = nl;
		updating = false;
	}

}
