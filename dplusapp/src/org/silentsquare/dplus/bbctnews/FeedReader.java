package org.silentsquare.dplus.bbctnews;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class FeedReader {
	
	private static final Logger logger = Logger.getLogger(FeedReader.class.getName());
	
	private DocumentBuilder builder;
	
	public FeedReader() {
		try {
			builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			logger.severe(e.getMessage());
			throw new RuntimeException(e);
		}
	}
	
	public List<News> read(String url) {
		Document doc = null;
		try {
			doc = builder.parse(url);
		} catch (SAXException e) {
			logger.severe(e.getMessage());
			return Collections.EMPTY_LIST;
		} catch (IOException e) {
			logger.severe(e.getMessage());
			return Collections.EMPTY_LIST;
		}
		
		return readFeed(url, doc);
	}

	private List<News> readFeed(String url, Document doc) {
		List<News> list = new ArrayList<News>();
		
		NodeList nl = doc.getDocumentElement().getElementsByTagName("item");
		for (int i = 0; i < nl.getLength(); i++) {
			Element item = (Element) nl.item(i);
			String title = getElementText(item, "title");
			String description = getElementText(item, "description");
			String link = getElementText(item, "link");
			News news = new News(url, title, description, link);
			news.parseTitle();
			if (news.getLocation() != null) {
				// make sure this news is understood
				list.add(news);
			}
			logger.finest(news.toString());
		}
		
		return list;
	}

	private String getElementText(Element item, String subname) {
		NodeList nl = item.getElementsByTagName(subname);
		if (nl.getLength() == 1) {
			Element e = (Element) nl.item(0);
			return e.getTextContent();
		} else {
			logger.warning("Either no or more than one " + subname + " in news item: " + item.getTextContent());
			return "";
		}		
	}
}
