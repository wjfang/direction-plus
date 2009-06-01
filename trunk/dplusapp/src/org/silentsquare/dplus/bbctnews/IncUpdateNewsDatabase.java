package org.silentsquare.dplus.bbctnews;

import java.util.List;
import java.util.logging.Logger;

public class IncUpdateNewsDatabase extends LocalNewsDatabase {
	
	private static final Logger logger = Logger.getLogger(IncUpdateNewsDatabase.class.getName());
	
	private static final int FEEDS_PER_UPDATE = 10;
	
	private static final int NEWS_PER_UPDATE = 50;

	private enum State {START, READING_FEEDS, FINDING_COORDINATE, FINISH};
	
	private State state = State.START;
	
	/*
	 * The current to-be-processed index of the feeds or the current to-be-processed index of the news.
	 */
	private int index;  
	
	@Override
	synchronized public void update() {
		switch (state) {
			case START :
				newsReader.buildFeedList();
				state = State.READING_FEEDS;
				index = 0;
				logger.info("Retrieved the feed list");
				break;
			
			case READING_FEEDS :
				List<String> fl = newsReader.getFeedList();
				for (int i = 0; i < FEEDS_PER_UPDATE && index < fl.size(); i++) {
					newsReader.parse(fl.get(index++));
				}
				logger.info("Retrieved Feed No." + (index - 1));
				if (index == fl.size()) {
					state = State.FINDING_COORDINATE;
					index = 0;
				}
				break;
				
			case FINDING_COORDINATE :
				List<News> nl = newsReader.getNewsList();
				for (int i = 0; i < NEWS_PER_UPDATE && index < nl.size(); i++) {
					News news = nl.get(index++);
					newsReader.updateCoordinate(news);
				}
				logger.info("Found coordinate of News No." + (index - 1));
				if (index == nl.size()) {
					state = State.FINISH;
					index = 0;
				}
				break;
				
			case FINISH :
				this.newsList = newsReader.copyNewsList();
				newsReader.reset();
				state = State.START;
				index = 0;
				logger.info("Incremental update finished: " + this.newsList.size() + " news retrieved");
				break;
		}
	}

}
