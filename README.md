
# Updated Vessel Simulator Program

Java program that simulates a vessel moving on the face of the earth. Assuming you have all
the prerequisites installed you can run the program by just going :

- cd <wherever you downloaded the program>
- gradle run

The program will simulate a boat navigating around subject to current set and drift but
does not (currently) add leeway. The current and various starting parameters are
set in a configuration file, described below. The emulation is good enough that it can be
used to test various types of navigation software like Expedition, OpenCPN, iNavX. iRegatta
and the like.

NMEA0183 sentences are sent out across the network via UDP broadcast on a pre-configured port
number. By default the program will use port 10110 which will look like a Comar device to Navionics
running on a tablet connected to the same network.

Right now we send the following sentences:

- $GPGSV
- $GPGLL
- $GPGGA
- $GPVHW
- $GPVDR
- $HCHDT
- $HCHDM
- $IIMWV
- $IIVTG
- $GPRMC
- $SDDPT

Tell your navigation software/whatever to connect to UDP 10110. NMEA 0183 sentences are sent out from
the simulation on port 10110 by default unless a property file is used to override it.

See below for details about setting up a property file.

# Prerequisites

Java of a reasonably current vintage. I use a recent build of OpenJDK for development and testing.

# Customization

Simulation parameters can be read from an external file. The program will first look in the current directory or if
you set the system property "property.file" (java -Dproperty.file="wherever the property file is" -jar ... etc) it
will read all properties from there. An example is provided with the program. If you want to tweak things like the
UDP port it transmits on or the initial starting position, then this is where you can do that.

# Operation

When the program is started you will see a number of text boxes on the right hand side and a centred compass rose.

You can change the direction the boat is heading by clicking anywhere on the compass rose. You can also change the
wind and current parameters by entering desired values into the input text fields on the right hand side.

The simulation will automatically look up the polars for the boat which it expects to find in a file called
PolarDiagram.csv in the current directory. These polars will be used to calculate the boat speed based on the
TWS and TWA calculated from land wind and current vectors plus boat heading. The polars currently loaded by
default are those for a Santa Cruz 52 sailboat but you could easily substitute your own.
