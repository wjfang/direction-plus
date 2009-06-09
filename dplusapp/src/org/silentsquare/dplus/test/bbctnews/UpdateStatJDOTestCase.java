package org.silentsquare.dplus.test.bbctnews;

import static org.junit.Assert.*;

import javax.jdo.PersistenceManager;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.silentsquare.dplus.bbctnews.UpdateStat;
import org.silentsquare.dplus.test.GAETestCase;

public class UpdateStatJDOTestCase extends GAETestCase {
	
	private PersistenceManager persistenceManager;
	
	@Before
	public void setUp() throws Exception {
		super.setUp();
	}

	@After
	public void tearDown() throws Exception {
		super.tearDown();
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

}
