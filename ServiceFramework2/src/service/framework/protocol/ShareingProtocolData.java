package service.framework.protocol;

import java.util.concurrent.atomic.AtomicInteger;

public class ShareingProtocolData {
	/**
	 * ���ݴ��仺�棬����
	 */
	public static final int BUFFER_SIZE = 1024;
	public static final String FRAMEWORK_IO_ENCODING = "UTF-8";
	public static final int BUFFER_MESSAGE_SIZE = 2048;
	/**
	 * ��ͷ��ʼ����
	 */
	public static final String MESSAGE_HEADER_START = "$#####$";
	/**
	 * ��ͷ��������
	 */
	public static final String MESSAGE_HEADER_END = "*#####*";
	/**
	 * ��ʾ��ͷ�У����ڱ�����峤�ȵ��ַ�����
	 */
	public static final int MESSAGE_HEADER_LENGTH_PART = 4;
	public static AtomicInteger aint = new AtomicInteger(0);
	
	
	
	public static void main(String[] args) throws Exception{
	}
}
