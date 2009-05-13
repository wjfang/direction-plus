package org.silentsquare.dplus.bbctnews;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.ResourceBundle;

import org.apache.log4j.Logger;

/**
 * Read + Serialization = 464328ms
 * Deserialization 		= 125ms
 * @author wjfang
 *
 */
public class TestNewsDatabase extends AbstractNewsDatabase {
	
	private static final Logger logger = Logger.getLogger(TestNewsDatabase.class);
	
	/**
	 * Sorted by latitude.
	 */
	private List<News> newsList;
	
	public TestNewsDatabase() {
		ResourceBundle bundle = ResourceBundle.getBundle("dplus");
		String dbPath = bundle.getString("test.newsDB");
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
				logger.error(e);
			} catch (IOException e) {
				logger.error(e);
			} catch (ClassNotFoundException e) {
				logger.error(e);
			}			
		} else {
			/*
			 * Create one
			 */
			newsList = readNews();
			/*
			 * Sort by latitude
			 */
			Collections.sort(newsList, latitudeComparator);
			try {
				ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(dbf));
				oos.writeObject(newsList);
				oos.close();
			} catch (FileNotFoundException e) {
				logger.error(e);
			} catch (IOException e) {
				logger.error(e);
			}			
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
	protected void updateDatabase() {
		// Do nothing.
	}

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		long start = System.currentTimeMillis();
		TestNewsDatabase tdb = new TestNewsDatabase(); 
		System.out.println(tdb.size());
		List<News> results = tdb.query(Arrays.asList(
				new float[] {50.904964f, -1.403234f}, new float[] {50.798912f, -1.0911627f}));
		for (News n : results) {
			System.out.println(n);
		}
		System.out.println((System.currentTimeMillis() - start) + "ms");
	}
}
