package seth.home;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.log4j.Logger;

public class SpeedTest extends Thread {
	static Logger logger = Logger.getLogger(SpeedTest.class.getName());

	public void run() {
		try {
			while (true) {
				getSpeed();
				//System.out.println("HERE");
				Thread.sleep(300000);
			}

		} catch (InterruptedException e) {
			logger.fatal("Interupted: ", e);
		}

	}

	public void getSpeed() {
		OSCheck.OSType ostype = OSCheck.getOperatingSystemType();
		switch (ostype) {
		case Windows:
			getSpeed_Windows();
			break;
		case MacOS:
			break;
		case Linux:
			getSpeed_Linux();
			break;
		case Other:
			break;
		}
	}

	private void getSpeed_Windows() {

	}

	private void getSpeed_Linux() {
		logger.debug("Getting speed");
		
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
					pg = inputLine.split(" ")[1];
					break;
				case "Download":
					down = inputLine.split(" ")[1];
					break;
				case "Upload":
					up = inputLine.split(" ")[1];
					break;
				default:
				}
				// System.out.println(inputLine);
				// result += inputLine;
			}
			in.close();

		} catch (IOException e) {
			System.out.println(e);
		}
		SpeedTestService service = SpeedTestService.getInstance();
		service.setUpload(up);
		service.setDownload(down);
		service.setPing(pg);
		service.setValues("\"ping\":\"" + service.getPing() + "\",\"upload\":\"" + service.getUpload()
				+ "\",\"download\":\"" + service.getDownload() + "\"");
		logger.debug("Done Getting speed");
		return;
	}
}
