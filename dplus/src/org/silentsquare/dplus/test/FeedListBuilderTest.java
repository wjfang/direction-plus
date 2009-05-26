package org.silentsquare.dplus.test;

import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.silentsquare.dplus.bbctnews.FeedListBuilder;

public class FeedListBuilderTest {

	private FeedListBuilder builder;
	
	@Before
	public void setUp() throws Exception {
		builder = new FeedListBuilder();
		builder.setUrl("http://www.bbc.co.uk/travelnews/tpeg/rss.opml");
	}

	@After
	public void tearDown() throws Exception {
		
	}
	
	@Test
	public void testBuild() throws Exception {
		List<String> list = builder.build();
		for (String s : list) {
			System.out.println(s);
		}
	}

}
