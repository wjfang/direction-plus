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

	private String updateKey;
	
	public String getUpdateKey() {
		return updateKey;
	}
	
	public void setUpdateKey(String updateKey) {
		this.updateKey = updateKey;
	}
}
