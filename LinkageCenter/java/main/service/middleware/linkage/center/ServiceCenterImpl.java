package service.middleware.linkage.center;

import java.util.LinkedList;
import java.util.List;

import service.middleware.linkage.center.client.ServiceCenterClientUtils;
import service.middleware.linkage.center.common.ServiceCenterUtils;
import service.middleware.linkage.framework.exception.ServiceException;
import service.middleware.linkage.framework.serviceaccess.entity.ServiceInformationEntity;

/**
 * 提供给其他Service服务器，注册使用
 * @author Smile
 *
 */
public class ServiceCenterImpl implements ServiceCenter {
	@Override
	public String register(String serviceInfor) {
		// TODO Auto-generated method stub
		List<ServiceInformationEntity> objServiceInformation = ServiceCenterUtils.deserializeServiceInformationList(serviceInfor);
		synchronized(ServiceCenter.serviceInformationList)
		{
			ServiceCenter.serviceInformationList.addAll(objServiceInformation);
		}
		for(ServiceInformationEntity clientServiceInformationEntity : ServiceCenter.serviceClientList)
		{
			try {
				ServiceCenterClientUtils.notifyClientServiceAdd(ServiceCenterClientUtils.defaultRouteConsume, clientServiceInformationEntity, objServiceInformation);
			} catch (ServiceException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		System.out.println("register service count = " + ServiceCenter.serviceInformationList.size());
		return "true";
	}
	
	@Override
	public String registerClientList(String serviceInfor) { 
		ServiceInformationEntity objServiceInformation = ServiceCenterUtils.deserializeServiceInformation(serviceInfor);
		ServiceCenter.serviceClientList.add(objServiceInformation);
		System.out.println("serviceInfor = " + serviceInfor);
		System.out.println("service client count = " + ServiceCenter.serviceInformationList.size());
		return "true";
	}

	@Override
	public String getServiceList(String serviceName) {
		List<ServiceInformationEntity> resultList = new LinkedList<ServiceInformationEntity>();	
		for(ServiceInformationEntity objServiceInformation : ServiceCenter.serviceInformationList){
			if(objServiceInformation.getServiceName().equals(serviceName))
			{
				resultList.add(objServiceInformation);
			}
		}
		return ServiceCenterUtils.serializeServiceInformationList(resultList);
	}
	/**
	 * 删除服务列表
	 */
	public String removeServiceList(String serviceName){
		List<ServiceInformationEntity> resultList = new LinkedList<ServiceInformationEntity>();	
		synchronized(ServiceCenter.serviceInformationList)
		{
			for(ServiceInformationEntity objServiceInformation : ServiceCenter.serviceInformationList){
				if(objServiceInformation.getServiceName().equals(serviceName))
				{
					resultList.add(objServiceInformation);
				}
			}
			ServiceCenter.serviceInformationList.removeAll(resultList);
		}
		return "true";
	}

}
