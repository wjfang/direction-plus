package org.silentsquare.dplus.bbctnews;

import java.util.List;
import java.util.logging.Logger;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

public class IncUpdateNewsDatabase extends LocalNewsDatabase {
	
	private static final Logger logger = Logger.getLogger(IncUpdateNewsDatabase.class.getName());
	
	private enum State {RESTORE, BUILD_FEED_LIST, READ_FEED, FIND_COORDINATE, FINISH_AND_SAVE};
	
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
	
	private String persistenceUnit;
	
	public String getPersistenceUnit() {
		return persistenceUnit;
	}
	
	public void setPersistenceUnit(String persistenceUnit) {
		this.persistenceUnit = persistenceUnit;
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
		
		logger.info("Walltime: " + (System.currentTimeMillis() - beginUpdate) + 
				"; Next: " + state + " " + index);
	}
	
	private boolean hasEnoughTime() {
		if (endDoUpdate + (endDoUpdate - beginDoUpdate) < beginUpdate + expectedWallTime * 1000)
			return true;
		else
			return false;
	}

	private EntityManager entityManager;
	
	private void doUpdate() {
		switch (state) {
			case RESTORE:
				EntityManagerFactory emf = Persistence.createEntityManagerFactory(persistenceUnit);
				entityManager = emf.createEntityManager();
				// TODO restore news from data store if exists
				state = State.BUILD_FEED_LIST;
				logger.info("Restored news from data store");
				break;			
		
			case BUILD_FEED_LIST:
				newsReader.buildFeedList();
				state = State.READ_FEED;
				index = 0;
				logger.info("Retrieved the feed list");
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
					state = State.FINISH_AND_SAVE;
					index = 0;
				}
				break;
				
			case FINISH_AND_SAVE:
				this.newsList = newsReader.copyNewsList();
				newsReader.reset();
				state = State.BUILD_FEED_LIST;
				index = 0;
				// TODO delete the previously saved news, and save news to data store
				logger.info("Incremental update finished: " + this.newsList.size() + " news retrieved");
				break;
		}
	}

}
