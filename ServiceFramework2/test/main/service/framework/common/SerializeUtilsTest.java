package service.framework.common;

import org.junit.Test;

import service.framework.common.entity.RequestEntity;

public class SerializeUtilsTest {
	/**
	 * 将request实体序列化为字符串
	 * @param request
	 * @return
	 */
	@Test 
	public void testSerializeRequest() {
		RequestEntity request = new RequestEntity();
		System.out.println(SerializeUtils.serializeRequest(request));
	}
	
}
