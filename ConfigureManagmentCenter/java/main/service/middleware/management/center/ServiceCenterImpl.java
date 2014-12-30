package service.middleware.management.center;

import java.util.LinkedList;
import java.util.List;

import service.middleware.framework.common.entity.ServiceInformationEntity;
import service.middleware.framework.exception.ServiceException;
import service.middleware.management.center.common.ServiceCenterUtils;
import service.middleware.management.center.comsume.DefaultRouteConsume;
import service.middleware.management.client.ServiceCenterClientUtils;

/**
 * �ṩ������Service��������ע��ʹ��
 * @author Smile
 *
 */
public class ServiceCenterImpl implements ServiceCenter {
	@Override
	public String register(String serviceInfor) {
		// TODO Auto-generated method stub
		List<ServiceInformationEntity> objServiceInformation = ServiceCenterUtils.deserializeServiceInformationList(serviceInfor);
		ServiceCenter.serviceInformationList.addAll(objServiceInformation);
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
	 * ɾ�������б�
	 */
	public String removeServiceList(String serviceName){
		List<ServiceInformationEntity> resultList = new LinkedList<ServiceInformationEntity>();	
		for(ServiceInformationEntity objServiceInformation : ServiceCenter.serviceInformationList){
			if(objServiceInformation.getServiceName().equals(serviceName))
			{
				resultList.add(objServiceInformation);
			}
		}
		ServiceCenter.serviceInformationList.removeAll(resultList);
		return "true";
	}

}