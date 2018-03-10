package seth.amazonaws.lambda.home;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.SdkClientException;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.S3Event;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.S3Object;

public class LambdaFunctionHandler implements RequestHandler<S3Event, String> {

	private AmazonS3 s3 = AmazonS3ClientBuilder.standard().build();
	static final Logger logger = LogManager.getLogger(LambdaFunctionHandler.class);

	public LambdaFunctionHandler() {
	}

	// Test purpose only.
	LambdaFunctionHandler(AmazonS3 s3) {
		this.s3 = s3;
	}

	@Override
	public String handleRequest(S3Event event, Context context) {
		logger.info("Received event: " + event);
		// Get the object from the event and show its conten

		String bucket = event.getRecords().get(0).getS3().getBucket().getName();
		String key = event.getRecords().get(0).getS3().getObject().getKey();
		String contentType = null;
		try {
			S3Object response = s3.getObject(new GetObjectRequest(bucket, key));
			contentType = response.getObjectMetadata().getContentType();
		} catch (AmazonServiceException ase) {
			logger.error(String
					.format("AmazonServiceException: Error getting object %s from bucket %s. Make sure they exist and"
							+ " your bucket is in the same region as this function.", key, bucket),
					ase);
		} catch (SdkClientException sdk) {
			logger.error(
					String.format("SdkClientException: Error getting object %s from bucket %s. Make sure they exist and"
							+ " your bucket is in the same region as this function.", key, bucket),
					sdk);
		}
		if (contentType != null) {
			logger.info("CONTENT TYPE: " + contentType);
		} else {
			logger.fatal(String.format(
					"ERROR: THEORETICALLY UNREACHABLE getting object %s from bucket %s. Make sure they exist and"
							+ " your bucket is in the same region as this function.",
					key, bucket));
		}
		return contentType;
	}
}