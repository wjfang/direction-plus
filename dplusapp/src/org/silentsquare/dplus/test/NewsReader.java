package org.silentsquare.dplus.test;

import java.util.List;

import org.silentsquare.dplus.bbctnews.FeedListBuilder;
import org.silentsquare.dplus.bbctnews.FeedReader;
import org.silentsquare.dplus.bbctnews.News;

public class NewsReader {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		FeedListBuilder builder = new FeedListBuilder();
		builder.setUrl("http://www.bbc.co.uk/travelnews/tpeg/rss.opml");
		List<String> feedlist = builder.build();
		
		FeedReader reader = new FeedReader();
		for (String url : feedlist) {
			List<News> newslist = reader.read(url);
			if (newslist.size() > 100)
				System.out.println(url + " - " + newslist.size());
		}
	}

}
