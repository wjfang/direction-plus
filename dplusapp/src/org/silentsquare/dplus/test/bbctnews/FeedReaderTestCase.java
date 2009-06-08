package org.silentsquare.dplus.test.bbctnews;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.silentsquare.dplus.bbctnews.FeedReader;
import org.silentsquare.dplus.bbctnews.News;

public class FeedReaderTestCase {

	private FeedReader feedReader;
	
	@Before
	public void setUp() throws Exception {
		feedReader = new FeedReader();
	}

	@After
	public void tearDown() throws Exception {
		
	}

	@Test
	public void testRead() {
		List<News> list = feedReader.read("http://www.bbc.co.uk/travelnews/tpeg/en/local/rtm/bedfordshire_rss.xml");
		for (News n : list) {
			System.out.println(n);
		}
	}

}
