package service.framework.provide;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import service.framework.common.entity.RequestEntity;
import service.framework.common.entity.ResponseEntity;
import service.properties.ServicePropertyEntity;
import service.properties.WorkingPropertyEntity;

public class ProviderBean {
	private final WorkingPropertyEntity servicePropertyEntity;
	
	public ProviderBean(WorkingPropertyEntity servicePropertyEntity){
		this.servicePropertyEntity = servicePropertyEntity;
	}
	
	private ServicePropertyEntity searchService(String serviceName){
		for(ServicePropertyEntity entity : servicePropertyEntity.getServiceList()){
			if(entity.getServiceName().equals(serviceName)){
				return entity;
			}
		}
		return null;
	}
	
	public ResponseEntity prcessRequest(RequestEntity request){
		ServicePropertyEntity entity = searchService(request.getServiceName());
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
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return new ResponseEntity();
	}
}
