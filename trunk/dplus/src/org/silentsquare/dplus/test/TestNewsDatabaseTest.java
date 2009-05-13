package org.silentsquare.dplus.test;

import static org.junit.Assert.*;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.silentsquare.dplus.bbctnews.News;
import org.silentsquare.dplus.bbctnews.TestNewsDatabase;

public class TestNewsDatabaseTest {
	
	private TestNewsDatabase tdb = new TestNewsDatabase();
	
	private long start;
	
	@Before
	public void setUp() throws Exception {
		start = System.currentTimeMillis();
	}

	@After
	public void tearDown() throws Exception {
		System.out.println((System.currentTimeMillis() - start) + "ms");
	}

	@Test
	public void testSerialization() throws Exception {
		News news = createTestNews();
		List<News> list = new ArrayList<News>();
		list.add(news);
		ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("./test.db"));
		oos.writeObject(list);
		oos.close();
		
		ObjectInputStream ois = new ObjectInputStream(new FileInputStream("./test.db"));
		List<News> list2 = (List<News>) ois.readObject();
		assertEquals(news, list2.get(0));	
	}
	
	private News createTestNews() {
		News news = new News("title01", "description01", "link01");
		news.setDegree(3);
		news.setLatitude(50.12f);
		news.setLongitude(-3.45f);
		news.setLocation("somewhere");
		return news;
	}
	
	@Test
	public void testQuerySP() {
		/*
		 * Southampton: 50.904964, -1.403234
		 * Portsmouth: 50.798912, -1.0911627
		 */
		List<News> results = tdb.query(Arrays.asList(
				new float[] {50.904964f, -1.403234f}, new float[] {50.798912f, -1.0911627f}));
		for (News n : results) {
			System.out.println(n);
		}
	}
	
	@Test
	public void testJSON() {
		News news = createTestNews();
		JSONObject jo = new JSONObject(news);
		System.out.println(jo);
		
		List<News> results = tdb.query(Arrays.asList(
				new float[] {50.904964f, -1.403234f}, new float[] {50.798912f, -1.0911627f}));
		JSONArray ja = new JSONArray();
		for (News n : results) {
			ja.put(new JSONObject(n));
		}
		System.out.println(ja);
	}

}
