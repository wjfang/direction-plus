package org.silentsquare.dplus.bbctnews;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.silentsquare.dplus.bbctnews.CoordinateFinder.Coordinate;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

public class NewsReader {
	
	private static final Logger logger = Logger.getLogger(NewsReader.class.getName()); 
	
	private List<News> newsList = new ArrayList<News>();
	
	public List<News> getNewsList() {
		return newsList;
	}
	
	private List<String> feedList;
	
	public List<String> getFeedList() {
		return feedList;
	}
	
	private XMLReader xmlParser;
	
	private FeedListBuilder feedListBuilder;
	
	public FeedListBuilder getFeedListBuilder() {
		return feedListBuilder;
	}
	
	public void setFeedListBuilder(FeedListBuilder feedListBuilder) {
		this.feedListBuilder = feedListBuilder;
	}
	
	private CoordinateFinder coordinateFinder;
	
	public CoordinateFinder getCoordinateFinder() {
		return coordinateFinder;
	}
	
	public void setCoordinateFinder(CoordinateFinder coordinateFinder) {
		this.coordinateFinder = coordinateFinder;
	}
	
	public NewsReader() throws SAXException {
		this.xmlParser = XMLReaderFactory.createXMLReader();		
		this.xmlParser.setContentHandler(new FeedContentHandler(this.newsList)); 
	}
	
	public List<News> read() {
		this.newsList.clear();
		
		buildFeedList();
		for (String url : this.feedList) {
			parse(url);
		}
		
		for (News news : newsList) {
			updateCoordinate(news);
		}
		
		return copyNewsList();
	}
	
	public void updateCoordinate(News news) {
		if (news.getLocation() != null) {
			Coordinate co = coordinateFinder.search(news.getLocation());
			if (co != null) {
				news.setLatitude(co.getLatitude());
				news.setLongitude(co.getLongitude());
			}
		}
	}

	public void parse(String url) {
		try {
			InputStream is = new URL(url).openStream();
			BufferedReader reader = new BufferedReader(new InputStreamReader(is));
			this.xmlParser.parse(new InputSource(reader));
			reader.close();
		} catch (Exception e) {
			logger.severe(e.getMessage());
		}
	}

	public void reset() {
		this.newsList.clear();		
	}

	public void buildFeedList() {
		this.feedList = this.feedListBuilder.build();
	}

	public List<News> copyNewsList() {
		return new ArrayList<News>(this.newsList);		
	}
}
