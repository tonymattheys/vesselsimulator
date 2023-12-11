/*
 * Copyright: Tony Mattheys
 */
package com.mattheys;

import java.awt.geom.Point2D;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Calendar;
import java.util.Locale;
import java.util.Random;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;
/**
 *
 * @author tony
 */
public class Simulation {
	
    private PolarCSVFile polar;
    // private CompassDeviation compassDeviation;
    private CompassDeclination compassDeclination;
    private Coordinates coordinates;

    private static final double earthRadius = 3443.89849 ;

    private double phi ;
    private int sign ;
    private double timestamp ;

    private double bs = 0.0;
    private double hdg = 0.0;
    private double lat = 49.0;
    private double lon = -123.4;
    private double lwd = 0.0;
    private double lws = 0.0;
    private double cd = 0.0;
    private double cs = 0.0;
    private double twa = 0.0;
    private double twd = 0.0;
    private double tws = 0.0;
    private double awa = 0.0;
    private double awd = 0.0;
    private double aws = 0.0;
    private double cog = 0.0;
    private double sog = 0.0;
    private Random dpt = new Random() ;
    
    private int port = 10110 ;
	private InetAddress ipAddress = null;
	private DatagramSocket dgramSocket = null;
	
	Logger logger ;
	
    public Simulation() {
		logger = Logger.getLogger(getClass().getName()) ;
		logger.setLevel(Level.OFF);
		logger.info("Logging is all set up in " + Simulation.class.getName()) ;

		polar = new PolarCSVFile(new InputStreamReader(Simulation.class.getResourceAsStream("/PolarDiagram.csv")));

		// compassDeviation = new CompassDeviation();
        compassDeclination = new CompassDeclination();
        timestamp = Calendar.getInstance(TimeZone.getTimeZone("UTC")).getTimeInMillis();
    	coordinates = new Coordinates(1.0, 1.0);

    	try {
			dgramSocket = new DatagramSocket(port);
            dgramSocket.setBroadcast(true);
            ipAddress = InetAddress.getByName("255.255.255.255");
            logger.warning("UDP socket created." + dgramSocket + ipAddress + ":" + port);
		} catch (IOException ex) {
            logger.severe("Error creating UDP socket " + ex);
		}
    }

    public boolean sendMessage(String m) {
        logger.info("sendMessage ==> " + m);
        byte[] sendData = m.concat("\r\n").getBytes();
        DatagramPacket udpPacket = new DatagramPacket(sendData, sendData.length, ipAddress, port);
        try {
			dgramSocket.send(udpPacket);
		} catch (IOException ex) {
            logger.severe("Error " + ex);
		}
		return true;
	}

    public void setUDPPort(int p) {
        logger.info("Set UDP port to ==> " + p);
        port = p;
    }

    public void setBoatHeading(double h) {
        logger.info("Set Boat Heading to ==> " + h);
    	if (h >= 360) {
    		hdg = h - 360 ;
    	} else if (h < 0) {
    		hdg = 360 + h ;
    	} else {
    		hdg = h;
    	}
        logger.info("Final Heading is ==> " + hdg);
    }

    public void setLatitude(double l) {
        lat = l;
    }

    public void setLongitude(double l) {
        lon = l;
    }

    public void setLandWindSpeed(double s) {
        lws = s;
    }

    public void setLandWindDirection(double d) {
        lwd = d;
    }

    public void setCurrentSpeed(double s) {
        cs = s;
    }

    public void setCurrentDirection(double d) {
        cd = d;
    }

    public double getBoatHeading() {
        return hdg;
    }

    public double getBoatSpeed() {
        return bs;
    }

    public double getCurrentSpeed() {
        return cs;
    }

    public double getCurrentDirection() {
        return cd;
    }

    public double getLandWindSpeed() {
        return lws;
    }

    public double getLandWindDirection() {
        return lwd;
    }

    public double getTrueWindSpeed() {
        return tws;
    }

    public double getTrueWindDirection() {
        return twd;
    }

    public double getTrueWindAngle() {
        return twa;
    }

    public double getApparentWindSpeed() {
        return aws;
    }

    public double getApparentWindDirection() {
        return awd;
    }

    public double getApparentWindAngle() {
        return awa;
    }

    public double getCourseOverGround() {
        return cog;
    }

    public double getSpeedOverGround() {
        return sog;
    }

    public double getLatitude() {
        return lat;
    }

    public double getLongitude() {
        return lon;
    }

    /**
     * Calculate checksums for NMEA 0183 sentences. basically what you do is to
     * XOR every byte starting from the second (the one after the "$") So - take
     * the second byte, XOR with third, then XOR the result with fourth and so
     * on until done. Return the two-digit hex value of the checksum as a
     * string.
     *
     * @param sentence
     * @return String
     */
    public String getNMEAChecksum(String sentence) {
		byte[] byteArray = sentence.getBytes();
        byte cksum = byteArray[1];
        for (int i = 2; i < byteArray.length; i++) {
            cksum = (byte) (cksum ^ byteArray[i]);
        }
        return String.format("%02X", cksum);
    }

    /**
     * Convert lat/long from decimal degrees to the format expected in NMWEA
     * 0183 sentences. We calculate to four decimal places but three would
     * probably be more than enough.
     *
     * @param L
     * @return String
     */
    public static String decimalToGPS(double L) {
        return String.format(Locale.US, "%.4f", ((int) Math.abs(L) * 100) + ((Math.abs(L) - (int) Math.abs(L)) * 60));
    }

    public void updateNavigationParameters() {
        logger.info("land wind speed is " + String.format("%.1f", lws));
        logger.info("land wind direction is " + String.format("%.0f", lwd));
        logger.info("current speed is " + String.format("%.1f", cs));
        logger.info("current direction is " + String.format("%.0f", cd));

        Point2D.Double truewind = new Point2D.Double(
        		coordinates.toCartesian(lws, lwd).x + coordinates.toCartesian(cs, cd).x,
        		coordinates.toCartesian(lws, lwd).y + coordinates.toCartesian(cs, cd).y
        		) ;
        tws = coordinates.toPolar(truewind.x, truewind.y).getX();
        twd = coordinates.toPolar(truewind.x, truewind.y).getY();

        logger.info("true wind velocity vector is " + truewind + " (vector sum of land wind plus current)");
        logger.info("true wind speed is " + String.format("%.1f", tws));
        logger.info("true wind direction is " + String.format("%.0f", twd));
        /**
         * Calculate TWA accounting for wraparound.
         */
        phi = Math.abs(hdg - twd) % 360;
        twa = phi > 180 ? 360 - phi : phi;
        sign = (hdg - twd >= 0 && hdg - twd <= 180) || (hdg - twd <= -180 && hdg - twd >= -360) ? -1 : 1;
        twa *= sign;

        logger.info("true heading is " + String.format("%.0f", hdg));
        logger.info("subtract true wind direction " + String.format("%.0f", twd));
        logger.info("so true wind angle is " + String.format("%.0f", twa) + " (difference between heading and true wind direction)");

        bs = polar.getPolarBoatSpeed(tws, Math.abs(twa));

        logger.info("boat speed through water from polars is " + String.format("%.1f", bs));

        Point2D.Double apparentwind = new Point2D.Double(
        		coordinates.toCartesian(bs, hdg).x + truewind.x,
        		coordinates.toCartesian(bs, hdg).y + truewind.y
        		);
        aws = coordinates.toPolar(apparentwind.getX(), apparentwind.getY()).getX();
        awd = coordinates.toPolar(apparentwind.getX(), apparentwind.getY()).getY();
        /**
         * Calculate AWA accounting for wraparound.
         */
        phi = Math.abs(hdg - awd) % 360;
        awa = phi > 180 ? 360 - phi : phi;
        sign = (hdg - awd >= 0 && hdg - awd <= 180) || (hdg - awd <= -180 && hdg - awd >= -360) ? -1 : 1;
        awa *= sign;

        logger.info("apparent wind velocity vector is " + apparentwind);
        logger.info("apparent wind speed is " + String.format("%.1f", aws));
        logger.info("apparent wind direction is " + String.format("%.0f", awd));

        logger.info("subtract apparent wind direction " + String.format("%.0f", awd));
        logger.info("so apparent wind angle is " + String.format("%.0f", awa) + " (difference between heading and true wind direction)");

        /**
         * Get COG and SOG as vector sum of boat velocity and current velocity
         * Note that this ignores leeway but should be OK for our purposes
         */
        Point2D.Double gs = new Point2D.Double (
        		coordinates.toCartesian(bs, hdg).x + coordinates.toCartesian(cs, cd).x,
        		coordinates.toCartesian(bs, hdg).y + coordinates.toCartesian(cs, cd).y
        		);
        cog = coordinates.toPolar(gs.x, gs.y).getY();
        sog = coordinates.toPolar(gs.x, gs.y).getX();

        logger.info("ground velocity is " + gs + " (vector sum of boat velocity plus current velocity)");
        logger.info("ground speed is " + String.format("%.1f", sog));
        logger.info("course over ground is " + String.format("%.0f", cog));

        /**
         * Here we need to update the lat/lon periodically to reflect any
         * movement during the last interval
         */
        /**
         * To find the lat/lon of a point on true course t, distance d from
         * (p1,l1) all in RADIANS along a rhumbline (initial point cannot be a
         * pole!):
         *
         * This calculation assumes a spherical earth and is quite accurate for
         * our purposes
         *
         * Formula: φ2 = asin( sin(φ1)*cos(d/R) + cos(φ1)*sin(d/R)*cos(θ) ) λ2 =
         * λ1 + atan2( sin(θ)*sin(d/R)*cos(φ1), cos(d/R)−sin(φ1)*sin(φ2) ) where
         * φ is latitude (in radians) λ is longitude (in radians) θ is the
         * bearing (in radians, clockwise from north) d is the distance
         * travelled (say, nautical miles) R is the earth’s radius in same units
         * as d (say, 3443.89849 nautical miles) (d/R is the angular distance,
         * in radians)
         *
         */
        logger.info("starting latitude: " + String.format("%.6f", lat));
        logger.info("starting longitude: " + String.format("%.6f", lon));

        double interval = Calendar.getInstance(TimeZone.getTimeZone("UTC")).getTimeInMillis() - timestamp ;
        timestamp = Calendar.getInstance(TimeZone.getTimeZone("UTC")).getTimeInMillis();

        logger.info("time interval for this calculation: " + String.format("%.2f", interval) + " milliseconds");

        double displacement = sog * interval / 1000.0 / 3600.0 ;

        logger.info("displacement is " + String.format("%.10f", displacement) + " nautical miles");

        double p2 = Math.asin(
        		Math.sin(Math.toRadians(lat)) * 
        		Math.cos(displacement / earthRadius) + 
        		Math.cos(Math.toRadians(lat)) * 
        		Math.sin(displacement / earthRadius) * 
        		Math.cos(Math.toRadians(cog))) ;
        lat = Math.toDegrees(p2);

        double l2 = Math.toRadians(lon) + 
        			Math.atan2(Math.sin(Math.toRadians(hdg)) * 
        			Math.sin(displacement / earthRadius) * 
        			Math.cos(Math.toRadians(lat)), Math.cos(displacement / earthRadius) - 
        			Math.sin(Math.toRadians(lat)) * 
        			Math.sin(p2)) ;
        lon = Math.toDegrees(l2);

        logger.info("ending latitude: " + String.format("%.6f", lat));
        logger.info("ending longitude: " + String.format("%.6f", lon));
        
        /*****************************************************************************************************
         *
         *  The rest of this section builds various NMEA sentences and dumps them onto the network
         *  
         *****************************************************************************************************/
        
        /**
         * Create a fake $GPGSA sentence saying we used satellites 16, 18, 22 and 24 and...
         * Dump out some fake "Satellites in View" sentences (GPGSV) Satellites
         * 16, 18, 22 and 24 will appear to be OK (non-zero dB SNR) The rest
         * (03, 04, 06, 13,14, 19, 27) will not (dB SNR value is zero)
         */
        String sentence = "$GPGSA,A,3,16,18,,22,,,24,,,,,,2.5,1.3,2.1" ;
        sendMessage(sentence + "*" + getNMEAChecksum(sentence) + "\r\n"); 
        sendMessage("$GPGSV,3,1,11,03,03,111,00,04,15,270,00,06,01,010,00,13,06,292,00*74" + "\r\n");
        sendMessage("$GPGSV,3,2,11,14,25,170,00,16,57,208,39,18,67,296,40,19,40,246,00*74" + "\r\n");
        sendMessage("$GPGSV,3,3,11,22,42,067,42,24,14,311,43,27,05,244,00,,,,*4D" + "\r\n");
        
        /**
         * Dump out a fake "DPT" sentence showing the depth to be somewhere between 10-20 metres
         * We initialize the depth to 10 metres and then just add a random amount between 0 and 5 metres
         */
        logger.info("Generating fake Depth sentence.");
        sentence = "$SDDPT," + String.format("%.2f", 10.0 + (0.5 - dpt.nextDouble())*5.0) + ",-2.2,250";
        sendMessage(sentence + "*" + getNMEAChecksum(sentence) + "\r\n");       
        logger.info("===> " + sentence + "*" + getNMEAChecksum(sentence));
        
        /**
         * Do a $GPGLL sentence which basically gives you Lat/Long and UTC time
         */
        sentence = "$GPGLL," + decimalToGPS(lat);
        if (lat > 0) {
            sentence = sentence + ",N";
        } else {
            sentence = sentence + ",S";
        }
        sentence = sentence + "," + decimalToGPS(lon);
        if (lon > 0) {
            sentence = sentence + ",E";
        } else {
            sentence = sentence + ",W";
        }
        Calendar c = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        sentence = sentence + "," + String.format("%tH%tM%tS", c, c, c) + ",A";
        sendMessage(sentence + "*" + getNMEAChecksum(sentence) + "\r\n");
        logger.info("===> " + sentence + "*" + getNMEAChecksum(sentence));
        
        /**
         * $GPGGA,123519,4807.038,N,01131.000,E,1,08,0.9,545.4,M,46.9,M,,*47
         *
         * Where:
         * GGA Global Positioning System Fix Data
         * 123519 Fix taken at 12:35:19 UTC
         * 4807.038,N Latitude 48 deg 07.038' N
         * 01131.000,E Longitude 11 deg 31.000' E
         * 1 Fix quality: 0 = invalid
         * 1 = GPS fix (SPS)
         * 2 = DGPS fix
         * 3 = PPS fix
         * 4 = Real Time Kinematic
         * 5 = Float RTK
         * 6 = estimated (dead reckoning) (2.3 feature)
         * 7 = Manual input mode
         * 8 = Simulation mode
         * 08 Number of satellites being tracked
         * 0.9 Horizontal dilution of position
         * 545.4,M Altitude, Meters, above mean sea level
         * 46.9,M Height of geoid (mean sea level) above WGS84
         * ellipsoid
         * (empty field) time in seconds since last DGPS update
         * (empty field) DGPS station ID number
         * 47 the checksum data, always begins with *
         *
         */
        sentence = "$GPGGA," + String.format("%tH%tM%tS", c, c, c) + "," + decimalToGPS(lat);
        if (lat > 0) {
            sentence = sentence + ",N";
        } else {
            sentence = sentence + ",S";
        }
        sentence = sentence + "," + decimalToGPS(lon);
        if (lon > 0) {
            sentence = sentence + ",E";
        } else {
            sentence = sentence + ",W";
        }
        sentence = sentence + ",1,12,1.5,0.0,M,0.0,M,";
        sendMessage(sentence + "*" + getNMEAChecksum(sentence) + "\r\n");
        logger.info("===> " + sentence + "*" + getNMEAChecksum(sentence));
        
        /**
         * Do a $IIVHW sentence.
         *
         * VHW - Water speed and heading
         * $--VHW,x.x,T,x.x,M,x.x,N,x.x,K*hh<CR><LF>
         * Field Number:
         * Degrees True (omitted by Raymarine)
         * T = True
         * Degrees Magnetic
         * M = Magnetic
         * Knots (speed of vessel relative to the water)
         * N = Knots
         * Kilometers (speed of vessel relative to the water)
         * K = Kilometers
         * Checksum
         */
        // double hdgm = hdg + compassDeclination.FindDeclination(lat, lon) + compassDeviation.FindDeviation(hdg);
        double hdgm = hdg - compassDeclination.FindDeclination(lat, lon) ;
        if (hdgm > 360) hdgm = hdgm - 360 ;
        if (hdgm < 0) hdgm = 360 + hdgm ;
        //sentence = "$IIVHW," + String.format(Locale.US, "%3.1f", hdg) + ",T," + String.format(Locale.US, "%3.1f", hdgm) + ",M," + String.format(Locale.US, "%3.1f", bs) + ",N," + String.format(Locale.US, "%3.1f", bs * 1.852) + ",K";
        sentence = "$IIVHW,,T," + String.format(Locale.US, "%3.1f", hdgm) + ",M," + String.format(Locale.US, "%3.1f", bs) + ",N," + String.format(Locale.US, "%3.1f", bs * 1.852) + ",K";
        sendMessage(sentence + "*" + getNMEAChecksum(sentence) + "\r\n");
        logger.info("===> " + sentence + "*" + getNMEAChecksum(sentence));
        
        /**
         * Do a $IIVTG sentence, very easy.
         * VTG - Velocity made good. The GPS receiver may use the LC prefix
         * instead of GP if it is emulating Loran output.
         *
         * $IIVTG,054.7,T,034.4,M,005.5,N,010.2,K*48
         *
         * where:
         * VTG Track made good and ground speed
         * 054.7,T True track made good (degrees)
         * 034.4,M Magnetic track made good (Raymarine leaves this out)
         * 005.5,N Ground speed, knots
         * 010.2,K Ground speed, Kilometers per hour
         * 48 Checksum
         *
         *
         */
        sentence = "$IIVTG," + String.format(Locale.US, "%3.1f", cog) + ",T,,M," + String.format(Locale.US, "%3.1f", sog) + ",N," + String.format(Locale.US, "%3.1f", sog * 1.852) + ",K,A";
        sendMessage(sentence + "*" + getNMEAChecksum(sentence) + "\r\n");
        logger.info("===> " + sentence + "*" + getNMEAChecksum(sentence));
        
        /**
         * Do a $GPVDR sentence (Current Set and Drift) as follows: VDR Set and
         * Drift
         * $--VDR,x.x,T,x.x,M,x.x,N*hh
         * 1) Degrees True
         * 2) T = True
         * 3) Degrees Magnetic
         * 4) M = Magnetic
         * 5) Knots (speed of current)
         * 6) N = Knots
         * 7) Checksum
         *
         * Raymarine does not send this so I am taking it out for now.
         * 
	        double currentSetM = cd + compassDeclination.FindDeclination(lat, lon);
	        sentence = "$GPVDR," + String.format(Locale.US, "%3.1f", cd) + ",T," + String.format(Locale.US, "%3.1f", currentSetM) + ",M," + String.format(Locale.US, "%3.1f", cs) + ",N";
	        nmeaServer.sendMessage(sentence + "*" + getNMEAChecksum(sentence) + "\r\n");
         */

        /**
         * Do a $HCHDG sentence, also very easy.
         * HDG - Magnetic heading, deviation, variation
         * 	Magnetic Sensor heading in degrees
         * 	Magnetic Deviation, degrees (omitted by Raymarine)
         * 	Magnetic Deviation direction, E = Easterly, W = Westerly
         * 	Magnetic Variation degrees
         * 	Magnetic Variation direction, E = Easterly, W = Westerly
         */
        logger.info("True Heading: " + String.format("%.0f", hdg));
        logger.info("Compass Heading: " + String.format("%.0f", hdgm));
        logger.info("Magnetic Variation: " + String.format("%.1f", compassDeclination.FindDeclination(lat, lon)));
        // logger.info("Compass deviation: " + String.format("%.0f", compassDeviation.FindDeviation(cog)));
        sentence = "$HCHDG," + String.format(Locale.US, "%3.1f", hdgm) + ",,," + String.format(Locale.US, "%3.1f", Math.abs(compassDeclination.FindDeclination(lat, lon))) ;
        if (compassDeclination.FindDeclination(lat, lon) >=  0 ) {
        	sentence = sentence + ",E" ;
        } else {
        	sentence = sentence + ",W" ;
        }
        sendMessage(sentence + "*" + getNMEAChecksum(sentence) + "\r\n");
        logger.info("===> " + sentence + "*" + getNMEAChecksum(sentence));
        
        /**
         * $WIMWV sentences for true and apparent winds.
         *
         * For true and apparent wind we have to convert it to bearing (0-360)
         * relative to the boat itself. Since port apparent wind is negative
         * we have to adjust it for wind on port side by adding it to 360 degrees.
         */

        /** REMOVE TRUE WIND - Raymarine doesn't send it unless you have an instrument to read it
        double twa1 = twa;
        if (twa1 < 0.0) {
            twa1 = 360 + twa1;
        }
        sentence = "$WIMWV," + String.format(Locale.US, "%3.0f", twa1) + ",T," + String.format(Locale.US, "%2.1f", tws) + ",N,A";
        nmeaServer.sendMessage(sentence + "*" + getNMEAChecksum(sentence) + "\r\n");
        */

        double awa1 = awa;
        if (awa1 < 0.0) {
            awa1 = 360 + awa1;
        }
        sentence = "$WIMWV," + String.format(Locale.US, "%.2f", awa1) + ",R," + String.format(Locale.US, "%.2f", aws) + ",N,A";
        sendMessage(sentence + "*" + getNMEAChecksum(sentence) + "\r\n");
        logger.info("===> " + sentence + "*" + getNMEAChecksum(sentence));
        
        /**
         * $WIVWR,154.1,L,2.3,N,1.2,M,4.3,K*4D
         */
        double mps = aws * 0.5144447 ;
        double kmh = aws * 1.852000925 ;
        sentence = "$WIVWR," ;
        if (awa < 0) {
        	sentence = sentence + String.format(Locale.US, "%.1f", Math.abs(awa)) + ",L," + String.format(Locale.US, "%2.1f", aws) ;
        } else {
        	sentence = sentence + String.format(Locale.US, "%.1f", Math.abs(awa)) + ",R," + String.format(Locale.US, "%2.1f", aws) ;
        }
        sentence = sentence + ",N," + String.format(Locale.US, "%.1f", Math.abs(mps)) + ",M" ;
        sentence = sentence + "," + String.format(Locale.US, "%.1f", Math.abs(kmh)) + ",K" ;
        sendMessage(sentence + "*" + getNMEAChecksum(sentence) + "\r\n");
        logger.info("===> " + sentence + "*" + getNMEAChecksum(sentence));
        
        /**
         * $--RMC,hhmmss.ss,A,llll.ll,a,yyyyy.yy,a,x.x,x.x,xxxx,x.x,a*hh
         * 1) Time (UTC)
         * 2) Status, V = Navigation receiver warning
         * 3) Latitude
         * 4) N or S
         * 5) Longitude
         * 6) E or W
         * 7) Speed over ground, knots
         * 8) Track made good, degrees true
         * 9) Date, ddmmyy
         * 10) Magnetic Variation, degrees
         * 11) E or W
         * 12) Checksum
         */
        sentence = "$GPRMC," + String.format("%tH%tM%tS", c, c, c) + ",A," + decimalToGPS(lat);
        if (lat > 0) {
            sentence = sentence + ",N";
        } else {
            sentence = sentence + ",S";
        }
        sentence = sentence + "," + decimalToGPS(lon);
        if (lon > 0) {
            sentence = sentence + ",E";
        } else {
            sentence = sentence + ",W";
        }
        sentence = sentence + "," + String.format(Locale.US, "%3.1f", sog) + "," + String.format(Locale.US, "%3.1f", cog) + "," + String.format("%td%tm%ty", c, c, c) + "," + String.format(Locale.US, "%3.1f", Math.abs(compassDeclination.FindDeclination(lat, lon)));
        if (compassDeclination.FindDeclination(lat, lon) > 0) {
            sentence = sentence + ",E";
        } else {
            sentence = sentence + ",W";
        }
        sendMessage(sentence + "*" + getNMEAChecksum(sentence) + "\r\n");
        logger.info("===> " + sentence + "*" + getNMEAChecksum(sentence));
    }
}
