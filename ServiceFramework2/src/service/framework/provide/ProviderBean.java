package service.framework.provide;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.TypeVariable;

import service.framework.provide.entity.RequestEntity;
import service.framework.provide.entity.ResponseEntity;

public class ProviderBean {
	private String interfaceName;
	private Object target;
	private String version;
	private String group;
	
	public ResponseEntity prcessRequest(RequestEntity request){
		try {
			Class clazz = Class.forName(interfaceName);
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
				System.out.println("Provider args : " + request.getArgs().get(0));
				Object result = findMethod.invoke(target, request.getArgs().toArray());
				ResponseEntity response = new ResponseEntity();
				response.setResult(result.toString());
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
		}
		return new ResponseEntity();
	}
	
	public String getInterfaceName() {
		return interfaceName;
	}

	public void setInterfaceName(String interfaceName) {
		this.interfaceName = interfaceName;
	}

	public Object getTarget() {
		return target;
	}

	public void setTarget(Object target) {
		this.target = target;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getGroup() {
		return group;
	}

	public void setGroup(String group) {
		this.group = group;
	}


}
