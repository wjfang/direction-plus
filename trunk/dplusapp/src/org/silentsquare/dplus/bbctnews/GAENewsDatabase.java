package org.silentsquare.dplus.bbctnews;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.logging.Logger;

import javax.jdo.JDOHelper;
import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.jdo.Query;

import org.silentsquare.dplus.bbctnews.CoordinateFinder.Coordinate;
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
	public List<StatusPart> monitor() {
		long begin = System.currentTimeMillis();
		List<StatusPart> status = new ArrayList<StatusPart>();
		/*
		 * Always check for SystemInfo first.
		 */
		status.add(new StatusPart("System Information", getSystemStatus()));
		status.add(new StatusPart("Current Update Process", getUpdateProcessStatus()));
		status.add(new StatusPart("Last Update Process", getLastUpdateStat()));
		
		logger.info("Monitor walltime: " + (System.currentTimeMillis() - begin) + " ms");	
		return status;
	}

	private List<StatusEntry> getLastUpdateStat() {
		PersistenceManager persistenceManager = persistenceManagerFactory.getPersistenceManager();
		try {
			Query query = persistenceManager.newQuery(UpdateStat.class);
		    query.setOrdering("startTime desc");
		    query.setRange(0, 2);
		    List<UpdateStat> statlist = Collections.EMPTY_LIST;
		    try {
		        statlist = (List<UpdateStat>) query.execute();
		        if (statlist.size() >= 2)
		        	return formatUpdateStat(statlist.get(1));
		        else
		        	return Collections.EMPTY_LIST;
		    } finally {
		        query.closeAll();
		    }
		} finally {
			persistenceManager.close();
		}
	}

	private List<StatusEntry> getUpdateProcessStatus() {
		List<StatusEntry> statusList = new ArrayList<StatusEntry>();
		
		PersistenceManager persistenceManager = persistenceManagerFactory.getPersistenceManager();
		try {
			UpdateProcess updateProcess = persistenceManager.getObjectById(UpdateProcess.class, updateProcessId);
			statusList.add(new StatusEntry("State", updateProcess.getState().toString()));
			statusList.add(new StatusEntry("Feed Index", updateProcess.getFeedIndex() + ""));
			statusList.add(new StatusEntry("News Index", updateProcess.getNewsIndex() + ""));
			statusList.add(new StatusEntry("Feed In Process", 
					updateProcess.getFeedIndex() == 0 ?
							updateProcess.getFeedList().get(0) :
								updateProcess.getFeedList().get(updateProcess.getFeedIndex() - 1)));
			
			UpdateStat updateStat = persistenceManager.getObjectById(
					UpdateStat.class, updateProcess.getUpdateStatId());
			statusList.addAll(formatUpdateStat(updateStat));			
		} finally {
			persistenceManager.close();
		}
		
		return statusList;
	}
	
	private List<StatusEntry> formatUpdateStat(UpdateStat updateStat) {
		List<StatusEntry> statusList = new ArrayList<StatusEntry>();
		statusList.add(new StatusEntry("Start Time", formatTime(updateStat.getStartTime())));
		statusList.add(new StatusEntry("Finish Time", formatTime(updateStat.getFinishTime())));
		statusList.add(new StatusEntry("Retrieved News", updateStat.getTotalNum() + ""));
		statusList.add(new StatusEntry("New News", updateStat.getNewNum() + ""));
		statusList.add(new StatusEntry("Coordinate Cache Hits", updateStat.getCoordinateHitNum() + ""));
		return statusList;
	}

	private List<StatusEntry> getSystemStatus() {
		List<StatusEntry> statusList = new ArrayList<StatusEntry>();
		
		PersistenceManager persistenceManager = persistenceManagerFactory.getPersistenceManager();
		try {
			SystemInfo systemInfo = getSystemInfo(persistenceManager);
			statusList.add(new StatusEntry("Startup Time", formatTime(systemInfo.getStartUpTime())));
			statusList.add(new StatusEntry("Current Time", formatTime(System.currentTimeMillis())));
			statusList.add(new StatusEntry("Completed Updates", systemInfo.getUpdateNum() + ""));
			statusList.add(new StatusEntry("Average Update Time (sec)", 
					systemInfo.getUpdateNum() == 0 ? "N/A" :
						(System.currentTimeMillis() - systemInfo.getStartUpTime()) / 
						systemInfo.getUpdateNum() / 1000 + ""));
		} finally {
			persistenceManager.close();
		}
		
		return statusList;
	}
	
	private DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.FULL, DateFormat.FULL, Locale.UK);
	
	private String formatTime(long time) {
		if (time == 0)
			return "N/A";
		
		Date date = new Date(time);
		return dateFormat.format(date);		
	}

	@Override
	public List<News> query(List<float[]> waypoints) {
		long begin = System.currentTimeMillis();
		
		if (waypoints == null || waypoints.size() < 2) {
			logger.severe("There is less than 2 waypoints. Return an empty list.");
			return Collections.EMPTY_LIST;
		}
		
		Rectangle rec = calculateRectangle(waypoints);
		
		List<News> nl = findNewsByLatRange(rec.bottom, rec.top);
		logger.info("Query walltime: " + (System.currentTimeMillis() - begin) + " ms");	
		return lookUpInList(waypoints, nl);
	}

	@Override
	public List<News> query(Route route) {
		// TODO Auto-generated method stub
		return null;
	}
	
	private List<News> findNewsByLatRange(float bottom, float top) {
		List<News> nl = Collections.EMPTY_LIST;
		PersistenceManager persistenceManager = persistenceManagerFactory.getPersistenceManager();
		try {
			Query query = persistenceManager.newQuery(News.class);
			query.setFilter("obsolete == false && latitude >= bottom && latitude <= top");
			query.declareParameters("float bottom, float top");
			query.setOrdering("latitude asc");

			try {
				 nl = (List<News>) query.execute(bottom, top);
			} finally {
				query.closeAll();
			}
		} finally {
			persistenceManager.close();
		}
		logger.info("Found " + nl.size() + " news relevant to the input waypoints.");
		return nl;
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
		
		logger.info("Update walltime: " + (System.currentTimeMillis() - beginUpdate) + " ms");
	}

	private void doUpdate(PersistenceManager persistenceManager) {
		/*
		 * Always check for SystemInfo first.
		 */
		SystemInfo systemInfo = getSystemInfo(persistenceManager);		
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
		
		persistenceManager.makePersistent(systemInfo);
		persistenceManager.makePersistent(updateProcess);
	}

	private SystemInfo getSystemInfo(PersistenceManager persistenceManager) {
		SystemInfo systemInfo = null;
		try {
			systemInfo = persistenceManager.getObjectById(SystemInfo.class, systemInfoId);
		} catch (JDOObjectNotFoundException e) {
			logger.warning("SystemInfo has not been created. This should mean the startup of the application.");
			initDataStore(persistenceManager);
			systemInfo = persistenceManager.getObjectById(SystemInfo.class, systemInfoId);
		}
		return systemInfo;
	}

	private void doFindCoordinate(PersistenceManager persistenceManager,
			UpdateProcess updateProcess, SystemInfo systemInfo) {
		if (testFeedFinished(updateProcess))
			return;
		
		Long id = updateProcess.getNewsIdList().get(updateProcess.getNewsIndex());
		
		/*
		 * Sometimes the IDed news may not be written to the data store due to exceptions happening
		 * in the previous update operations, such as com.google.apphosting.api.DeadlineExceededException
		 * or com.google.appengine.api.datastore.DatastoreTimeoutException, so getObjectById() may throw out
		 * a JDOObjectNotFoundException that prevents the update process continuing.
		 */
		try {
			News news = persistenceManager.getObjectById(News.class, id);
			
			String location = news.getLocation();
			
			UpdateStat updateStat = persistenceManager.getObjectById(
					UpdateStat.class, updateProcess.getUpdateStatId());
			
			/*
			 * First check if there is an obsolete news with the same location;
			 * if failed, use CoordinateFinder.
			 */		
			Coordinate co = findCachedCoordinate(persistenceManager, location);
			if (co == null) {
				co = coordinateFinder.search(location);
			} else {
				// Found a cached coordinate
				updateStat.setCoordinateHitNum(updateStat.getCoordinateHitNum() + 1);
				persistenceManager.makePersistent(updateStat);
			}
			
			if (co != null) {
				news.setLatitude(co.getLatitude());
				news.setLongitude(co.getLongitude());
				persistenceManager.makePersistent(news);
			} else {
				// Can not find coordinate, no need to keep this news.
				persistenceManager.deletePersistent(news);
			}
			
		} catch (JDOObjectNotFoundException e) {
			logger.severe(e.getLocalizedMessage());
		} catch (Exception e) {
			logger.severe(e.getLocalizedMessage());
		} finally {
			updateProcess.setNewsIndex(updateProcess.getNewsIndex() + 1);
		}
	}

	private Coordinate findCachedCoordinate(PersistenceManager persistenceManager, String location) {
		Query query = persistenceManager.newQuery(News.class);
		query.setFilter("obsolete == true && location == locParam");
		query.declareParameters("String locParam");

		List<News> nl = null;
		try {
			 nl = (List<News>) query.execute(location);
		} finally {
			query.closeAll();
		}
		
		Coordinate co = null;
		for (News n : nl) {
			if (n.getLatitude() != News.INITIAL_LATITUDE && n.getLongitude() != News.INITIAL_LONGITUDE) {
				co = new Coordinate(n.getLatitude(), n.getLongitude());
				break;
			}
		}

		// Delete all obsolete news of the location because now there will be 
		// an active news of the location.
		for (News n : nl) {
			persistenceManager.deletePersistent(n);
		}
		
		return co;
	}

	private boolean testFeedFinished(UpdateProcess updateProcess) {
		if (updateProcess.getNewsIdList() == null || 
				updateProcess.getNewsIndex() >= updateProcess.getNewsIdList().size()) {
			/*
			 * I suspect when a JDO is retrieved from the data store, an empty list will be
			 * represented by a null.
			 */
			// Done all new news in the feed
			logUpdateInfo(updateProcess.getState(), "Found coordinates for all new news");
			updateProcess.setState(State.READ_FEED);
			return true;
		} else
			return false;
	}

	// Read a feed, merge its news with the current news, calculates new news, 
	// put them in updateProcess.newsIdList.
	private void doReadFeed(PersistenceManager persistenceManager,
			UpdateProcess updateProcess, SystemInfo systemInfo) {
		if (testUpdateFinished(updateProcess, systemInfo))
			return;
		
		String url = updateProcess.getFeedList().get(updateProcess.getFeedIndex());
		
		List<News> nl = feedReader.read(url);
		
		if (nl.size() == 0) {
			updateProcess.setFeedIndex(updateProcess.getFeedIndex() + 1);
			logUpdateInfo(State.READ_FEED, url + " is empty");
			return;
		}
		
		Collections.sort(nl, new Comparator<News>() {
			@Override
			public int compare(News n1, News n2) {
				// It is guaranteed location is not null. see FeedReader.java.
				return n1.getLocation().compareTo(n2.getLocation());
			}			
		});
		
		List<News> cl = findCurrentNews(persistenceManager, url);
		List<News> al = merge(nl, cl);
		persistenceManager.makePersistentAll(cl);
		al = (List<News>) persistenceManager.makePersistentAll(al);
		
		List<Long> il = new ArrayList<Long>();
		for (News an : al) {
			il.add(an.getId());
		}
		
		updateProcess.setFeedIndex(updateProcess.getFeedIndex() + 1);
		updateProcess.setNewsIdList(il);
		updateProcess.setNewsIndex(0);
		updateProcess.setState(State.FIND_COORDINATE);
		
		UpdateStat updateStat = persistenceManager.getObjectById(
				UpdateStat.class, updateProcess.getUpdateStatId());
		updateStat.setTotalNum(updateStat.getTotalNum() + nl.size());
		updateStat.setNewNum(updateStat.getNewNum() + al.size());
		persistenceManager.makePersistent(updateStat);
		
		logUpdateInfo(State.READ_FEED, nl.size() + " read, " + al.size() + " created, " + cl.size() + " updated");
	}

	/**
	 * Use nl to update or retire news in cl and return a list of news in nl but not in cl
	 * @param nl the newly retrieved news list
	 * @param cl the current news list
	 * @return a list of news in nl but not in cl
	 */
	private List<News> merge(List<News> nl, List<News> cl) {
		List<News> al = new ArrayList<News>();
		int i = 0, j = 0;
		while (i < nl.size() && j < cl.size()) {
			News nn = nl.get(i);
			News cn = cl.get(j);
			int k = nn.getLocation().compareTo(cn.getLocation());
			if (k < 0) {
				// nn is a brand new news, add it to al
				al.add(nn);
				i++;
			} else if (k == 0) {
				// update cn with nn
				updateNews(nn, cn);
				i++;
				j++;
			} else { // k > 0
				// cn is obsolet
				cn.setObsolete(true);
				j++;
			}
		}
		
		if (i == nl.size()) {
			// make the remaining news in cl obsolete
			for (; j < cl.size(); j++) {
				cl.get(j).setObsolete(true);
			}
		}
		
		if (j == cl.size()) {
			// add the remaining news in nl to al
			for (; i < nl.size(); i++) {
				al.add(nl.get(i));
			}
		}
		
		return al;
	}

	/**
	 * Use the information in src to update dst
	 * @param src
	 * @param dst
	 */
	private void updateNews(News src, News dst) {
		dst.setTitle(src.getTitle());
		dst.setDescription(src.getDescription());
		dst.setLink(src.getLink());
		dst.setLocation(src.getLocation());
		dst.setDegree(src.getDegree());
		dst.setUpdateTime(System.currentTimeMillis());
	}

	// Return the current valid news sorted by location asc and then degree desc.
	private List<News> findCurrentNews(PersistenceManager persistenceManager, String url) {
		Query query = persistenceManager.newQuery(News.class);
		query.setFilter("obsolete == false && url == urlParam");
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

	private boolean testUpdateFinished(UpdateProcess updateProcess, SystemInfo systemInfo) {
		if (updateProcess.getFeedList() == null || 
				updateProcess.getFeedIndex() >= updateProcess.getFeedList().size()) {
			// Done all feeds
			logUpdateInfo(updateProcess.getState(), "All feeds processed");
			updateProcess.setState(State.INIT);
			systemInfo.setUpdateNum(systemInfo.getUpdateNum() + 1);
			return true;
		} else
			return false;
	}

	// Build feed list
	private void doBuildFeedList(PersistenceManager persistenceManager,
			UpdateProcess updateProcess, SystemInfo systemInfo) {
		List<String> feedList = feedListBuilder.build();
		updateProcess.setFeedList(feedList);
		logUpdateInfo(State.BUILD_FEED_LIST, "Found " + feedList.size() + " feeds");
		updateProcess.setState(State.READ_FEED);
	}

	// Reset updateProcess and create an updateStat
	private void doInit(PersistenceManager persistenceManager,
			UpdateProcess updateProcess, SystemInfo systemInfo) {
		if (updateProcess.getUpdateStatId() != null && updateProcess.getUpdateStatId() != 0) {
			UpdateStat updateStat = persistenceManager.getObjectById(
					UpdateStat.class, updateProcess.getUpdateStatId());
			updateStat.setFinishTime(System.currentTimeMillis());
			persistenceManager.makePersistent(updateStat);
		}
		
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
		persistenceManager.makePersistent(systemInfo);
		logger.info("Created the systemInfo");
		
		UpdateProcess updateProcess = new UpdateProcess();
		updateProcess.setId(updateProcessId);
		persistenceManager.makePersistent(updateProcess);
		logger.info("Created the updateProcess");
	}

	private void logUpdateInfo(State state, String message) {
		logger.info("Update Process " + state + ": " + message);
	}
}
