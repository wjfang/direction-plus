package org.silentsquare.dplus.bbctnews;

import java.util.List;

public interface NewsDatabase {
	
	/**
	 * 
	 * @param bottom bottom latitude
	 * @param top top latitude
	 * @param left left longitude
	 * @param right right longitude
	 * @return all travel news happening inside the box
	 */
	public List<News> query(float bottom, float top, float left, float right);
	
}
