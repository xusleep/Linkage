package management.service.client;

import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;

import management.service.center.common.ServiceCenterUtils;
import management.service.center.comsume.DefaultRouteConsume;
import service.framework.common.entity.RequestResultEntity;
import service.framework.common.entity.ServiceInformationEntity;
import service.framework.exception.ServiceException;
import service.framework.io.common.NIOWorker;
import service.framework.properties.ServicePropertyEntity;
import service.framework.properties.WorkingServicePropertyEntity;
import service.framework.serviceaccess.ServiceAccess;

/**
 * this class is used for call from the client side
 * from the service center side, it will call method to request the service of client
 * from the client side, it will call method to request the service of service center
 * @author Smile
 *
 */
public final class ServiceCenterClientUtils {
	private static Logger  logger = Logger.getLogger(ServiceCenterClientUtils.class);  

	public static DefaultRouteConsume defaultRouteConsume = null;
	
	// the service center side
	public static final String SERVICE_CENTER_SERVICE_NAME   				= "serviceCenter";
	public static final String SERVICE_CENTER_REGISTER_ID    				= "serviceCenterRegister";
	public static final String SERVICE_CENTER_UNREGISTER_ID 				= "serviceCenterUnregister";
	public static final String SERVICE_CENTER_GET_SERVICE_ID 				= "serviceCenterGetServiceList";
	public static final String SERVICE_CENTER_REGISTER_CLIENT_ID    		= "serviceCenterRegisterClient";
	
	// the service center client side
	public static final String SERVICE_CENTER_CLIENT_SERVICE_NAME			= "serviceCenterClientService";
	public static final String SERVICE_CENTER_CLIENT_SERVICE_REMOVE 		= "serviceCenterClientServiceRemove";
	public static final String SERVICE_CENTER_CLIENT_SERVICE_ADD 			= "serviceCenterClientServiceAdd";
	
	/**
	 * invoke the client service method
	 * @param clientID
	 * @param serviceInformationList
	 * @throws ServiceException 
	 */
	public static boolean notifyClientServiceAdd(ServiceAccess consume, ServiceInformationEntity clientServiceInformationEntity, List<ServiceInformationEntity> serviceInformationList ) throws ServiceException{
		String strServiceInformation = ServiceCenterUtils.serializeServiceInformationList(serviceInformationList);
		List<String> args = new LinkedList<String>();
		args.add(strServiceInformation);
		RequestResultEntity result = consume.requestServicePerConnectSync(SERVICE_CENTER_CLIENT_SERVICE_ADD, args, clientServiceInformationEntity);
		if(result.isException()){
			result.getException().printStackTrace();
			throw result.getException();
		}
		return true;
	}
	
	/**
	 * call the service of the service center, 
	 * register the client information to service center,
	 * when the center need to notify the client about the service change,
	 * the center could use the client list which is register from here
	 * @param clientServiceInformationEntity
	 * @return
	 * @throws ServiceException
	 */
	public static boolean registerClientInformation(ServiceAccess consume, ServiceInformationEntity centerServiceInformationEntity, 
			ServiceInformationEntity clientServiceInformationEntity) throws ServiceException{
		String strServiceInformation = ServiceCenterUtils.serializeServiceInformation(clientServiceInformationEntity);
		List<String> args = new LinkedList<String>();
		args.add(strServiceInformation);
		RequestResultEntity result = consume.requestServicePerConnectSync(SERVICE_CENTER_REGISTER_CLIENT_ID, args, centerServiceInformationEntity);
		if(result.isException()){
			result.getException().printStackTrace();
			throw result.getException();
		}
		return true;
	}
	
	/**
	 * this method is used to register the service to service center
	 * @param consumerBean
	 * @param workingServicePropertyEntity
	 * @return
	 * @throws ServiceException
	 */
	public static boolean registerServiceList(ServiceAccess consume, ServiceInformationEntity centerServiceInformationEntity, WorkingServicePropertyEntity workingServicePropertyEntity) throws ServiceException{
		List<ServiceInformationEntity> serviceInformationList = new LinkedList<ServiceInformationEntity>();
		
		//获取所有服务，将服务注册到注册中心
		List<ServicePropertyEntity> serviceList = workingServicePropertyEntity.getServiceList();
		for(ServicePropertyEntity serviceEntity: serviceList)
		{
			String interfaceName = serviceEntity.getServiceInterface();
			try {
				Class interfaceclass = Class.forName(interfaceName);
				Method[] methods = interfaceclass.getMethods();
				for(int i = 0; i < methods.length; i++){
					ServiceInformationEntity subServiceInformation = new ServiceInformationEntity();
					subServiceInformation.setAddress(workingServicePropertyEntity.getServiceAddress());
					subServiceInformation.setPort(workingServicePropertyEntity.getServicePort());
					subServiceInformation.setServiceMethod(methods[i].getName());
					subServiceInformation.setServiceName(serviceEntity.getServiceName());
					subServiceInformation.setServiceVersion(serviceEntity.getServiceVersion());
					serviceInformationList.add(subServiceInformation);
					logger.debug("service name : " + serviceEntity.getServiceName() + " method name : " + methods[i].getName());
				}
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
			
		}
		String strServiceInformation = ServiceCenterUtils.serializeServiceInformationList(serviceInformationList);
		List<String> args = new LinkedList<String>();
		args.add(strServiceInformation);
		RequestResultEntity result = consume.requestServicePerConnectSync(ServiceCenterClientUtils.SERVICE_CENTER_REGISTER_ID, args, centerServiceInformationEntity);
		if(result.isException()){
			result.getException().printStackTrace();
			throw result.getException();
		}
		return true;
	}
	
	
}
