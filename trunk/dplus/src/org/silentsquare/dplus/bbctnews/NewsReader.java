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
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

public class NewsReader {
	
	private static final Logger logger = Logger.getLogger(NewsReader.class); 
	
	private List<News> newsList = new ArrayList<News>();
	
	private XMLReader xmlParser;
	
	private FeedListBuilder feedListBuilder;
	
	public FeedListBuilder getFeedListBuilder() {
		return feedListBuilder;
	}
	
	public void setFeedListBuilder(FeedListBuilder feedListBuilder) {
		this.feedListBuilder = feedListBuilder;
	}
	
	public NewsReader() throws SAXException {
		this.xmlParser = XMLReaderFactory.createXMLReader();		
		this.xmlParser.setContentHandler(new FeedContentHandler(this.newsList)); 
	}
	
	public List<News> read() {
		this.newsList.clear();
		for (String url : this.feedListBuilder.build()) {
			try {
				parse(url);
			} catch (MalformedURLException e) {
				logger.error(e);
			} catch (IOException e) {
				logger.error(e);
			} catch (SAXException e) {
				logger.error(e);
			}
		}
		
		return new ArrayList<News>(this.newsList);
	}
	
	private void parse(String url) throws MalformedURLException, IOException, SAXException {
		InputStream is = new URL(url).openStream();
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		this.xmlParser.parse(new InputSource(reader));
		reader.close();
	}

}
