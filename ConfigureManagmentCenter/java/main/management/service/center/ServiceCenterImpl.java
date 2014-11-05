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
		System.out.println("��������ע���������:" + serviceInfor);
		// TODO Auto-generated method stub
		List<ServiceInformationEntity> objServiceInformation = ServiceCenterUtils.deserializeServiceInformationList(serviceInfor);
		ServiceCenter.ServiceInformationList.addAll(objServiceInformation);
		System.out.println("service count = " + ServiceCenter.ServiceInformationList.size());
		return "true";
	}

	@Override
	public String getServiceList(String serviceName) {
		System.out.println("��������ע���������: ��ȡ�����б� " + serviceName + 
				"ServiceInformationList .." + ServiceCenterUtils.serializeServiceInformationList(ServiceCenter.ServiceInformationList));
		List<ServiceInformationEntity> resultList = new LinkedList<ServiceInformationEntity>();	
		for(ServiceInformationEntity objServiceInformation : ServiceCenter.ServiceInformationList){
			if(objServiceInformation.getServiceName().equals(serviceName))
			{
				resultList.add(objServiceInformation);
			}
		}
		System.out.println("service count = " + ServiceCenter.ServiceInformationList.size());
		return ServiceCenterUtils.serializeServiceInformationList(resultList);
	}
	
	public String removeServiceList(String serviceName){
		System.out.println("remove serviceName " + serviceName);
		List<ServiceInformationEntity> resultList = new LinkedList<ServiceInformationEntity>();	
		for(ServiceInformationEntity objServiceInformation : ServiceCenter.ServiceInformationList){
			if(objServiceInformation.getServiceName().equals(serviceName))
			{
				resultList.add(objServiceInformation);
			}
		}
		ServiceCenter.ServiceInformationList.removeAll(resultList);
		System.out.println("service count = " + ServiceCenter.ServiceInformationList.size());
		return "true";
	}

}