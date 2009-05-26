package org.silentsquare.dplus.test;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.silentsquare.dplus.bbctnews.FeedListBuilder;
import org.silentsquare.dplus.bbctnews.NewsReader;

public class NewsReaderTest {

	private NewsReader newsReader;
	
	private long start;
	
	@Before
	public void setUp() throws Exception {
		FeedListBuilder builder = new FeedListBuilder();
		builder.setUrl("http://www.bbc.co.uk/travelnews/tpeg/rss.opml");
		newsReader = new NewsReader();
		newsReader.setFeedListBuilder(builder);
		start = System.currentTimeMillis();
	}

	@After
	public void tearDown() throws Exception {
		System.out.println((System.currentTimeMillis() - start) + "ms");
	}
	
	@Test
	public void testRead() throws Exception {
		System.out.println(newsReader.read().size());
	}

	/*
	ERROR: org.silentsquare.dplus.bbctnews.NewsDatabase.build(NewsDatabase.java:45)
	 java.io.FileNotFoundException: http://www.bbc.co.uk/travelnews/tpeg/cy/regions/rtm/wales_rss.xml
	ERROR: org.silentsquare.dplus.bbctnews.NewsDatabase.build(NewsDatabase.java:45)
	 java.io.FileNotFoundException: http://www.bbc.co.uk/travelnews/tpeg/en/pti/swanseacorkferries_rss.xml
	2393
	475532ms
	490157ms
	583219ms
	481281ms
	 */
}
