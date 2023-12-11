/*
 * This Java source file was generated by the Gradle 'init' task.
 */
package com.mattheys;

import org.junit.Test;
import static org.junit.Assert.*;

public class VesselSimulatorTest {

	static Simulation sim = new Simulation();

	@Test
	public void testSimulationSetup() {
		sim.setUDPPort(12345);
		sim.setBoatHeading(45.0);
		sim.setLandWindSpeed(10.0);
		sim.setLandWindDirection(250.0);
		sim.setCurrentSpeed(1.5);
		sim.setCurrentDirection(120);
		sim.setLatitude(49.0);
		sim.setLongitude(-123.4);
	}

	@Test
	public void testUpdateNavigationParameters() {
		sim.updateNavigationParameters();
	}

	@Test
	public void testSendMessage() {
		assertEquals(true, sim.sendMessage("Fred")); 
	}

	@Test
	public void testSetandGetLatandLong() {
		sim.setLatitude(123.456789);
		assertEquals((int) 123456789, (int) (sim.getLatitude() * 1000000));
		sim.setLongitude(12.345678);
		assertEquals((int) 12345678, (int) (sim.getLongitude() * 1000000));
	}

	@Test
	public void testSetandGetCurrent() {
		sim.setCurrentSpeed(1.5);
		sim.setCurrentDirection(345);
		assertEquals((int) 15, (int) (sim.getCurrentSpeed() * 10));
		assertEquals((int) 345, (int) sim.getCurrentDirection());
	}
}
