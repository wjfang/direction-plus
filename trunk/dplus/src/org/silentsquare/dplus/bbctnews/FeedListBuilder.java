package org.silentsquare.dplus.bbctnews;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
	
	private String url;
	
	public String getUrl() {
		return url;
	}
	
	public void setUrl(String url) {
		this.url = url;
	}
	
	private List<String> list;
	
	public FeedListBuilder() {
		
	}
	
	public List<String> build() {
		this.list = new ArrayList<String>();
		
		DocumentBuilder builder = null;
		try {
			builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			logger.fatal(e);
			return list;
		}
		
		Document doc = null;
		try {
			doc = builder.parse(url);
		} catch (SAXException e) {
			logger.fatal(e);
			return list;
		} catch (IOException e) {
			logger.fatal(e);
			return list;
		}
		
		return buildFeedList(doc);
	}

	private List<String> buildFeedList(Document doc) {
		NodeList nl = doc.getDocumentElement().getElementsByTagName("body");
		Element body = (Element) nl.item(0);
		if (body == null) {
			logger.fatal("Do not understand the format of feed list");
			return list;
		}				
		logger.debug(body.getNodeName());
		
		nl = body.getElementsByTagName("outline");
		if (nl.getLength() <= 0) {
			logger.error("Emply feed list");
			return list;
		}
		for (int i = 0; i < nl.getLength(); i++) {
			Element outline = (Element) nl.item(i);
			/*
			 * Only handle news in English
			 */
			String language = outline.getAttribute("language");
			if (!"en".equals(language))
				continue;
//			String text = outline.getAttribute("text");
//			logger.debug(text);
//			String ss[] = text.split("\\|");
//			if (ss.length != 2) {
//				logger.error("<outline text=\"...\"> is not of \"Travel News | Bedfordshire\"");
//				continue;
//			}
//			text = ss[1].trim();
			String url = outline.getAttribute("xmlUrl");
			logger.debug(url);
			list.add(url);
		}
		
		return list;
	}

}
