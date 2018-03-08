package seth.home;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.Writer;

import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;

import java.util.Date;
import java.util.Enumeration;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

public class ArduinoLogger implements SerialPortEventListener {
	String internet = "unchanged";
	SerialPort serialPort;
	Date lastChecked;
	// the logger
	static Logger logger = Logger.getLogger(ArduinoLogger.class.getName());
	/** The port we're normally going to use. */
	private static final String PORT_NAMES[] = { "/dev/tty.usbserial-A9007UX1", // Mac OS X
			"/dev/ttyACM0", // Raspberry Pi
			"/dev/ttyUSB0", // Linux
			"COM3", // Windows
	};
	/**
	 * A BufferedReader which will be fed by a InputStreamReader converting the
	 * bytes into characters making the displayed results codepage independent
	 */
	private BufferedReader input;
	/** The output stream to the port */
	private OutputStream output;
	/** Milliseconds to block while waiting for port open */
	private static final int TIME_OUT = 2000;
	/** Default bits per second for COM port. */
	private static final int DATA_RATE = 9600;
	private String upload;
	private String download;
	private String ping;
	public static boolean isDouble(String str){
	    try{
	        Double.parseDouble(str);
	        return true;
	    }
	    catch( Exception e ){
	        return false;
	    }
	}
	public void initialize() {
		// the next line is for Raspberry Pi and
		// gets us into the while loop and was suggested here was suggested
		// http://www.raspberrypi.org/phpBB3/viewtopic.php?f=81&t=32186
		System.setProperty("gnu.io.rxtx.SerialPorts", "/dev/ttyACM0");

		CommPortIdentifier portId = null;
		Enumeration portEnum = CommPortIdentifier.getPortIdentifiers();

		// First, Find an instance of serial port as set in PORT_NAMES.
		while (portEnum.hasMoreElements()) {
			CommPortIdentifier currPortId = (CommPortIdentifier) portEnum.nextElement();
			for (String portName : PORT_NAMES) {
				if (currPortId.getName().equals(portName)) {
					portId = currPortId;
					break;
				}
			}
		}
		if (portId == null) {
			logger.info("Could not find COM port.");
			return;
		}

		try {
			// open serial port, and use class name for the appName.
			serialPort = (SerialPort) portId.open(this.getClass().getName(), TIME_OUT);

			// set port parameters
			serialPort.setSerialPortParams(DATA_RATE, SerialPort.DATABITS_8, SerialPort.STOPBITS_1,
					SerialPort.PARITY_NONE);

			// open the streams
			input = new BufferedReader(new InputStreamReader(serialPort.getInputStream()));
			output = serialPort.getOutputStream();

			// add event listeners
			serialPort.addEventListener(this);
			serialPort.notifyOnDataAvailable(true);
		} catch (Exception e) {
			logger.error("Error opening the event listener: ", e);
		}
	}

	/**
	 * This should be called when you stop using the port. This will prevent port
	 * locking on platforms like Linux.
	 */
	public synchronized void close() {
		if (serialPort != null) {
			serialPort.removeEventListener();
			serialPort.close();
		}
	}

	/**
	 * Handle an event on the serial port. Read the data and print it.
	 */
	public synchronized void serialEvent(SerialPortEvent oEvent) {
		if (oEvent.getEventType() == SerialPortEvent.DATA_AVAILABLE) {
			try {
				String inputLine = input.readLine();
				getSpeed();
				while(!isDouble(download)) {
					getSpeed();
				}
				logger.info(inputLine.replaceAll("}",internet)
						.replaceAll("temp", "\"temp\"")
						.replaceAll("audio", "\"audio\""));
			} catch (Exception e) {
				logger.error("Error reading the line", e);
			}
		}
		// Ignore all the other eventTypes, but you should consider the other ones.
	}

	public void getSpeed() {
		Date now = new Date();
		if (now.getTime() - lastChecked.getTime() > 300000) {
			String result = null;
			String pg = "";
			String up = "";
			String down = "";
			try {
				Runtime r = Runtime.getRuntime();
				Process p = r.exec("speedtest-cli --simple");
				BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
				String inputLine;
				while ((inputLine = in.readLine()) != null) {
					switch (inputLine.split(":")[0]) {
					case "Ping":
						pg =  inputLine.split(" ")[1];
						break;
					case "Download":
						down =  inputLine.split(" ")[1];
						break;
					case "Upload":
						up =  inputLine.split(" ")[1];
						break;
					default:
					}
					//System.out.println(inputLine);
					result += inputLine;
				}
				in.close();

			} catch (IOException e) {
				System.out.println(e);
			}
			upload=up;
			download=down;
			ping=pg;
			lastChecked = now;
			internet = ",\"ping\":\""+ping+"\",\"upload\":\""+upload+"\",\"download\":\""+download+"\"}";
			return;
		}
		return;
	}

	public static void main(String[] args) throws Exception {
		ArduinoLogger main = new ArduinoLogger();
		main.lastChecked = new Date (0);
		main.initialize();
		Thread t = new Thread() {
			public void run() {
				// the following line will keep this app alive for 1000 seconds,
				// waiting for events to occur and responding to them (printing incoming
				// messages to console).
				try {
					Thread.sleep(1000000);
				} catch (InterruptedException ie) {
				}
			}
		};
		// t.start();
		logger.info("Started");
	}
}