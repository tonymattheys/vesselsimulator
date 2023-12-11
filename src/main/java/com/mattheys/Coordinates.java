/*
 * Copyright: Tony Mattheys
 */
package com.mattheys;

import java.awt.Point;
import java.awt.geom.Point2D;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author tony
 *
 */
public class Coordinates {

    private double xOrigin = 0.0;
    private double yOrigin = 0.0;
	static Logger logger ;


    /**
     *
     * @param maxX the maximum X dimension of the control in screen pixels
     * @param maxY the maximum Y dimension of the control in screen pixels
     * 
     * Initializes the object's local origin to the centre of the coordinate space 
     * described by the maximum X and Y coordinates as understood by Java. 
     * See drawing below:
     * 
     *    (0, 0)           (maxX, 0)
     *      +-------------------+
     *      |                   |
     *      .                   .
     *      . xOrigin, yOrigin) .
     *      .         X         .
     *      .                   .
     *      |                   |
     *      +-------------------+
     *    (0, maxY)        (maxX, maxY) 
     */
    public Coordinates(double maxX, double maxY) {
		logger = Logger.getLogger(getClass().getName()) ;
		logger.setLevel(Level.OFF);
		logger.info("Logging is all set up in " + Coordinates.class.getName()) ;
		
        xOrigin = maxX / 2;
        yOrigin = maxY / 2;
    }

    /**
     *
     * @param javaX the X coordinate of a point inside a control using the control's 
     * Java coordinate system. Top left is (0, 0) and coordinates increase from
     * left to right and top to bottom to (maxX, maxY).
     * @param javaY the Y coordinate of a point inside a control using the control's 
     * Java coordinate system. Top left is (0, 0) and coordinates increase from
     * left to right and top to bottom to (maxX, maxY).
     * @return a Point object describing the coordinates of that point in a 
     * local coordinate system whose origin is at the centre of the control.
     * 
     */
    public Point getLocalCoordinates(double javaX, double javaY) {
        return new Point((int) (javaX - xOrigin), (int) (yOrigin - javaY));
    }

    /**
     *
     * @param X the X coordinate previously obtained by a getLocalCoordinates call
     * @param Y the Y coordinate previously obtained by a getLocalCoordinates call
     * @return the angle in degrees from an origin located at the midpoint of 
     * the control to the point (X, Y) supplied as a parameter
     */
    public double getAngle(double x, double y) {
		double r = Math.sqrt(x*x + y*y) ;
		logger.info("R = " + r) ;
		double theta = Math.toDegrees(Math.asin( x / r)) ;
		logger.info("Theta = " + theta) ;
		if (x >= 0) { // Either NE or SE quadrants
			if (y >= 0) {
				return theta ;
			} else {
				return (180 - theta) ;
			}
		} else { // SW or NW quadrants
			if (y >= 0) {
				return (360 + theta) ;
			} else {
				return (180 - theta) ;
			}
		}
    }
    
    /**
     *
     * @param magnitude Vector length referenced to the origin (0, 0) in arbitrary units
     * @param direction in DEGREES measured clockwise from zero (North) 
     * (i.e. East is 90, South is 180, West is 270)
     * @return the Cartesian coordinates of the tip of the vector referenced to 
     * the origin (0, 0)
     */
    public Point2D.Double toCartesian(double magnitude, double direction) {
        return new Point2D.Double(magnitude * Math.sin(Math.toRadians(direction)), magnitude * Math.cos(Math.toRadians(direction))) ;
    }
    
    /**
     *
     * @param X the x-coordinate of the desired point in arbitrary units
     * @param Y the y-coordinate of the desired point
     * @return Point2D containing the radius and angle of the point in polar 
     * coordinates expressed clockwise from zero (North) 
     * (i.e. East is 90, South is 180, West is 270)
     */
    public Point2D.Double toPolar(double X, double Y) {
        return new Point2D.Double(Math.sqrt(X*X + Y*Y), getAngle(X, Y)) ;
    }
}
