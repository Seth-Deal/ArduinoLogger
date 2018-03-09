package seth.home;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

public class ArduinoLoggerController {
	static Logger logger = Logger.getLogger(ArduinoLoggerController.class.getName());

	public static void main(String[] args) {
		String log4jConfPath = "src/main/resources/log4j.properties";
		SpeedTestService service = SpeedTestService.getInstance();
		logger.debug(service.getValues());
		PropertyConfigurator.configure(log4jConfPath);
		//System.out.println("HE22RE");
		SpeedTest tester = new SpeedTest();
		tester.start();
		ArduinoLogger main = new ArduinoLogger();
		main.initialize();
		Thread t = new Thread() {
			public void run() {
				// the following line will keep this app alive for 1000 seconds,
				// waiting for events to occur and responding to them (printing incoming
				// messages to console).
				try {
					Thread.sleep(1000000);
				} catch (InterruptedException ie) {
					logger.error("InterruptedException: ",ie);
				}
			}
		};
		t.start();
		logger.debug("Started");
	}
}