package management.service.center.common;

import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;

import service.framework.common.entity.RequestResultEntity;
import service.framework.common.entity.ServiceInformationEntity;
import service.framework.comsume.ConsumerBean;
import service.framework.exception.ServiceException;
import service.framework.properties.ServicePropertyEntity;
import service.framework.properties.WorkingServicePropertyEntity;

public final class ServiceCenterClientUtils {
	public static final String SERVICE_CENTER_SERVICE_NAME = "serviceCenter";
	public static final String SERVICE_CENTER_REGISTER_ID = "serviceCenterRegister";
	public static final String SERVICE_CENTER_UNREGISTER_ID = "serviceCenterUnregister";
	public static final String SERVICE_CENTER_GET_SERVICE_ID = "serviceCenterGetServiceList";
	
	/**
	 * this method is used to register the service to service center
	 * @param consumerBean
	 * @param workingServicePropertyEntity
	 * @return
	 * @throws ServiceException
	 */
	public static boolean registerService(ConsumerBean consumerBean, WorkingServicePropertyEntity workingServicePropertyEntity) throws ServiceException{
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
					System.out.println("service name : " + serviceEntity.getServiceName() + " method name : " + methods[i].getName());
				}
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		String strServiceInformation = ServiceCenterUtils.serializeServiceInformationList(serviceInformationList);
		List<String> args = new LinkedList<String>();
		args.add(strServiceInformation);
		RequestResultEntity result = consumerBean.prcessRequestPerConnectSync(SERVICE_CENTER_REGISTER_ID, args);
		if(result.isException()){
			result.getException().printStackTrace();
			throw result.getException();
		}
		return true;
	}
	
	
}
