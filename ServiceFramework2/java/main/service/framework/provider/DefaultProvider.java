package service.framework.provider;

import java.lang.reflect.Method;

import org.apache.log4j.Logger;

import service.framework.common.StringUtils;
import service.framework.common.entity.RequestEntity;
import service.framework.common.entity.ResponseEntity;
import service.framework.setting.ServiceSettingEntity;
import service.framework.setting.reader.ServiceSettingReader;

public class DefaultProvider implements Provider{
	private final ServiceSettingReader servicePropertyEntity;
	private static Logger  logger = Logger.getLogger(Provider.class); 
	
	public DefaultProvider(ServiceSettingReader servicePropertyEntity){
		this.servicePropertyEntity = servicePropertyEntity;
	}
	
	/**
	 * search the service from the service list
	 * @param serviceName
	 * @return
	 */
	private ServiceSettingEntity searchService(String serviceName){
		for(ServiceSettingEntity entity : servicePropertyEntity.getServiceList()){
			if(entity.getServiceName().equals(serviceName)){
				return entity;
			}
		}
		return null;
	}
	
	/**
	 * process the request from the client
	 * @param request
	 * @return
	 */
	public ResponseEntity acceptServiceRequest(RequestEntity request){
		ServiceSettingEntity entity = searchService(request.getServiceName());
		if(entity == null){
			ResponseEntity response = new ResponseEntity();
			response.setResult("Can not find the service name of " + request.getServiceName());
			response.setRequestID(request.getRequestID());
			return response;
		}
		try {
			Class clazz = Class.forName(entity.getServiceInterface());
			Method[] methods = clazz.getMethods();
			Method findMethod = null;
			for(Method method : methods){
				if(method.getName().equals(request.getMethodName())){
					findMethod = method;
					break;
				}
			}
			if(findMethod != null)
			{
				Object result = findMethod.invoke(entity.getServiceTargetObject(), request.getArgs().toArray());
				ResponseEntity response = new ResponseEntity();
				response.setResult(result.toString());
				response.setRequestID(request.getRequestID());
				return response;
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			logger.error("unexpected exception happened. exception detail:" + StringUtils.ExceptionStackTraceToString(e));
			ResponseEntity response = new ResponseEntity();
			response.setResult("no service found in the server.");
			response.setRequestID(request.getRequestID());
			return response;
		} 
		return new ResponseEntity();
	}
}
