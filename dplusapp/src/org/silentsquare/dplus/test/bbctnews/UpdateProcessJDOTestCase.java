package org.silentsquare.dplus.test.bbctnews;

import static org.junit.Assert.*;

import java.util.Arrays;

import javax.jdo.PersistenceManager;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.silentsquare.dplus.bbctnews.UpdateProcess;
import org.silentsquare.dplus.test.GAETestCase;

public class UpdateProcessJDOTestCase extends GAETestCase {
	
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
		String upid = "updateProc";
		long usid = 1234;
		
		UpdateProcess updateProcess = new UpdateProcess();
		updateProcess.setId(upid);
		updateProcess.setFeedList(Arrays.asList("hello", "world"));
		updateProcess.setUpdateStatId(usid);
		
		persistenceManager = persistenceManagerFactory.getPersistenceManager();
		try {
			persistenceManager.makePersistent(updateProcess);
		} finally {
			persistenceManager.close();
		}
		
		persistenceManager = persistenceManagerFactory.getPersistenceManager();
		try {
			UpdateProcess updateProcess2 = persistenceManager.getObjectById(UpdateProcess.class, upid);
			assertTrue(updateProcess2.getUpdateStatId() == usid);
			assertTrue(updateProcess2.getFeedList().size() == 2);
		} finally {
			persistenceManager.close();
		}
	}

}
