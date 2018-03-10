package seth.amazonaws.lambda.home;

import java.util.zip.ZipEntry;

public class ExtractZipEntry extends Thread{
	private ZipEntry entry;
	String s3Prefix;
	public ExtractZipEntry(ZipEntry entry) {
		super();
		this.entry = entry;
	}
	public void withS3Target(String s3Prefix) {
		this.s3Prefix = s3Prefix;
	}
	public void run(){
		
	}
}
