package com.mattheys;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.SystemColor;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;
import javax.swing.text.NumberFormatter;

public class VesselSimulator {

	private static final int SIM_INTERVAL = 1000; // milliseconds
	private static JFrame frame;
	private JTextField valGWD;
	private JTextField valGWS;
	private JTextField valSET;
	private JTextField valDFT;

	static Logger logger;
	static Simulation sim;

	private static ImageIcon drawCompass() {
		BufferedImage compassCard ;
		Point2D.Double lineEndPoint ;
		logger.info("Entering drawCmpass") ;
		try {
			compassCard = ImageIO.read(VesselSimulator.class.getResourceAsStream("/Compass_Card_BW.png"));
		} catch (IOException e) {
			e.printStackTrace();
			// Initialise to SOMETHING even if it's nonsense so we don't try referring to a null
			compassCard = new BufferedImage(null, null, false, null) ;
			compassCard.getScaledInstance(200, 200, 0) ;
		}
		logger.info("compassCard is " + compassCard) ;
		Point2D.Double origin = new Point2D.Double((double) compassCard.getWidth() / 2.0, (double) compassCard.getHeight() / 2.0) ;
		Coordinates coords = new Coordinates(compassCard.getWidth(), compassCard.getHeight());
		Graphics2D g = (Graphics2D) compassCard.getGraphics();

		// Now draw the heading vector
		g.setStroke(new BasicStroke(3));
		g.setColor(Color.MAGENTA);
		lineEndPoint = coords.toCartesian(0.9 * (double) compassCard.getWidth() / 2.0, sim.getBoatHeading());
		g.drawLine((int) origin.x, (int) origin.y, (int) (origin.x + lineEndPoint.x), (int) (origin.y - lineEndPoint.y));

		// And draw the True Wind
		g.setStroke(new BasicStroke(3));
		g.setColor(Color.YELLOW);
		lineEndPoint = coords.toCartesian(0.9 * (double) compassCard.getWidth() / 2.0, sim.getLandWindDirection());
		g.drawLine((int) origin.x, (int) origin.y, (int) (origin.x + lineEndPoint.x), (int) (origin.y - lineEndPoint.y));

		// And the Current
		g.setStroke(new BasicStroke(5));
		g.setColor(Color.BLUE);
		lineEndPoint = coords.toCartesian(0.45 * (double) compassCard.getWidth() / 2.0, sim.getCurrentDirection());
		g.drawLine((int) origin.x, (int) origin.y, (int) (origin.x + lineEndPoint.x), (int) (origin.y - lineEndPoint.y));

		logger.info("Leaving drawCmpass") ;
		return new ImageIcon(compassCard);
	}

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		logger = Logger.getLogger("com.mattheys.HelloApplicationWindow") ;
		logger.setLevel(Level.OFF);
		logger.info("Logging is all set up in " + VesselSimulator.class.getName()) ;

		sim = new Simulation();
		/**
		 * First we need to load the initial simulation properties from an (optional)
		 * external file. If the file name is specified in the JVM parameter
		 * property.file then we try to load that one, otherwise we assume the file is
		 * called simulation.properties in the current directory. Of course if there is
		 * no property file specified then we simply load a set of default parameters to
		 * start the simulation.
		 */
		String propertyfilename = System.getProperties().getProperty("property.file", "simulation.properties");
		logger.info("Property file is " + Paths.get("").toAbsolutePath().normalize().toString() + "/" + propertyfilename);
		Properties defaultProps = new Properties();
		try {
			defaultProps.load(
					new FileReader(Paths.get("").toAbsolutePath().normalize().toString() + "/" + propertyfilename));
		} catch (FileNotFoundException fnf) {
			logger.severe("No property file could be found in current directory \""	+ Paths.get("").toAbsolutePath().normalize().toString() + "\"");
			logger.info("All simulation parameters will be set to defaults.");
		} catch (IOException iox) {
			logger.severe("IO Exception: " + iox.getMessage());
			logger.info("All simulation parameters will be set to defaults.");
		} catch (NullPointerException npx) {
			logger.severe("Null Pointer Exception: " + npx.getMessage());
			logger.info("Generally this happens if there are empty lines in the property file.");
		}
		/**
		 * Set up the navigation simulation with initial parameters if specified in a
		 * property file or else just set some sane values to start with
		 */
		if (defaultProps.isEmpty()) {
			logger.info("Setting defaults for simulation paremeters.");
			sim.setUDPPort(10110);
			sim.setBoatHeading(45.0);
			sim.setLandWindSpeed(10.0);
			sim.setLandWindDirection(250.0);
			sim.setCurrentSpeed(1.5);
			sim.setCurrentDirection(120);
			sim.setLatitude(49.0);
			sim.setLongitude(-123.4);
		} else {
			logger.info("Getting simulation paremeters from property file.");
			try {
				if (defaultProps.getProperty("nmeaserver.port").trim() != null) {
					int port = Integer.parseInt(defaultProps.getProperty("nmeaserver.port").trim());
					sim.setUDPPort(port);
					logger.info("UDP port " + port);
				}
				if (defaultProps.getProperty("navigation.heading").trim() != null) {
					sim.setBoatHeading(Double.parseDouble(defaultProps.getProperty("navigation.heading").trim()));
					logger.info("Heading: " + sim.getBoatHeading());
				}
				if (defaultProps.getProperty("navigation.latitude").trim() != null) {
					sim.setLatitude(Double.parseDouble(defaultProps.getProperty("navigation.latitude").trim()));
					logger.info("Latitude: " + sim.getLatitude());
				}
				if (defaultProps.getProperty("navigation.longitude").trim() != null) {
					sim.setLongitude(Double.parseDouble(defaultProps.getProperty("navigation.longitude").trim()));
					logger.info("Longitude: " + sim.getLongitude());
				}
				if (defaultProps.getProperty("navigation.currentspeed").trim() != null) {
					sim.setCurrentSpeed(Double.parseDouble(defaultProps.getProperty("navigation.currentspeed").trim()));
					logger.info("Current speed: " + sim.getCurrentSpeed());
				}
				if (defaultProps.getProperty("navigation.currentdirection").trim() != null) {
					sim.setCurrentDirection(
							Double.parseDouble(defaultProps.getProperty("navigation.currentdirection").trim()));
					logger.info("Current direction: " + sim.getCurrentDirection());
				}
				if (defaultProps.getProperty("navigation.landwindspeed").trim() != null) {
					sim.setLandWindSpeed(
							Double.parseDouble(defaultProps.getProperty("navigation.landwindspeed").trim()));
					logger.info("Land wind speed: " + sim.getLandWindSpeed());
				}
				if (defaultProps.getProperty("navigation.landwinddirection").trim() != null) {
					sim.setLandWindDirection(
							Double.parseDouble(defaultProps.getProperty("navigation.landwinddirection").trim()));
					logger.info("Land wind direction: " + sim.getLandWindDirection());
				}
			} catch (Exception e) {
				logger.severe(
						"Oh no. Something went wrong when I tried to read the properties file. Can you have a look at it?");
			}
		}
		/**
		 * And update all parameters in the simulation run.
		 */
		sim.updateNavigationParameters();

		/**
		 * Set up the Event Queue
		 */
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					VesselSimulator window = new VesselSimulator();
					VesselSimulator.frame.setVisible(true);
				} catch (Exception e) {
					logger.warning(e.toString());
				}
			}
		});

		/**
		 * Then we enter a loop where we call sim.updateNavigationParameters()
		 * periodically to update everything and send out a NMEA sentence on the bus.
		 */
		while (true) {
			try {
				Thread.sleep(SIM_INTERVAL);
			} catch (InterruptedException ex) {
				logger.info("Interrupted while sleeping" + ex);
			}
			try {
				sim.updateNavigationParameters();
				Component[] contentpaneComponents = frame.getContentPane().getComponents();
				for (int i = 0; i < contentpaneComponents.length; i++) {
					if (contentpaneComponents[i] instanceof javax.swing.JPanel) {
						logger.info("JPANEL NAME - " + contentpaneComponents[i].getName());
						JPanel panel = (JPanel) contentpaneComponents[i];
						Component[] panelComponents = panel.getComponents();
						for (int j = 0; j < panelComponents.length; j++) {
							logger.info("JPanel Component " + j + " is " + panelComponents[j]);
							if (panelComponents[j] instanceof javax.swing.JTextField) {
								JTextField txtFld = (JTextField) panelComponents[j];
								if (txtFld.getName() != null) {
									logger.info("Text Field Name is " + txtFld.getName());
								}
							} else if (panelComponents[j] instanceof javax.swing.JLabel) {
								JLabel txtFld = (JLabel) panelComponents[j];
								logger.info("Label Field Name is \"" + txtFld.getName() + "\"");
								if (txtFld.getName() != null) {
									switch (txtFld.getName()) {
									case "varLatitude":
										txtFld.setText(String.format("%.6f", sim.getLatitude()));
										break;
									case "lblNorthSouth":
										if (sim.getLatitude() >= 0) {
											txtFld.setText("N");
										} else {
											txtFld.setText("S");
										}
										break;
									case "varLongitude":
										txtFld.setText(String.format("%.6f", sim.getLongitude()));
										break;
									case "lblEastWest":
										if (sim.getLongitude() >= 0) {
											txtFld.setText("E");
										} else {
											txtFld.setText("W");
										}
										break;
									case "valHDG":
										txtFld.setText(String.format("%.0f", sim.getBoatHeading()));
										break;
									case "valSTW":
										txtFld.setText(String.format("%.1f", sim.getBoatSpeed()));
										break;
									case "valCOG":
										txtFld.setText(String.format("%.0f", sim.getCourseOverGround()));
										break;
									case "valSOG":
										txtFld.setText(String.format("%.1f", sim.getSpeedOverGround()));
										break;
									case "valAWA":
										txtFld.setText(String.format("%.0f", sim.getApparentWindAngle()));
										break;
									case "valAWS":
										txtFld.setText(String.format("%.1f", sim.getApparentWindSpeed()));
										break;
									case "valTWA":
										txtFld.setText(String.format("%.0f", sim.getTrueWindAngle()));
										break;
									case "valTWS":
										txtFld.setText(String.format("%.1f", sim.getTrueWindSpeed()));
										break;
									default:
										break;
									}
								}
							}
						}
					} else if (contentpaneComponents[i] instanceof JLabel) {
						logger.info("JLABEL - " + contentpaneComponents[i].getClass() + "\tNAME - "
								+ contentpaneComponents[i].getName());
					}
				}
			} catch (Exception ex) {
				logger.severe("Updating Navigation Parameters and fell off." + ex);
			}
		}
		// logger.info("Leaving main()") ;
	}

	/**
	 * Create the application.
	 */
	public VesselSimulator() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.setBounds(100, 100, 600, 500);
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		/**
		 * Formatters for JFormattedTextFields
		 */
		NumberFormat decimalFormat = new DecimalFormat("0.0");
		NumberFormatter decimalFormatter = new NumberFormatter(decimalFormat);
		NumberFormat integerFormat = new DecimalFormat("000");
		NumberFormatter integerFormatter = new NumberFormatter(integerFormat);

		JLabel compassRose = new JLabel("");
		compassRose.setName("compassRose");
		// compassRose.setIcon(new
		// ImageIcon(VesselSimulator.class.getResource("/Compass_Card_BW.png")));
		compassRose.setIcon(drawCompass());
		compassRose.setHorizontalAlignment(SwingConstants.CENTER);
		compassRose.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				Coordinates rosecoords = new Coordinates(compassRose.getSize().width, compassRose.getSize().height);
				logger.info("Compass rose Width = " + compassRose.getSize().width + "\tHeight = "
						+ compassRose.getSize().height);
				logger.info("Clicked on compass rose");
				logger.info("Size is " + compassRose.getSize());
				Point localXY = rosecoords.getLocalCoordinates(e.getX(), e.getY());
				logger.info("X=" + localXY.x + " Y=" + localXY.y);

				sim.setBoatHeading(rosecoords.getAngle(localXY.x, localXY.y));
				compassRose.setIcon(drawCompass());

				logger.info("HDG = " + sim.getBoatHeading());
			}
		});
		frame.getContentPane().add(compassRose, BorderLayout.CENTER);

		JPanel panelNorth = new JPanel();
		panelNorth.setName("panelNorth");
		frame.getContentPane().add(panelNorth, BorderLayout.NORTH);
		panelNorth.setLayout(new GridLayout(0, 6, 10, 0));

		JLabel lblLatitude = new JLabel("Latitude");
		lblLatitude.setHorizontalAlignment(SwingConstants.RIGHT);
		panelNorth.add(lblLatitude);

		JLabel varLatitude = new JLabel(String.format("%.6f", sim.getLatitude()));
		varLatitude.setName("varLatitude");
		varLatitude.setHorizontalAlignment(SwingConstants.RIGHT);
		varLatitude.setAlignmentX(Component.CENTER_ALIGNMENT);
		panelNorth.add(varLatitude);

		JLabel lblNorthSouth = new JLabel("NS");
		lblNorthSouth.setName("lblNorthSouth");
		panelNorth.add(lblNorthSouth);

		JLabel lblLongitude = new JLabel("Longitude");
		lblLongitude.setHorizontalAlignment(SwingConstants.RIGHT);
		panelNorth.add(lblLongitude);

		JLabel varLongitude = new JLabel(String.format("%.6f", sim.getLongitude()));
		varLongitude.setName("varLongitude");
		varLongitude.setHorizontalAlignment(SwingConstants.RIGHT);
		panelNorth.add(varLongitude);

		JLabel lblEastWest = new JLabel("EW");
		lblEastWest.setName("lblEastWest");
		panelNorth.add(lblEastWest);

		JPanel panelSouth = new JPanel();
		panelSouth.setName("panelSouth");
		frame.getContentPane().add(panelSouth, BorderLayout.SOUTH);
		panelSouth.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));

		JButton btnLeft10 = new JButton("<< Left 10");
		btnLeft10.setFocusable(false);
		btnLeft10.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				logger.info("Left 10 degrees");
				sim.setBoatHeading(sim.getBoatHeading() - 10);
				compassRose.setIcon(drawCompass()) ;
			}
		});
		panelSouth.add(btnLeft10);

		JButton btnLeft1 = new JButton("< Left 1");
		btnLeft1.setFocusable(false);
		btnLeft1.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				logger.info("Left 1 degree");
				sim.setBoatHeading(sim.getBoatHeading() - 1);
				compassRose.setIcon(drawCompass()) ;
			}
		});
		btnLeft1.setActionCommand("left");
		panelSouth.add(btnLeft1);
		
		/**
		JButton btnUpdate = new JButton("??????");
		btnUpdate.setFocusable(false);
		btnUpdate.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				logger.info("Update");
			}
		});
		panelSouth.add(btnUpdate);
		*/

		JButton btnRight1 = new JButton("Right 1 >");
		btnRight1.setFocusable(false);
		btnRight1.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				logger.info("Right 1 degree");
				sim.setBoatHeading(sim.getBoatHeading() + 1);
				compassRose.setIcon(drawCompass()) ;
			}
		});
		panelSouth.add(btnRight1);

		JButton btnRight10 = new JButton("Right 10 >>");
		btnRight10.setFocusable(false);
		btnRight10.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				logger.info("Right 10 degrees");
				sim.setBoatHeading(sim.getBoatHeading() + 10);
				compassRose.setIcon(drawCompass()) ;
			}
		});
		panelSouth.add(btnRight10);

		JPanel panelWest = new JPanel();
		panelWest.setName("panelWest");
		frame.getContentPane().add(panelWest, BorderLayout.WEST);
		panelWest.setLayout(new GridLayout(0, 2, 10, 0));

		JLabel lblHDG = new JLabel("HDG");
		lblHDG.setHorizontalTextPosition(SwingConstants.LEFT);
		lblHDG.setHorizontalAlignment(SwingConstants.LEFT);
		panelWest.add(lblHDG);
		lblHDG.setFont(new Font("Lucida Grande", Font.BOLD, 13));

		JLabel valHDG = new JLabel(String.format("%.0f", sim.getBoatHeading()));
		valHDG.setName("valHDG");
		valHDG.setHorizontalTextPosition(SwingConstants.RIGHT);
		valHDG.setAlignmentX(Component.CENTER_ALIGNMENT);
		valHDG.setHorizontalAlignment(SwingConstants.RIGHT);
		valHDG.setFont(new Font("Lucida Grande", Font.PLAIN, 13));
		panelWest.add(valHDG);

		JLabel lblSTW = new JLabel("STW");
		lblSTW.setBackground(SystemColor.window);
		lblSTW.setHorizontalTextPosition(SwingConstants.LEFT);
		lblSTW.setHorizontalAlignment(SwingConstants.LEFT);
		lblSTW.setFont(new Font("Lucida Grande", Font.BOLD, 13));
		panelWest.add(lblSTW);

		JLabel valSTW = new JLabel(String.format("%.1f", sim.getBoatSpeed()));
		valSTW.setName("valSTW");
		valSTW.setHorizontalTextPosition(SwingConstants.RIGHT);
		valSTW.setAlignmentX(Component.CENTER_ALIGNMENT);
		valSTW.setHorizontalAlignment(SwingConstants.RIGHT);
		panelWest.add(valSTW);

		JLabel lblCOG = new JLabel("COG");
		lblCOG.setHorizontalTextPosition(SwingConstants.LEFT);
		lblCOG.setHorizontalAlignment(SwingConstants.LEFT);
		lblCOG.setFont(new Font("Lucida Grande", Font.BOLD, 13));
		panelWest.add(lblCOG);

		JLabel valCOG = new JLabel(String.format("%.0f", sim.getCourseOverGround()));
		valCOG.setName("valCOG");
		valCOG.setHorizontalTextPosition(SwingConstants.RIGHT);
		valCOG.setAlignmentX(Component.CENTER_ALIGNMENT);
		valCOG.setHorizontalAlignment(SwingConstants.RIGHT);
		panelWest.add(valCOG);

		JLabel lblSOG = new JLabel("SOG");
		lblSOG.setHorizontalTextPosition(SwingConstants.LEFT);
		lblSOG.setHorizontalAlignment(SwingConstants.LEFT);
		lblSOG.setFont(new Font("Lucida Grande", Font.BOLD, 13));
		panelWest.add(lblSOG);

		JLabel valSOG = new JLabel(String.format("%.1f", sim.getSpeedOverGround()));
		valSOG.setName("valSOG");
		valSOG.setHorizontalTextPosition(SwingConstants.RIGHT);
		valSOG.setAlignmentX(Component.CENTER_ALIGNMENT);
		valSOG.setHorizontalAlignment(SwingConstants.RIGHT);
		panelWest.add(valSOG);

		JLabel lblAWA = new JLabel("AWA");
		lblAWA.setHorizontalTextPosition(SwingConstants.LEFT);
		lblAWA.setHorizontalAlignment(SwingConstants.LEFT);
		lblAWA.setFont(new Font("Lucida Grande", Font.BOLD, 13));
		panelWest.add(lblAWA);

		JLabel valAWA = new JLabel(String.format("%.0f", sim.getApparentWindAngle()));
		valAWA.setName("valAWA");
		valAWA.setHorizontalTextPosition(SwingConstants.RIGHT);
		valAWA.setAlignmentX(Component.CENTER_ALIGNMENT);
		valAWA.setHorizontalAlignment(SwingConstants.RIGHT);
		panelWest.add(valAWA);

		JLabel lblAWS = new JLabel("AWS");
		lblAWS.setHorizontalTextPosition(SwingConstants.LEFT);
		lblAWS.setHorizontalAlignment(SwingConstants.LEFT);
		lblAWS.setFont(new Font("Lucida Grande", Font.BOLD, 13));
		panelWest.add(lblAWS);

		JLabel valAWS = new JLabel(String.format("%.1f", sim.getApparentWindSpeed()));
		valAWS.setName("valAWS");
		valAWS.setHorizontalTextPosition(SwingConstants.RIGHT);
		valAWS.setAlignmentX(Component.CENTER_ALIGNMENT);
		valAWS.setHorizontalAlignment(SwingConstants.RIGHT);
		panelWest.add(valAWS);

		JLabel lblTWA = new JLabel("TWA");
		lblTWA.setHorizontalTextPosition(SwingConstants.LEFT);
		lblTWA.setHorizontalAlignment(SwingConstants.LEFT);
		lblTWA.setFont(new Font("Lucida Grande", Font.BOLD, 13));
		panelWest.add(lblTWA);

		JLabel valTWA = new JLabel(String.format("%.0f", sim.getTrueWindAngle()));
		valTWA.setName("valTWA");
		valTWA.setHorizontalTextPosition(SwingConstants.RIGHT);
		valTWA.setAlignmentX(Component.CENTER_ALIGNMENT);
		valTWA.setHorizontalAlignment(SwingConstants.RIGHT);
		panelWest.add(valTWA);

		JLabel lblTWS = new JLabel("TWS");
		lblTWS.setHorizontalTextPosition(SwingConstants.LEFT);
		lblTWS.setHorizontalAlignment(SwingConstants.LEFT);
		lblTWS.setFont(new Font("Lucida Grande", Font.BOLD, 13));
		panelWest.add(lblTWS);

		JLabel valTWS = new JLabel(String.format("%.1f", sim.getTrueWindSpeed()));
		valTWS.setName("valTWS");
		valTWS.setBackground(Color.WHITE);
		valTWS.setAlignmentX(Component.CENTER_ALIGNMENT);
		valTWS.setHorizontalAlignment(SwingConstants.RIGHT);
		panelWest.add(valTWS);

		JPanel panelEast = new JPanel();
		panelEast.setName("panelEast");
		frame.getContentPane().add(panelEast, BorderLayout.EAST);
		panelEast.setLayout(new GridLayout(0, 2, 10, 0));

		JLabel lblGWD = new JLabel("GWD");
		lblGWD.setHorizontalTextPosition(SwingConstants.RIGHT);
		lblGWD.setHorizontalAlignment(SwingConstants.RIGHT);
		lblGWD.setFont(new Font("Lucida Grande", Font.BOLD, 13));
		panelEast.add(lblGWD);

		valGWD = new JFormattedTextField(integerFormatter);
		valGWD.setText(String.format("%.0f", sim.getLandWindDirection()));
		valGWD.setName("valGWD");
		valGWD.setSelectionColor(SystemColor.textHighlight);
		valGWD.addFocusListener(new FocusAdapter() {
			@Override
			public void focusGained(FocusEvent e) {
				valGWD.setBackground(SystemColor.text);
				valGWD.selectAll();
			}

			@Override
			public void focusLost(FocusEvent e) {
				valGWD.setBackground(SystemColor.window);
				sim.setLandWindDirection(Double.parseDouble(valGWD.getText()));
				compassRose.setIcon(drawCompass()) ;
			}
		});
		valGWD.setBackground(SystemColor.window);
		panelEast.add(valGWD);
		valGWD.setColumns(3);

		JLabel lblGWS = new JLabel("GWS");
		lblGWS.setHorizontalTextPosition(SwingConstants.RIGHT);
		lblGWS.setHorizontalAlignment(SwingConstants.RIGHT);
		lblGWS.setFont(new Font("Lucida Grande", Font.BOLD, 13));
		panelEast.add(lblGWS);

		valGWS = new JFormattedTextField(integerFormatter);
		valGWS.setText(String.format("%.0f", sim.getLandWindSpeed()));
		valGWS.setName("valGWS");
		valGWS.setSelectionColor(SystemColor.textHighlight);
		valGWS.addFocusListener(new FocusAdapter() {
			@Override
			public void focusGained(FocusEvent e) {
				valGWS.setBackground(SystemColor.text);
				valGWS.selectAll();
			}

			@Override
			public void focusLost(FocusEvent e) {
				logger.info("Focus lost on valGWS " + e.getComponent());
				valGWS.setBackground(SystemColor.window);
				sim.setLandWindSpeed(Double.parseDouble(valGWS.getText()));
				compassRose.setIcon(drawCompass()) ;
			}
		});
		valGWS.setBackground(SystemColor.window);
		panelEast.add(valGWS);
		valGWS.setColumns(3);

		JLabel lblSET = new JLabel("SET");
		lblSET.setHorizontalTextPosition(SwingConstants.RIGHT);
		lblSET.setHorizontalAlignment(SwingConstants.RIGHT);
		lblSET.setFont(new Font("Lucida Grande", Font.BOLD, 13));
		panelEast.add(lblSET);

		valSET = new JFormattedTextField(integerFormatter);
		valSET.setText(String.format("%.0f", sim.getCurrentDirection()));
		valSET.setName("valSET");
		valSET.setSelectionColor(SystemColor.textHighlight);
		valSET.addFocusListener(new FocusAdapter() {
			@Override
			public void focusGained(FocusEvent e) {
				valSET.setBackground(SystemColor.text);
				valSET.selectAll();
			}

			@Override
			public void focusLost(FocusEvent e) {
				logger.info("Focus lost on valSET " + e.getComponent());
				valSET.setBackground(SystemColor.window);
				sim.setCurrentDirection(Double.parseDouble(valSET.getText()));
				compassRose.setIcon(drawCompass()) ;
		}
		});
		valSET.setBackground(SystemColor.window);
		panelEast.add(valSET);
		valSET.setColumns(3);

		JLabel lblDFT = new JLabel("DFT");
		lblDFT.setHorizontalTextPosition(SwingConstants.RIGHT);
		lblDFT.setHorizontalAlignment(SwingConstants.RIGHT);
		lblDFT.setFont(new Font("Lucida Grande", Font.BOLD, 13));
		panelEast.add(lblDFT);

		valDFT = new JFormattedTextField(decimalFormatter);
		valDFT.setText(String.format("%.1f", sim.getCurrentSpeed()));
		valDFT.setName("valDFT");
		valDFT.setSelectionColor(SystemColor.textHighlight);
		valDFT.addFocusListener(new FocusAdapter() {
			@Override
			public void focusGained(FocusEvent e) {
				valDFT.setBackground(SystemColor.text);
				valDFT.selectAll();
			}

			@Override
			public void focusLost(FocusEvent e) {
				logger.info("Focus lost on valDFT " + e.getComponent());
				valDFT.setBackground(SystemColor.window);
				sim.setCurrentSpeed(Double.parseDouble(valDFT.getText()));
				compassRose.setIcon(drawCompass()) ;
			}
		});
		valDFT.setBackground(SystemColor.window);
		panelEast.add(valDFT);
		valDFT.setColumns(3);
	}
}
