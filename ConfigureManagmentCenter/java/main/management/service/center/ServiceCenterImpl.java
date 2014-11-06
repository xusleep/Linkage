package management.service.center;

import java.util.LinkedList;
import java.util.List;

import management.service.center.common.ServiceCenterUtils;
import service.framework.common.entity.ServiceInformationEntity;

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
		System.out.println("service count = " + ServiceCenter.serviceInformationList.size());
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
