package org.silentsquare.dplus.test.bbctnews;

import static org.junit.Assert.*;

import java.util.List;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.silentsquare.dplus.bbctnews.FeedReader;
import org.silentsquare.dplus.bbctnews.News;
import org.silentsquare.dplus.test.GAETestCase;

public class NewsJDOTestCase extends GAETestCase {
	
	private PersistenceManager persistenceManager;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		setUp();
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		tearDown();
	}

	private String url = "http://www.bbc.co.uk/travelnews/tpeg/en/local/rtm/bedfordshire_rss.xml"; 
	private static long id;
	
	@Test
	public void testCreate() throws Exception {
		FeedReader feedReader = new FeedReader();
		List<News> list = feedReader.read(url);
		
		for (News n : list) {
			assertNull(n.getId());
		}		
		
		persistenceManager = persistenceManagerFactory.getPersistenceManager();
		try {
			list = (List) persistenceManager.makePersistentAll(list);
			for (News n : list) {
				System.out.println(n.getId());
			}
			id = list.get(list.size() - 1).getId();
		} finally {
			persistenceManager.close();
		}
	}
	
	@Test
	public void testGet() throws Exception {
		News news = null;
		persistenceManager = persistenceManagerFactory.getPersistenceManager();
		try {
			news = persistenceManager.getObjectById(News.class, id);
			System.out.println(news);
		} finally {
			persistenceManager.close();
		}
		System.out.println(news);
		news.setTitle("hello");
	}
	
	@Test
	public void testFindCurrent() throws Exception {
		persistenceManager = persistenceManagerFactory.getPersistenceManager();
		try {
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
			System.out.println(nl.size());
			for (News n : nl) {
				System.out.println(n.getLocation() + ": " + n.getLatitude());
			}
		} finally {
			persistenceManager.close();
		}
	}
	
	@Test
	public void testQueryByLocation() throws Exception {
		String location = "Balliol Road Kempston Hardwick";
		
		persistenceManager = persistenceManagerFactory.getPersistenceManager();
		try {
			Query query = persistenceManager.newQuery(News.class);
			query.setFilter("obsolete == true && location == locParam");
			query.declareParameters("String locParam");

			List<News> nl = null;
			try {
				 nl = (List<News>) query.execute(location);
			} finally {
				query.closeAll();
			}
			System.out.println(nl.size());
			for (News n : nl) {
				System.out.println(n.getLocation());
			}
		} finally {
			persistenceManager.close();
		}
	}
	
	@Test
	public void testQueryByLatRange() throws Exception {
		float top = 70;
		float bottom = -90; 
		List<News> nl = null;
		
		persistenceManager = persistenceManagerFactory.getPersistenceManager();
		try {
			Query query = persistenceManager.newQuery(News.class);
			query.setFilter("obsolete == false && latitude <= top && latitude >= bottom");
			query.declareParameters("float top, float bottom");
			query.setOrdering("latitude asc");

			try {
				 nl = (List<News>) query.execute(top, bottom);
			} finally {
				query.closeAll();
			}
			System.out.println(nl.size());
			for (News n : nl) {
				System.out.println(n.getLocation() + ": " + n.getLatitude());
			}
		} finally {
			persistenceManager.close();
		}
		
		System.out.println(nl.get(0));
		
		nl.get(0).setDegree(0);
	}

	@Test
	public void testClean() throws Exception {
		persistenceManager = persistenceManagerFactory.getPersistenceManager();
		try {
			Query query = persistenceManager.newQuery(News.class);
			query.setFilter("obsolete == true");
			query.setRange(0, 100);

			List<News> nl = null;
			try {
				 nl = (List<News>) query.execute();
			} finally {
				query.closeAll();
			}
			persistenceManager.deletePersistentAll(nl);
		} finally {
			persistenceManager.close();
		}
	}
}
