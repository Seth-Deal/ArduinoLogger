package seth.home;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Enumeration;
import java.util.TooManyListenersException;

import org.apache.log4j.Logger;

import gnu.io.CommPortIdentifier;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;
import gnu.io.UnsupportedCommOperationException;

public class ArduinoLogger implements SerialPortEventListener {
	SerialPort serialPort;
	final static int buffSize = 16;
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
	private PrintStream printer;
	/** Milliseconds to block while waiting for port open */
	private static final int TIME_OUT = 2000;
	/** Default bits per second for COM port. */
	private static final int DATA_RATE = 9600;

	public static boolean isDouble(String str) {
		try {
			Double.parseDouble(str);
			return true;
		} catch (Exception e) {
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

		// open serial port, and use class name for the appName.
		try {
			serialPort = (SerialPort) portId.open(this.getClass().getName(), TIME_OUT);
		} catch (PortInUseException e2) {
			logger.fatal("Port already in use: ", e2);
			System.exit(1);
		}

		// set port parameters
		try {
			serialPort.setSerialPortParams(DATA_RATE, SerialPort.DATABITS_8, SerialPort.STOPBITS_1,
					SerialPort.PARITY_NONE);
		} catch (UnsupportedCommOperationException e1) {
			logger.fatal("Error setting data rate: ", e1);
			System.exit(1);
		}

		// open the streams
		try {
			input = new BufferedReader(new InputStreamReader(serialPort.getInputStream()));
		} catch (IOException e) {
			logger.fatal("Can't open input stream: write-only", e);
			input = null;
		}

		try {
			output = serialPort.getOutputStream();
			printer = new PrintStream(output, true);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			logger.fatal("Can't open output stream: read-only", e);
			output = null;
		}

		// add event listeners
		try {
			serialPort.addEventListener(this);
		} catch (TooManyListenersException e) {
			// TODO Auto-generated catch block
			logger.fatal("Too many listeners", e);
		}
		serialPort.notifyOnDataAvailable(true);

		/*
		 * serialPort.notifyOnOutputEmpty(true);
		 * serialPort.notifyOnBreakInterrupt(true);
		 * serialPort.notifyOnCarrierDetect(true); serialPort.notifyOnCTS(true);
		 * serialPort.notifyOnDSR(true); serialPort.notifyOnFramingError(true);
		 * serialPort.notifyOnOverrunError(true); serialPort.notifyOnParityError(true);
		 * serialPort.notifyOnRingIndicator(true);
		 */

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
	 * Read data printed to the serial by arduino
	 *
	 * @param event
	 *            The data available event
	 */
	public synchronized void dataAvailable(SerialPortEvent event) {
		SpeedTestService service = SpeedTestService.getInstance();
		while (service.getValues() == "WAIT_FOR_SPEED") {
			try {
				logger.debug("waiting for speed");
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				logger.fatal("Interupted: ", e);
			}
		}
		try {
			String print = String.format("%.1f", new Double(service.getDownload()).doubleValue()) + "/"
					+ String.format("%.1f", new Double(service.getUpload()).doubleValue()) + " P"
					+ String.format("%.2f", new Double(service.getPing()).doubleValue());
			// String print = "HelloArduino";
			// HelloArduino1234
			for (int i = buffSize - print.length(); i > 0; i--) {
				print += " ";
			}
			for (int i = print.length() - buffSize; i > 0; i--) {
				print = print.substring(0, print.length() - 1);
			}

			printer.print(print);
			String inputLine = input.readLine();
			logger.info(inputLine.replaceAll("}", "," + service.getValues() + "}").replaceAll("temp", "\"temp\"")
					.replaceAll("audio", "\"audio\""));
		} catch (Exception e) {
			logger.error("Error reading the line, exiting", e);
			close();
			initialize();
			return;
		}
	}

	/**
	 * Handle output buffer empty events.
	 * 
	 * @param event
	 *            The output buffer empty event
	 */
	protected void outputBufferEmpty(SerialPortEvent event) {
		// Implement writing more data here
	}

	public synchronized void serialEvent(SerialPortEvent event) {
		//
		// Dispatch event to individual methods. This keeps this ugly
		// switch/case statement as short as possible.
		//
		switch (event.getEventType()) {
		case SerialPortEvent.OUTPUT_BUFFER_EMPTY:
			outputBufferEmpty(event);
			break;

		case SerialPortEvent.DATA_AVAILABLE:
			dataAvailable(event);
			break;

		/*
		 * Other events, not implemented here -> case SerialPortEvent.BI:
		 * breakInterrupt(event); break;
		 * 
		 * case SerialPortEvent.CD: carrierDetect(event); break;
		 * 
		 * case SerialPortEvent.CTS: clearToSend(event); break;
		 * 
		 * case SerialPortEvent.DSR: dataSetReady(event); break;
		 * 
		 * case SerialPortEvent.FE: framingError(event); break;
		 * 
		 * case SerialPortEvent.OE: overrunError(event); break;
		 * 
		 * case SerialPortEvent.PE: parityError(event); break;
		 * 
		 * case SerialPortEvent.RI: ringIndicator(event); break; <- other events, not
		 * implemented here
		 */

		}
	}

}
