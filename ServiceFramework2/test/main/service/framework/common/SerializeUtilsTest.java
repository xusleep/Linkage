package service.framework.common;

import static org.junit.Assert.assertTrue;

import java.util.LinkedList;
import java.util.List;

import org.junit.Test;

import service.framework.common.entity.RequestEntity;
import service.framework.common.entity.ResponseEntity;

public class SerializeUtilsTest {
	
	/**
	 * 将request实体序列化为字符串
	 * @param request
	 * @return
	 */
	@Test 
	public void testSerializeRequest() {
		RequestEntity request = new RequestEntity();
		request.setMethodName("test23%^Method&");
		request.setServiceName("<testServiceNmae>");
		request.setGroup("testGroup");
		request.setRequestID("10000");
		request.setVersion("@@!#$test.1.0");
		List<String> args = new LinkedList<String>();
		args.add("arg1");
		args.add("arg2&*^%");
		args.add("arg3");
		request.setArgs(args);
		String serializeStr = SerializeUtils.serializeRequest(request);
		System.out.println("serializeStr : " + serializeStr);
		RequestEntity result = SerializeUtils.deserializeRequest(serializeStr);
		assertTrue("result.getServiceName() not equals to <testServiceNmae> realvalue is " + result.getServiceName(),  result.getServiceName().equals("<testServiceNmae>"));
		assertTrue("result.getMethodName()  not equals to test23%^Method& realvalue is " + result.getMethodName(),  result.getMethodName().equals("test23%^Method&"));
		assertTrue("result.getGroup()       not equals to testGroup realvalue is " + result.getGroup(),  result.getGroup().equals("testGroup"));
		assertTrue("result.getVersion()     not equals to @@!#$test.1.0 realvalue is " + result.getVersion(),  result.getVersion().equals("@@!#$test.1.0"));
		assertTrue("result.setRequestID()   not equals to 10000 realvalue is " + result.getRequestID(),  result.getRequestID().equals("10000"));
		assertTrue("result.getArgs().get(1) not equals to arg2&*^% realvalue is " + result.getArgs().get(1),  result.getArgs().get(1).equals("arg2&*^%"));
	}
	
	/**
	 * 将ResponseEntity实体序列化为字符串
	 * @param request
	 * @return
	 */
	@Test 
	public void testSerializeResponse() {
		ResponseEntity response = new ResponseEntity();
		response.setRequestID("100001212");
		response.setResult("sdsjdlfkj$@^!*#!4457@$$");
		String serializeStr = SerializeUtils.serializeResponse(response);
		System.out.println("serializeStr : " + serializeStr);
		ResponseEntity result = SerializeUtils.deserializeResponse(serializeStr);
		assertTrue("result.getResult() not equals to sdsjdlfkj$@^!*#!4457@$$ realvalue is " + result.getResult(),  result.getResult().equals("sdsjdlfkj$@^!*#!4457@$$"));
		assertTrue("result.setRequestID()   not equals to 100001212 realvalue is " + result.getRequestID(),  result.getRequestID().equals("100001212"));
	}
}
