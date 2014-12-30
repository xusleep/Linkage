package service.framework.io.protocol;


public class CommunicationProtocol {
	/**
	 * data transform buffer Setting, in the data level
	 */
	public static final int BUFFER_SIZE = 1024;
	/**
	 * data transform coding Setting
	 */
	public static final String FRAMEWORK_IO_ENCODING = "UTF-8";
	/**
	 * data transform buffer seting, in the message level
	 */
	public static final int RECEIVE_BUFFER_MESSAGE_SIZE = 2048;
	/**
	 * message package header start
	 */
	public static final String MESSAGE_HEADER_START = "$#####$";
	/**
	 * message package header end
	 */
	public static final String MESSAGE_HEADER_END = "*#####*";
	/**
	 * message package header length
	 */
	public static final int MESSAGE_HEADER_LENGTH_PART = 4;
}
