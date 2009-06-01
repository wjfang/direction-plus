package org.silentsquare.dplus.bbctnews;

import java.util.List;

import org.apache.log4j.Logger;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

class FeedContentHandler extends DefaultHandler {
	
	private static final Logger logger = Logger.getLogger(FeedContentHandler.class); 
	
	private List<News> newsList;
	
	private News currentNews;
	
	private StringBuffer textBuffer;
	
	FeedContentHandler(List<News> newsList) {
		this.newsList = newsList;
	}
	
	@Override
	public void startDocument() throws SAXException {
					
	}

	@Override
	public void startElement(String namespaceURI, String localName, 
			   String qualifiedName, Attributes atts) throws SAXException {
		if ("item".equals(localName)) {
			currentNews = new News();
		} else if (currentNews != null 
				&& ("title".equals(localName)
						|| "description".equals(localName) 
						|| "link".equals(localName))) {
			textBuffer = new StringBuffer();
		}
	}

	@Override
	public void endElement(String namespaceURI, String localName, 
			   String qualifiedName) throws SAXException {
		if ("item".equals(localName)) {
			this.newsList.add(currentNews);
			currentNews.parseTitle();
			logger.debug(currentNews);
			currentNews = null;
		} else if (currentNews != null) {
			if ("title".equals(localName)) {
				currentNews.setTitle(textBuffer.toString());
			} else if ("description".equals(localName)) {
				currentNews.setDescription(textBuffer.toString());
			} else if ("link".equals(localName)) {
				currentNews.setLink(textBuffer.toString());
			}
		}
	}
	
	@Override
	public void characters(char[] buf, int offset, int len) throws SAXException {
		if (textBuffer != null) {
			String value = new String(buf, offset, len);
			textBuffer.append(value);
		}
	}
}
