package org.silentsquare.dplus.bbctnews;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.jdo.JDOHelper;
import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.jdo.Query;

import org.silentsquare.dplus.bbctnews.UpdateProcess.State;

public class GAENewsDatabase extends AbstractNewsDatabase {
	
	private static final Logger logger = Logger.getLogger(GAENewsDatabase.class.getName());
	
	private FeedListBuilder feedListBuilder;
	
	public FeedListBuilder getFeedListBuilder() {
		return feedListBuilder;
	}
	
	public void setFeedListBuilder(FeedListBuilder feedListBuilder) {
		this.feedListBuilder = feedListBuilder;
	}
	
	private FeedReader feedReader;
	
	public FeedReader getFeedReader() {
		return feedReader;
	}
	
	public void setFeedReader(FeedReader feedReader) {
		this.feedReader = feedReader;
	}
	
	private CoordinateFinder coordinateFinder;
	
	public CoordinateFinder getCoordinateFinder() {
		return coordinateFinder;
	}
	
	public void setCoordinateFinder(CoordinateFinder coordinateFinder) {
		this.coordinateFinder = coordinateFinder;
	}

	private int expectedUpdateWallTime;
	
	public void setExpectedUpdateWallTime(int expectedUpdateWallTime) {
		this.expectedUpdateWallTime = expectedUpdateWallTime;
	}
	
	public int getExpectedUpdateWallTime() {
		return expectedUpdateWallTime;
	}
	
	private String systemInfoId;
	
	public String getSystemInfoId() {
		return systemInfoId;
	}
	
	public void setSystemInfoId(String systemInfoId) {
		this.systemInfoId = systemInfoId;
	}
	
	private String updateProcessId;
	
	public String getUpdateProcessId() {
		return updateProcessId;
	}
	
	public void setUpdateProcessId(String updateProcessId) {
		this.updateProcessId = updateProcessId;
	}
	
	private PersistenceManagerFactory persistenceManagerFactory;
	
	public GAENewsDatabase(String persistenceManagerFactoryName) {
		persistenceManagerFactory =
			JDOHelper.getPersistenceManagerFactory(persistenceManagerFactoryName);
	}
	
	@Override
	public Map<String, List<StatusEntry>> monitor() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<News> query(List<float[]> waypoints) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void update() {
		long beginUpdate = System.currentTimeMillis();
		long beginDoUpdate = beginUpdate;
		long endDoUpdate = beginUpdate;
		
		int safeRatio = 4;
		while (endDoUpdate + (endDoUpdate - beginDoUpdate) * safeRatio < 
				beginUpdate + expectedUpdateWallTime * 1000) {
			beginDoUpdate = System.currentTimeMillis();
			PersistenceManager persistenceManager = persistenceManagerFactory.getPersistenceManager();
			try {
				doUpdate(persistenceManager);
			} finally {
				persistenceManager.close();
			}
			endDoUpdate = System.currentTimeMillis();
		}		
		
		logger.info("Update walltime: " + (System.currentTimeMillis() - beginUpdate));
	}

	private void doUpdate(PersistenceManager persistenceManager) {
		SystemInfo systemInfo = null;
		try {
			systemInfo = persistenceManager.getObjectById(SystemInfo.class, systemInfoId);
		} catch (JDOObjectNotFoundException e) {
			logger.warning("SystemInfo has not been created. This should mean the startup of the application.");
			initDataStore(persistenceManager);
			return;
		}
		
		UpdateProcess updateProcess = persistenceManager.getObjectById(UpdateProcess.class, updateProcessId);
		
		switch (updateProcess.getState()) {
			case INIT:
				doInit(persistenceManager, updateProcess, systemInfo);
				break;
			
			case BUILD_FEED_LIST:
				doBuildFeedList(persistenceManager, updateProcess, systemInfo);
				break;
			
			case READ_FEED:
				doReadFeed(persistenceManager, updateProcess, systemInfo);
				break;
				
			case FIND_COORDINATE:
				doFindCoordinate(persistenceManager, updateProcess, systemInfo);
				break;
				
			default:
				// This should never happen.
				logger.severe("Unknown update process state: " + updateProcess.getState());
		}
		
		persistenceManager.makePersistent(updateProcess);
		persistenceManager.makePersistent(systemInfo);
	}

	private void doFindCoordinate(PersistenceManager persistenceManager,
			UpdateProcess updateProcess, SystemInfo systemInfo) {
		// TODO Auto-generated method stub
		
	}

	// Read a feed, merge its news with the current news, calculates new news, 
	// put them in updateProcess.newsIdList.
	private void doReadFeed(PersistenceManager persistenceManager,
			UpdateProcess updateProcess, SystemInfo systemInfo) {
		if (testUpdateFinished(updateProcess))
			return;
		
		String url = updateProcess.getFeedList().get(updateProcess.getFeedIndex());
		List<News> nl = feedReader.read(url);
		
		if (nl.size() == 0) {
			updateProcess.setFeedIndex(updateProcess.getFeedIndex() + 1);
			logUpdateInfo(State.READ_FEED, url + " is empty");
			testUpdateFinished(updateProcess);
			return;
		}
		
		Collections.sort(nl, new Comparator<News>() {
			@Override
			public int compare(News n1, News n2) {
				if (n1.getLocation() == null)
					return -1;
				else
					return n1.getLocation().compareTo(n2.getLocation());
			}			
		});
		
		List<News> cl = getCurrentNews(persistenceManager, url);
		
		List<News> al = merge(nl, cl);
		// TODO
	}

	/**
	 * Use nl to update or retire news in cl and return a list of news in nl but not in cl
	 * @param nl the newly retrieved news list
	 * @param cl the current news list
	 * @return a list of news in nl but not in cl
	 */
	private List<News> merge(List<News> nl, List<News> cl) {
		int i = 0, j = 0;
		// TODO
		return null;
	}

	// Return the current valid news sorted by location asc and then degree desc.
	private List<News> getCurrentNews(PersistenceManager persistenceManager, String url) {
		Query query = persistenceManager.newQuery(News.class);
		query.setFilter("url == urlParam");
		query.setFilter("obsolete == false");
		query.declareParameters("String urlParam");
		query.setOrdering("location asc, degree desc");

		List<News> nl = null;
		try {
			 nl = (List<News>) query.execute(url);
		} finally {
			query.closeAll();
		}
		return nl;
	}

	private boolean testUpdateFinished(UpdateProcess updateProcess) {
		if (updateProcess.getFeedIndex() >= updateProcess.getFeedList().size()) {
			// Done all feeds
			updateProcess.setState(State.INIT);
			logUpdateInfo(updateProcess.getState(), "All feeds processed");
			return true;
		} else
			return false;
	}

	// Build feed list
	private void doBuildFeedList(PersistenceManager persistenceManager,
			UpdateProcess updateProcess, SystemInfo systemInfo) {
		List<String> feedList = feedListBuilder.build();
		updateProcess.setFeedList(feedList);
		updateProcess.setState(State.READ_FEED);
		logUpdateInfo(State.BUILD_FEED_LIST, "Found " + feedList.size() + " feeds");
	}

	// Reset updateProcess and create an updateStat
	private void doInit(PersistenceManager persistenceManager,
			UpdateProcess updateProcess, SystemInfo systemInfo) {
		UpdateStat updateStat = new UpdateStat();
		updateStat.setStartTime(System.currentTimeMillis());
		updateStat = persistenceManager.makePersistent(updateStat);
		logUpdateInfo(State.INIT, "Created a new updateStat");
		
		updateProcess.setUpdateStatId(updateStat.getId());
		updateProcess.setFeedList(Collections.EMPTY_LIST);
		updateProcess.setNewsIdList(Collections.EMPTY_LIST);
		updateProcess.setFeedIndex(0);
		updateProcess.setNewsIndex(0);
		updateProcess.setState(State.BUILD_FEED_LIST);
		logUpdateInfo(State.INIT, "Done");
	}

	// Create a singleton SystemInfo and a singleton UpdateProcess.
	private void initDataStore(PersistenceManager persistenceManager) {
		SystemInfo systemInfo = new SystemInfo();
		systemInfo.setId(systemInfoId);
		systemInfo.setStartUpTime(System.currentTimeMillis());
		logger.info("Created the systemInfo");
		
		UpdateProcess updateProcess = new UpdateProcess();
		updateProcess.setId(updateProcessId);
		logger.info("Created the updateProcess");
	}

	private void logUpdateInfo(State state, String message) {
		logger.info("Update Process " + state + ": " + message);
	}
}