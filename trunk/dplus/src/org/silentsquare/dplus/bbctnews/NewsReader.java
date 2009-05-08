package org.silentsquare.dplus.bbctnews;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import org.apache.log4j.Logger;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

public class NewsReader {
	
	private static final Logger logger = Logger.getLogger(NewsReader.class); 
	
	private List<News> newsList = new ArrayList<News>();
	
	private XMLReader xmlParser;
	
	private ResourceBundle feedUrlBundle;
	
	public NewsReader() throws SAXException {
		this.xmlParser = XMLReaderFactory.createXMLReader();		
		this.xmlParser.setContentHandler(new FeedContentHandler(this.newsList));
		
		this.feedUrlBundle = ResourceBundle.getBundle("feeds"); 
	}
	
	public List<News> read() {
		for (String key : feedUrlBundle.keySet()) {
			logger.debug(key);
			String url = feedUrlBundle.getString(key);
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
		
		return this.newsList;
	}
	
	private void parse(String url) throws MalformedURLException, IOException, SAXException {
		InputStream is = new URL(url).openStream();
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		this.xmlParser.parse(new InputSource(reader));
		reader.close();
	}
	
	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		long start = System.currentTimeMillis();
		System.out.println(new NewsReader().read().size());
		System.out.println((System.currentTimeMillis() - start) + "ms");
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