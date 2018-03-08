package seth.home;

import java.util.Date;

import org.apache.log4j.Logger;

public class SpeedTestService {

	private static volatile SpeedTestService instance;
	private static Object mutex = new Object();

	private String upload;
	private String download;
	private String ping;
	private String values;
	private String OS;

	static Logger logger = Logger.getLogger(SpeedTestService.class.getName());
	private SpeedTestService() {
		upload = "0";
		download = "0";
		ping = "0";
		values = "WAIT_FOR_SPEED";
		OS = "";
	}

	public static SpeedTestService getInstance() {
		SpeedTestService result = instance;
		logger.debug("Get instance called");
		//System.out.println("HERE2");
		if (result == null) {
			synchronized (mutex) {
				result = instance;
				if (result == null)
					instance = result = new SpeedTestService();
			}
		}
		return result;
	}

	public String getUpload() {
		return upload;
	}

	public void setUpload(String upload) {
		this.upload = upload;
	}

	public String getDownload() {
		return download;
	}

	public void setDownload(String download) {
		this.download = download;
	}

	public String getPing() {
		return ping;
	}

	public void setPing(String ping) {
		this.ping = ping;
	}

	public String getValues() {
		return values;
	}

	public void setValues(String values) {
		this.values = values;
	}

	public String getOS() {
		return OS;
	}

	public void setOS(String oS) {
		OS = oS;
	}

}