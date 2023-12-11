package com.mattheys;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

public class HelloWorldSwing {

	static JFrame frame;

	static Logger logger;

	private static ImageIcon drawCompass(double dir) {
		BufferedImage myPicture = null;
		try {
			myPicture = ImageIO.read(HelloWorldSwing.class.getResourceAsStream("/Compass_Card_BW.png"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Coordinates coords = new Coordinates(myPicture.getWidth(), myPicture.getHeight());
		Graphics2D g = (Graphics2D) myPicture.getGraphics();
		g.setStroke(new BasicStroke(5));
		g.setColor(Color.BLUE);
		Point2D.Double lineEnd = coords.toCartesian(myPicture.getWidth()/2, dir) ;
		logger.info("Line end is at " + lineEnd) ;
		g.drawLine(myPicture.getWidth()/2, myPicture.getHeight()/2, (int) (myPicture.getWidth()/2 + lineEnd.x), (int) (myPicture.getHeight()/2 - lineEnd.y));

		logger.info("Drawing line from X=" + myPicture.getWidth()/2) ;
		logger.info("Drawing line from Y=" + myPicture.getHeight()/2) ;
		logger.info("Drawing line to   X=" + (int) lineEnd.x) ;
		logger.info("Drawing line to   Y=" + (int) lineEnd.y) ;

		return new ImageIcon(myPicture);
	}

	/**
	 * Create the GUI and show it. For thread safety, this method should be invoked
	 * from the event-dispatching thread.
	 */
	private static void createAndShowGUI() {
		frame = new JFrame();
		frame.setBounds(100, 100, 645, 28);
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

		JLabel compassRose = new JLabel("");
		compassRose.setIcon(drawCompass(0));
		compassRose.setHorizontalAlignment(SwingConstants.CENTER);

		compassRose.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				Coordinates rosecoords = new Coordinates(compassRose.getSize().width, compassRose.getSize().height);
				Point localXY = rosecoords.getLocalCoordinates(e.getX(), e.getY());
				logger.info("local X = " + localXY.x + " Local Y = " + localXY.y);
				logger.info("HDG = " + rosecoords.getAngle(localXY.x, localXY.y));
				compassRose.setIcon(drawCompass(rosecoords.getAngle(localXY.x, localXY.y)));
			}
		});

		frame.getContentPane().add(compassRose, BorderLayout.CENTER);

		// Display the window.
		frame.pack();
		frame.setVisible(true);
	}

	public static void main(String[] args) {
		logger = Logger.getLogger("com.mattheys.HelloWorldSwing");
		logger.setLevel(Level.ALL);
		logger.info("Entering main()");

		// Schedule a job for the event-dispatching thread:
		// creating and showing this application's GUI.
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				createAndShowGUI();
			}
		});
		logger.info("Exiting main()");
	}
}