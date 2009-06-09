package org.silentsquare.dplus.test.bbctnews;

import static org.junit.Assert.*;

import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.silentsquare.dplus.bbctnews.SystemInfo;
import org.silentsquare.dplus.test.GAETestCase;

public class SystemInfoJDOTestCase extends GAETestCase {
	
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
		String siid = "systemInfo";
		
		SystemInfo si = new SystemInfo();
		si.setId(siid);
		si.setStartUpTime(System.currentTimeMillis());
		
		persistenceManager = persistenceManagerFactory.getPersistenceManager();
		try {
			persistenceManager.makePersistent(si);
		} finally {
			persistenceManager.close();
		}
		
		persistenceManager = persistenceManagerFactory.getPersistenceManager();
		try {
			SystemInfo si2 = persistenceManager.getObjectById(SystemInfo.class, siid);
			System.out.println(si2.getStartUpTime());
			assertTrue(si2.getStartUpTime() > 0);
		} finally {
			persistenceManager.close();
		}
	}

	@Test
	public void testInit() throws Exception {
		String siid = "systemInfo";
		persistenceManager = persistenceManagerFactory.getPersistenceManager();
		try {
			try {
				SystemInfo si = persistenceManager.getObjectById(SystemInfo.class, siid);
			} catch (JDOObjectNotFoundException e) {
				System.out.println(e);
			}
		} finally {
			persistenceManager.close();
		}
	}
}
