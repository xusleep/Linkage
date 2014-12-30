package service.middleware.linkage.framework.io.protocol;


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
	
	/**
	 * extract the message from the message buffer
	 * please note that this is not a thread safe method
	 * you need to use the synchronized means when call it if required
	 * @param sb
	 * @return
	 * @throws Exception 
	 */
	public static String extractMessage(StringBuffer bufferMessage) throws Exception{
		int headStartIndex = bufferMessage.indexOf(CommunicationProtocol.MESSAGE_HEADER_START);
		int headEndIndex = bufferMessage.indexOf(CommunicationProtocol.MESSAGE_HEADER_END);
		if(CommunicationProtocol.MESSAGE_HEADER_START.length() > bufferMessage.length())
			return "";
		if(headStartIndex != 0)
		{
			throw new Exception("the received message is not comleted, some message not receive correct");
		}
		// message header is not completed, means it does not receive a complete message package
		if(headEndIndex <= 0)
			return "";
		String head = bufferMessage.substring(headStartIndex, headEndIndex + CommunicationProtocol.MESSAGE_HEADER_END.length());
		String bodyLenthStr =  bufferMessage.substring(headStartIndex + CommunicationProtocol.MESSAGE_HEADER_START.length(), 
				headStartIndex + CommunicationProtocol.MESSAGE_HEADER_START.length() + CommunicationProtocol.MESSAGE_HEADER_LENGTH_PART);
		int bodyLenth = Integer.parseInt(bodyLenthStr);
		// message body length is not reached as the expected length
		if(bufferMessage.length() < CommunicationProtocol.MESSAGE_HEADER_START.length() + 
				CommunicationProtocol.MESSAGE_HEADER_LENGTH_PART + CommunicationProtocol.MESSAGE_HEADER_END.length() + bodyLenth)
		{
			return "";
		}
		String messageBody = bufferMessage.substring(headEndIndex + CommunicationProtocol.MESSAGE_HEADER_END.length(), 
				headEndIndex + CommunicationProtocol.MESSAGE_HEADER_END.length() + bodyLenth);
		bufferMessage.delete(headStartIndex, headEndIndex + CommunicationProtocol.MESSAGE_HEADER_END.length() + bodyLenth);
		return messageBody;
	}
	
	/**
	 * wrap the message as protocol
	 * @param message
	 * @return
	 */
	public static String wrapMessage(String message){
		return CommunicationProtocol.MESSAGE_HEADER_START + toLengthString(message.length()) + CommunicationProtocol.MESSAGE_HEADER_END + message; 
	}
	
	/**
	 * Convert the number to a string 
	 * 2    -- > 0002
	 * 21   -- > 0021
	 * 2345 -- > 2345
	 * @param length
	 * @return
	 */
	private static String toLengthString(int length){
		String tmp = "" + length;
		int tmpLength = tmp.length();
		for(int i = 0; i < CommunicationProtocol.MESSAGE_HEADER_LENGTH_PART - tmpLength; i++){
			tmp = "0" + tmp;
		}
		return tmp;
	}
}
