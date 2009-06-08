package org.silentsquare.dplus.test.bbctnews;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.silentsquare.dplus.bbctnews.CoordinateFinder;
import org.silentsquare.dplus.bbctnews.CoordinateFinder.Coordinate;

public class CoordinateFinderTestCase {

	private CoordinateFinder finder;
	
	@Before
	public void setUp() throws Exception {
		finder = new CoordinateFinder();
		finder.setGeoURL("http://maps.google.com/maps/geo?output=json&oe=utf8&sensor=false");
		finder.setApiKey("ABQIAAAAj1DqbVODIwfxFozz52vGCBTBKtqaU_qKk-cwvy6tFS36W-EYRhTQIx4Jc7ffCTqfgjkPZSJocVxSCA");
	}

	@After
	public void tearDown() throws Exception {
		
	}

	@Test
	public void testSearch() {
		Coordinate c;
//		c = finder.search("SO16 7PD");
//		c = finder.search("A379 Teignmouth");
//		c = finder.search("Southampton");
		c = finder.search("Portsmouth");
//		c = finder.search("Portland");
		System.out.println(c.getLatitude() + ", " + c.getLongitude());
	}

}
