package service.framework.protocol;

import java.util.concurrent.atomic.AtomicInteger;

public class ShareingProtocolData {
	/**
	 * 数据传输缓存，设置
	 */
	public static final int BUFFER_SIZE = 1024;
	public static final String FRAMEWORK_IO_ENCODING = "UTF-8";
	public static final int BUFFER_MESSAGE_SIZE = 2048;
	/**
	 * 包头开始部分
	 */
	public static final String MESSAGE_HEADER_START = "$#####$";
	/**
	 * 包头结束部分
	 */
	public static final String MESSAGE_HEADER_END = "*#####*";
	/**
	 * 表示包头中，用于标书包体长度的字符长度
	 */
	public static final int MESSAGE_HEADER_LENGTH_PART = 4;
	public static AtomicInteger aint = new AtomicInteger(0);
	
	
	
	public static void main(String[] args) throws Exception{
	}
}
