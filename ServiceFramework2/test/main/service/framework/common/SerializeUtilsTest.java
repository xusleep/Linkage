package service.framework.common;

import org.junit.Test;

import service.framework.common.entity.RequestEntity;

public class SerializeUtilsTest {
	/**
	 * ��requestʵ�����л�Ϊ�ַ���
	 * @param request
	 * @return
	 */
	@Test 
	public void testSerializeRequest() {
		RequestEntity request = new RequestEntity();
		System.out.println(SerializeUtils.serializeRequest(request));
	}
	
}
