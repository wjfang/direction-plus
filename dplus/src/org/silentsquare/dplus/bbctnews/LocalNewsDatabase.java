package org.silentsquare.dplus.bbctnews;

import java.util.List;

import org.apache.log4j.Logger;

/**
 * In memory, persistent database.
 * @author wjfang
 *
 */
public class LocalNewsDatabase extends AbstractNewsDatabase {
	
	private static final Logger logger = Logger.getLogger(LocalNewsDatabase.class);

	@Override
	public List<News> query(List<float[]> waypoints) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected void updateDatabase() {
		// TODO Auto-generated method stub

	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
