package core;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class ViennaPublicTraficLiveTickerTest {

	ViennaPublicTrafficLiveTicker ticker = null;
	
	@Before
	public void setUp() throws Exception {
		ticker = new ViennaPublicTrafficLiveTicker();
	}

	@Test
	public void testBuildURL() {
		
		String actual = ticker.buildURL(123);
		String expected = "http://www.wienerlinien.at/ogd_realtime/monitor?rbl=123&sender=Aq5inVKiQsJwRm9c";
		
		assertEquals(expected, actual);
		
		actual = ticker.buildURL(null);
		expected = "http://www.wienerlinien.at/ogd_realtime/monitor?rbl=null&sender=Aq5inVKiQsJwRm9c";
		
		assertEquals(expected, actual);
		
	}

}
