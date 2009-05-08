package org.silentsquare.dplus.bbctnews;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.log4j.Logger;
import org.xml.sax.SAXException;

public abstract class AbstractNewsDatabase implements NewsDatabase {
	
	private static final Logger logger = Logger.getLogger(AbstractNewsDatabase.class); 

	public static final long DATABASE_UPDATE_PERIOD = 3600 * 1000; // one hour in ms 
		
	private NewsReader newsReader;
	
	private Timer timer = new Timer("Updating Travel News Database");
	
	protected AbstractNewsDatabase() {
		try {
			this.newsReader = new NewsReader();
		} catch (SAXException e) {
			/*
			 * Should not happen.
			 */
			logger.fatal(e);
		}
		
		timer.scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				updateDatabase();				
			}}, 
			0, DATABASE_UPDATE_PERIOD);
	}
	
	@Override
	public abstract List<News> query(float bottom, float top, float left, float right);
	
	protected final List<News> readNews() {
		return newsReader.read();
	}
	
	protected abstract void updateDatabase();
}
