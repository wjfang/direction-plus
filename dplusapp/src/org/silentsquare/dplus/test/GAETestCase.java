package org.silentsquare.dplus.test;

import java.io.File;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManagerFactory;

import com.google.appengine.api.datastore.dev.LocalDatastoreService;
import com.google.appengine.tools.development.ApiProxyLocalImpl;
import com.google.apphosting.api.ApiProxy;

public class GAETestCase {
	
	protected static PersistenceManagerFactory persistenceManagerFactory;
	
	protected static void setUp() throws Exception {
        ApiProxy.setEnvironmentForCurrentThread(new TestEnvironment());
        ApiProxy.setDelegate(new ApiProxyLocalImpl(new File(".")){});
        
        ApiProxyLocalImpl proxy = (ApiProxyLocalImpl) ApiProxy.getDelegate();
        proxy.setProperty(LocalDatastoreService.NO_STORAGE_PROPERTY, Boolean.TRUE.toString());
        
        persistenceManagerFactory =
			JDOHelper.getPersistenceManagerFactory("transactions-optional");
    }

	protected static void tearDown() throws Exception {
    	ApiProxyLocalImpl proxy = (ApiProxyLocalImpl) ApiProxy.getDelegate();
        LocalDatastoreService datastoreService = (LocalDatastoreService) proxy.getService("datastore_v3");
        datastoreService.clearProfiles();
        
        ApiProxy.setDelegate(null);
        ApiProxy.setEnvironmentForCurrentThread(null);
    }

}
