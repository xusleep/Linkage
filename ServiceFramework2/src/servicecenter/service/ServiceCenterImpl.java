package servicecenter.service;

import java.util.LinkedList;
import java.util.List;

import service.framework.serialization.SerializeUtils;

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
		ServiceInformation objServiceInformation = SerializeUtils.deserializeServiceInformation(serviceInfor);
		ServiceCenter.ServiceInformationList.add(objServiceInformation);
		return "true";
	}

	@Override
	public String getServiceList(String serviceName) {
		System.out.println("��������ע���������: ��ȡ�����б� " + serviceName + 
				"ServiceInformationList .." + SerializeUtils.serializeServiceInformationList(ServiceCenter.ServiceInformationList));
		List<ServiceInformation> resultList = new LinkedList<ServiceInformation>();	
		for(ServiceInformation objServiceInformation : ServiceCenter.ServiceInformationList){
			if(objServiceInformation.getServiceName().equals(serviceName))
			{
				resultList.add(objServiceInformation);
			}
		}
		return SerializeUtils.serializeServiceInformationList(resultList);
	}

}
