/*
 * Copyright: Tony Mattheys
 */
package com.mattheys;

import java.io.Reader;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

/**
 * @author Tony Mattheys
 */
public class PolarCSVFile {

    private List<CSVRecord> records;
    private CSVRecord header;
    // static final ClassLoader loader = ;
	static Logger logger ;

    public PolarCSVFile(Reader polarFileReader) {
		logger = Logger.getLogger(getClass().getName()) ;
		logger.setLevel(Level.OFF);
		logger.info("Logging is all set up in " + PolarCSVFile.class.getName()) ;
		
        try {
            CSVParser parser = CSVParser.parse(polarFileReader, CSVFormat.DEFAULT);
            records = parser.getRecords();
            header = records.get(0);
            parser.close();

        } catch (Exception e) {
            logger.severe("Exception trying to open the Polar CSV file: " + e.getMessage());
        }
    }

    public double getPolarBoatSpeed(double TWS, double TWA) {
        /**
         * Find the two TWS columns above and below the given value. If
         * the value is exactly one of the TWS values in the table then
         * x and y will both be set to the same column number
         */
        int x = 1;
        int y = header.size() - 1;
        boolean found = false;
        for (int i = 1; i < header.size(); i++) {
            if (Double.parseDouble(header.get(i)) <= TWS) {
                x = i;
            }
            if (Double.parseDouble(header.get(i)) >= TWS && !found) {
                y = i;
                found = true;
            }
        }
        /**
         * Now find the two rows with TWA values above and below the given TWA.
         * If the given TWA is exactly the same as a particular row then m and n
         * will both be set to the same row number.
         */
        int m = 1;
        int n = records.size() - 1;
        found = false;
        for (int i = 1; i < records.size(); i++) {
            if (Double.parseDouble(records.get(i).get(0)) <= TWA) {
                m = i;
            }
            if (Double.parseDouble(records.get(i).get(0)) >= TWA && !found) {
                n = i;
                found = true;
            }
        }
        /**
         * All of this mess below basically just takes the four (or so) values
         * that lie above and below the TWA and above and below the TWS. Then
         * we interpolate between the four values to get an approximation of the
         * boat speed. m and n are the two rows which bracket the TWA value and
         * x and y are the two columns that bracket the TWS value. We need to make
         * sure that we don't divide by zero anywhere so we check for the special
         * case where we are exactly on a line or a column (or both).
         */
        double f1 = 0.0;
        if (Double.parseDouble(header.get(x)) != Double.parseDouble(header.get(y))) {
            f1 = (TWS - Double.parseDouble(header.get(x))) / (Double.parseDouble(header.get(y)) - Double.parseDouble(header.get(x)));
        }
        double s1 = Double.parseDouble(records.get(m).get(x)) + f1 * (Double.parseDouble(records.get(m).get(y)) - Double.parseDouble(records.get(m).get(x)));
        double s2 = Double.parseDouble(records.get(n).get(x)) + f1 * (Double.parseDouble(records.get(n).get(y)) - Double.parseDouble(records.get(n).get(x)));

        double f2 = 0.0;
        if (Double.parseDouble(records.get(m).get(0)) != Double.parseDouble(records.get(n).get(0))) {
            f2 = (TWA - Double.parseDouble(records.get(m).get(0))) / (Double.parseDouble(records.get(n).get(0)) - Double.parseDouble(records.get(m).get(0)));
        }
        double s3 = s1 + f2 * (s2 - s1);
        return s3;
    }
}