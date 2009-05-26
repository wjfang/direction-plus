package org.silentsquare.dplus;

import org.silentsquare.dplus.bbctnews.NewsDatabase;

public class Configuration {
	
	private NewsDatabase newsDatabase;
	
	public NewsDatabase getNewsDatabase() {
		return newsDatabase;
	}
	
	public void setNewsDatabase(NewsDatabase newsDatabase) {
		this.newsDatabase = newsDatabase;
	}

}
