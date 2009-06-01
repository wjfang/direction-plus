package org.silentsquare.dplus.bbctnews;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.silentsquare.dplus.bbctnews.CoordinateFinder.Coordinate;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

public class NewsReader {
	
	private static final Logger logger = Logger.getLogger(NewsReader.class); 
	
	private List<News> newsList = new ArrayList<News>();
	
	public List<News> getNewsList() {
		return newsList;
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
		for (String url : this.feedListBuilder.build()) {
			parse(url);
		}
		
		for (News news : newsList) {
			updateCoordinate(news);
		}
		
		return this.newsList;
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
			logger.error(e);
		}
	}

	public void reset() {
		this.newsList.clear();		
	}

	public List<String> buildFeedList() {
		return feedListBuilder.build();
	}

}
