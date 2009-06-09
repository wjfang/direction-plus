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
		persistenceManager = persistenceManagerFactory.getPersistenceManager();
		try {
			News news = persistenceManager.getObjectById(News.class, id);
			System.out.println(news);
		} finally {
			persistenceManager.close();
		}
	}
	
	@Test
	public void testFindCurrent() throws Exception {
		persistenceManager = persistenceManagerFactory.getPersistenceManager();
		try {
			Query query = persistenceManager.newQuery(News.class);
			query.setFilter("url == urlParam");
			query.setFilter("obsolete == false");
//			query.setFilter("createTime > 0");
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
				System.out.println(n.getLocation() + ": " + n.getTitle());
			}
		} finally {
			persistenceManager.close();
		}
	}

}
