package org.silentsquare.dplus.test.bbctnews;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.silentsquare.dplus.bbctnews.Route.Step;

public class RouteTestCase {

	@Before
	public void setUp() throws Exception {
		
	}

	@After
	public void tearDown() throws Exception {
		
	}

	@Test
	public void testGuestRoad() {
		Step step = new Step();
		step.setDistance(11000);
		step.setDescriptionHtml("At junction <b>6a</b>, exit onto <b>M25</b> " +
				"toward <b>Dartford/<wbr/>M11/<wbr/>M20</b> <div class=\"google_impnote\">Partial toll road</div>");
		if (step.isLongEnough())
			System.out.println(step.guessRoad());
	}
	
	@Test
	public void testGuestRoad2() {
		Step step = new Step();
		step.setDistance(11000);
		step.setDescriptionHtml("Turn <b>left</b> at <b>A702/<wbr/>Home St</b>");
		if (step.isLongEnough())
			System.out.println(step.guessRoad());
	}

}
