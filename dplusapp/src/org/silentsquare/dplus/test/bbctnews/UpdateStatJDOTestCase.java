package org.silentsquare.dplus.test.bbctnews;

import static org.junit.Assert.*;

import java.util.Calendar;
import java.util.List;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.silentsquare.dplus.bbctnews.UpdateStat;
import org.silentsquare.dplus.test.GAETestCase;

public class UpdateStatJDOTestCase extends GAETestCase {
	
	private PersistenceManager persistenceManager;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		setUp();
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		tearDown();
	}

	@Test
	public void test() throws Exception {
		UpdateStat us = new UpdateStat();
		us.setStartTime(System.currentTimeMillis());
		long id = 0;
		
		persistenceManager = persistenceManagerFactory.getPersistenceManager();
		try {
			us = persistenceManager.makePersistent(us);
			id = us.getId();
			assertTrue(id != 0);
		} finally {
			persistenceManager.close();
		}
		
		persistenceManager = persistenceManagerFactory.getPersistenceManager();
		try {
			UpdateStat us2 = persistenceManager.getObjectById(UpdateStat.class, id);
			System.out.println(us2.getStartTime());
			assertTrue(us2.getStartTime() > 0);
		} finally {
			persistenceManager.close();
		}
	}
	
	@Test
	public void testFindLast() throws Exception {
		persistenceManager = persistenceManagerFactory.getPersistenceManager();
		try {
			Query query = persistenceManager.newQuery(UpdateStat.class);
		    query.setOrdering("startTime desc");
		    query.setRange(0, 2);
		    try {
		        List<UpdateStat> list = (List<UpdateStat>) query.execute();
		        System.out.println(list.size());
		    } finally {
		        query.closeAll();
		    }
		} finally {
			persistenceManager.close();
		}		
	}
	
	@Test
	public void testClean() throws Exception {
		long d = System.currentTimeMillis() - 1000 * 3600 * 24 * 7;
		
		persistenceManager = persistenceManagerFactory.getPersistenceManager();
		try {
			Query query = persistenceManager.newQuery(UpdateStat.class);
			query.setFilter("startTime < " + d);
		    query.setOrdering("startTime asc");
		    query.setRange(0, 50);
		    List<UpdateStat> list = null;
		    try {
		    	list  = (List<UpdateStat>) query.execute();
		    } finally {
		        query.closeAll();
		    }
		    persistenceManager.deletePersistentAll(list);
		} finally {
			persistenceManager.close();
		}		
	}

}
