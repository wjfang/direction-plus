package org.silentsquare.dplus.bbctnews;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.MissingResourceException;
import java.util.Properties;
import java.util.ResourceBundle;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class FeedListBuilder {
	
	private static final Logger logger = Logger.getLogger(FeedListBuilder.class); 
	
	public FeedListBuilder() {
		
	}
	
	public void build() {
		ResourceBundle bundle = null;
		try {
			bundle = ResourceBundle.getBundle("bbcTravelNews");
		} catch (MissingResourceException e) {
			logger.fatal(e);
			return;
		}
		
		String url = null;
		String feeds = null;
		try {
			url = bundle.getString("url");
			feeds = bundle.getString("feeds");
		} catch (MissingResourceException e) {
			logger.fatal(e);
			return;
		}
		
		DocumentBuilder builder = null;
		try {
			builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			logger.fatal(e);
			return;
		}
		
		Document doc = null;
		try {
			doc = builder.parse(url);
		} catch (SAXException e) {
			logger.fatal(e);
			return;
		} catch (IOException e) {
			logger.fatal(e);
			return;
		}
		
		buildFeedList(doc, feeds);
	}

	private void buildFeedList(Document doc, String feeds) {
		List<FeedInfo> list = new ArrayList<FeedInfo>();
		
		NodeList nl = doc.getDocumentElement().getElementsByTagName("body");
		Element body = (Element) nl.item(0);
		if (body == null) {
			logger.fatal("Do not understand the format of feed list");
			return;
		}				
		logger.debug(body.getNodeName());
		
		nl = body.getElementsByTagName("outline");
		if (nl.getLength() <= 0) {
			logger.error("Emply feed list");
			return;
		}
		for (int i = 0; i < nl.getLength(); i++) {
			Element outline = (Element) nl.item(i);
			/*
			 * Only handle news in English
			 */
			String language = outline.getAttribute("language");
			if (!"en".equals(language))
				continue;
			String text = outline.getAttribute("text");
			logger.debug(text);
			String ss[] = text.split("\\|");
			if (ss.length != 2) {
				logger.error("<outline text=\"...\"> is not of \"Travel News | Bedfordshire\"");
				continue;
			}
			text = ss[1].trim();
			String url = outline.getAttribute("xmlUrl");
			logger.debug(url);
			list.add(new FeedInfo(text, url));
		}
		
		saveFeedList(list, feeds);
	}

	private void saveFeedList(List<FeedInfo> list, String feeds) {
		Properties p = new Properties();
		for (FeedInfo info : list) {
			p.setProperty(info.name, info.url);
		}
		
		File f = new java.io.File(feeds);
		logger.debug(f);
		if (f.exists())
			f.delete();
		try {
			p.store(new FileOutputStream(f), Calendar.getInstance().toString());
		} catch (FileNotFoundException e) {
			logger.error(e);
			return;
		} catch (IOException e) {
			logger.error(e);
			return;
		}
	}

	static class FeedInfo {
		String name;
		String url;
		
		FeedInfo(String name, String url) {
			this.name = name;
			this.url = url;
		}
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		new FeedListBuilder().build();
	}

}
