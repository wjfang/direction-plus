package org.silentsquare.dplus.bbctnews;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
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
	
	private List<News> newsList;
	
	private Comparator<News> latitudeComparator = new Comparator<News>() {
		@Override
		public int compare(News n1, News n2) {
			float f = n1.getLatitude() - n2.getLatitude();
			if (f < 0)
				return -1;
			else if (f > 0)
				return 1;
			else // f == 0
				return 0;
		}
	};
	
	private Comparator<News> longitudeComparator = new Comparator<News>() {
		@Override
		public int compare(News n1, News n2) {
			float f = n1.getLongitude() - n2.getLongitude();
			if (f < 0)
				return -1;
			else if (f > 0)
				return 1;
			else // f == 0
				return 0;
		}
	};
	
	public TestNewsDatabase() {
		ResourceBundle bundle = ResourceBundle.getBundle("dplus");
		String dbPath = bundle.getString("test.newsDB");
		
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
	public List<News> query(float bottom, float top, float left, float right) {
		int i = Collections.binarySearch(newsList, new News(bottom, 0), latitudeComparator);
		if (i < 0)
			i = -i - 1;
		
		int j = Collections.binarySearch(newsList, new News(top, 0), latitudeComparator);
		if (j < 0)
			j = -j - 1;
		
		List<News> templist = new ArrayList<News>(newsList.subList(i, j));
		Collections.sort(templist, longitudeComparator);
		
		i = Collections.binarySearch(templist, new News(0, left), longitudeComparator);
		if (i < 0)
			i = -i - 1;
		
		j = Collections.binarySearch(templist, new News(0, right), longitudeComparator);
		if (j < 0)
			j = -j - 1;
		
		return templist.subList(i, j);
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
		List<News> results = tdb.query(50.798912f, 50.904964f, -1.403234f, -1.0911627f);
		for (News n : results) {
			System.out.println(n);
		}
		System.out.println((System.currentTimeMillis() - start) + "ms");
	}
}
