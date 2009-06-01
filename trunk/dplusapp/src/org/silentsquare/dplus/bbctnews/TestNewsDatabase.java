package org.silentsquare.dplus.bbctnews;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.List;
import java.util.logging.Logger;

/**
 * Read + Serialization = 464328ms
 * Deserialization 		= 125ms
 * @author wjfang
 *
 */
public class TestNewsDatabase extends AbstractNewsDatabase {
	
	private static final Logger logger = Logger.getLogger(TestNewsDatabase.class.getName());
	
	/**
	 * Sorted by latitude.
	 */
	private List<News> newsList;
	
	public TestNewsDatabase(String dbPath) {
		logger.info(dbPath);
		
		File dbf = new File(dbPath);
		if (dbf.exists()) {
			/*
			 * Use this one
			 */
			try {
				ObjectInputStream ois = new ObjectInputStream(new FileInputStream(dbf));
				newsList = (List<News>) ois.readObject();
				ois.close();
			} catch (FileNotFoundException e) {
				logger.severe(e.getMessage());
			} catch (IOException e) {
				logger.severe(e.getMessage());
			} catch (ClassNotFoundException e) {
				logger.severe(e.getMessage());
			}			
		} else {
			logger.severe(dbPath + " does not exist.");
		}
	}

	public int size() {
		return this.newsList.size();
	}
	
	@Override
	public List<News> query(List<float[]> waypoints) {
		return lookUpInList(waypoints, newsList);
	}
	
	@Override
	public void update() {
		// Do nothing		
	}
	
}
