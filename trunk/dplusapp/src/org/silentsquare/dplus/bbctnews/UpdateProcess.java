package org.silentsquare.dplus.bbctnews;

import java.util.Collections;
import java.util.List;

import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.PrimaryKey;

@PersistenceCapable(identityType = IdentityType.APPLICATION)
public class UpdateProcess {
	
	@PrimaryKey
    private String id;

	public static enum State {INIT, BUILD_FEED_LIST, READ_FEED, FIND_COORDINATE};
	
	private State state = State.INIT;
	
	/**
	 * The list of all feeds.
	 */
	private List<String> feedList = Collections.EMPTY_LIST;
	
	/**
	 * The list of IDs of all news JDOs belonging to the same feed currently working on.
	 */
	private List<Long> newsIdList = Collections.EMPTY_LIST;
	
	/**
	 * The current to-be-processed index into the feed list.
	 */
	private int feedIndex;

	/**
	 * The current to-be-processed index into the news id list.
	 */
	private int newsIndex;
	
	/**
	 * The ID of current UpdateStat JDO.
	 */
	private Long updateStatId;
	
	public final String getId() {
		return id;
	}
	
	public final void setId(String id) {
		this.id = id;
	}
	
	public final State getState() {
		return state;
	}
	
	public final void setState(State state) {
		this.state = state;
	}

	public final List<String> getFeedList() {
		return feedList;
	}

	public final void setFeedList(List<String> feedList) {
		this.feedList = feedList;
	}

	public final List<Long> getNewsIdList() {
		return newsIdList;
	}

	public final void setNewsIdList(List<Long> newsIdList) {
		this.newsIdList = newsIdList;
	}

	public final int getFeedIndex() {
		return feedIndex;
	}

	public final void setFeedIndex(int feedIndex) {
		this.feedIndex = feedIndex;
	}

	public final int getNewsIndex() {
		return newsIndex;
	}

	public final void setNewsIndex(int newsIndex) {
		this.newsIndex = newsIndex;
	}

	public final Long getUpdateStatId() {
		return updateStatId;
	}

	public final void setUpdateStatId(Long updateStatId) {
		this.updateStatId = updateStatId;
	}
	
}
