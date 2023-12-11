/*
 * Copyright: Tony Mattheys
 */
package com.mattheys;

/**
 * @author Tony Mattheys
 *
 */
public class DeclinationTableEntry {

	public double latitude ;
	public double longitude ;
	public double declination ;
	
	public DeclinationTableEntry(double lat, double lon, double dec) {
		latitude = lat ;
		longitude = lon ;
		declination = dec ;
	}
}