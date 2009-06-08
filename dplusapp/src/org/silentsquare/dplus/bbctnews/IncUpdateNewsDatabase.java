package org.silentsquare.dplus.bbctnews;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Logger;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;

public class IncUpdateNewsDatabase extends LocalNewsDatabase {
	
	private static final Logger logger = Logger.getLogger(IncUpdateNewsDatabase.class.getName());
	
	private enum State {RESTORE, BUILD_FEED_LIST, READ_FEED, FIND_COORDINATE, FINISH, PERSIST};
	
	private State state = State.RESTORE;
	
	/*
	 * The current to-be-processed index of the feeds or the current to-be-processed index of the news.
	 */
	private int index;  
	
	private int expectedWallTime;
	
	public int getExpectedWallTime() {
		return expectedWallTime;
	}
	
	public void setExpectedWallTime(int expectedWallTime) {
		this.expectedWallTime = expectedWallTime;
	}
	
	private String persistenceManagerFactoryName;
	
	public String getPersistenceManagerFactoryName() {
		return persistenceManagerFactoryName;
	}
	
	public void setPersistenceManagerFactoryName(
			String persistenceManagerFactoryName) {
		this.persistenceManagerFactoryName = persistenceManagerFactoryName;
	}
	
	/**
	 * Status
	 */
	private long startUpTime;
	private long dbUpdatedTime;
	private long lastUpdateWallTime;

	private long beginRestoreTime;
	private long beginBuildFeedListTIme;
	private long beginReadFeedTime;
	private long beginFindCoordinateTime;
	private long beginFinishTime;
	private long beginPersistTime;
	private long endTime;
	
	public IncUpdateNewsDatabase() {
		startUpTime = System.currentTimeMillis();
	}
	
	private long beginUpdate;	
	private long beginDoUpdate;
	private long endDoUpdate;
	
	@Override
	synchronized public void update() {
		beginUpdate = System.currentTimeMillis();
		beginDoUpdate = beginUpdate;
		endDoUpdate = beginUpdate;
		
		while (hasEnoughTime()) {
			beginDoUpdate = System.currentTimeMillis();
			doUpdate();
			endDoUpdate = System.currentTimeMillis();
		}		
		
		lastUpdateWallTime = System.currentTimeMillis() - beginUpdate;
		logger.info("Walltime: " + lastUpdateWallTime + 
				"; Next: " + state + " " + index);
	}
	
	private boolean hasEnoughTime() {
		switch (state) {
			case RESTORE:
			case BUILD_FEED_LIST:
			case FINISH:
			case PERSIST:
				/*
				 * Only do this when there is 90% time available.
				 */
				if (endDoUpdate - beginUpdate < expectedWallTime * 1000 / 10)
					return true;
				else
					return false;				
				
			case READ_FEED:
				if (endDoUpdate + (endDoUpdate - beginDoUpdate) < beginUpdate + expectedWallTime * 1000)
					return true;
				else
					return false;
				
			case FIND_COORDINATE:
				if (endDoUpdate + (endDoUpdate - beginDoUpdate) * 4 < beginUpdate + expectedWallTime * 1000)
					return true;
				else
					return false;		
			
			default:
				return true;
		}
	}

	private PersistenceManager persistenceManager;
	
	private void doUpdate() {
		switch (state) {
			case RESTORE:
				this.beginRestoreTime = System.currentTimeMillis();
				PersistenceManagerFactory pmf =
					JDOHelper.getPersistenceManagerFactory(this.persistenceManagerFactoryName);
				persistenceManager = pmf.getPersistenceManager();
				// TODO restore news from data store if exists
				state = State.BUILD_FEED_LIST;
				logger.info("Restored news from data store");
				break;			
		
			case BUILD_FEED_LIST:
				this.beginBuildFeedListTIme = System.currentTimeMillis();
				newsReader.buildFeedList();
				state = State.READ_FEED;
				index = 0;
				logger.info("Retrieved the feed list");
				this.beginReadFeedTime = System.currentTimeMillis();
				break;
			
			case READ_FEED:
				List<String> fl = newsReader.getFeedList();
				if (index < fl.size()) {
					newsReader.parse(fl.get(index));
					index++;
				}
				logger.fine("Retrieved Feed No." + index);
				if (index == fl.size()) {
					state = State.FIND_COORDINATE;
					index = 0;
					this.beginFindCoordinateTime = System.currentTimeMillis();
				}
				break;
				
			case FIND_COORDINATE:
				List<News> nl = newsReader.getNewsList();
				if (index < nl.size()) {
					News news = nl.get(index);
					newsReader.updateCoordinate(news);
					index++;
				}
				logger.fine("Found coordinate of News No." + index);
				if (index == nl.size()) {
					state = State.FINISH;
					index = 0;
					this.beginFinishTime = System.currentTimeMillis();
				}
				break;
				
			case FINISH:
				this.newsList = newsReader.copyNewsList();
				this.dbUpdatedTime = System.currentTimeMillis();
				newsReader.reset();
				state = State.BUILD_FEED_LIST;
				index = 0;
				logger.info("Incremental update finished: " + this.newsList.size() + " news retrieved");
				break;
				
			case PERSIST:
				// TODO delete the previously saved news, and save news to data store
				logger.info("Persist ... ");
				break;
		}
	}

	@Override
	public Map<String, String> monitor() {
		Map<String, String> status = new HashMap<String, String>();
		
		status.put("The size of the news database in use", newsList.size() + "");
		status.put("The updated time of the news database in use", formatTime(this.dbUpdatedTime));
		
		status.put("The startup time of D+", formatTime(this.startUpTime));
		
		status.put("The current update state", state.toString());
		status.put("The current update index", index + "");
		status.put("The last update wall time", formatWallTime(lastUpdateWallTime));
		
		status.put("The wall time spent in building feed list", 
				formatWallTime(this.beginReadFeedTime - this.beginBuildFeedListTIme));
		status.put("The wall time spent in reading feed", 
				formatWallTime(this.beginFindCoordinateTime - this.beginReadFeedTime));
		status.put("The wall time spent in finding coordinate", 
				formatWallTime(this.beginFinishTime - this.beginFindCoordinateTime));
		
		status.put("The size of the news database being updated", newsReader.getNewsList().size() + "");
		
		return status;
	}

	private String formatWallTime(long t) {
		if (t < 0) 
			return "N/A";
		else
			return t + " ms";		
	}

	private DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.FULL, DateFormat.FULL, Locale.UK);
	
	private String formatTime(long time) {
		if (time == 0)
			return "N/A";
		
		Date date = new Date(time);
		return dateFormat.format(date);		
	}
}
