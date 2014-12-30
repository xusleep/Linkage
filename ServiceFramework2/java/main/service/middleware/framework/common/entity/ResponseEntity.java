package service.middleware.framework.common.entity;

/**
 * this class stores the information which response from the server
 * @author zhonxu
 *
 */
public class ResponseEntity {
	private String requestID;
	private String result;

	public String getResult() {
		return result;
	}

	public void setResult(String result) {
		this.result = result;
	}

	public String getRequestID() {
		return requestID;
	}

	public void setRequestID(String requestID) {
		this.requestID = requestID;
	}
	
}
