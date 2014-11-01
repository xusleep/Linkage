package service.center;

import java.util.LinkedList;
import java.util.List;

import service.framework.common.SerializeUtils;
import service.framework.common.entity.ServiceInformationEntity;

/**
 * 提供给其他Service服务器，注册使用
 * @author Smile
 *
 */
public class ServiceCenterImpl implements ServiceCenter {

	@Override
	public String register(String serviceInfor) {
		System.out.println("服务中心注册服务被启动:" + serviceInfor);
		// TODO Auto-generated method stub
		List<ServiceInformationEntity> objServiceInformation = SerializeUtils.deserializeServiceInformationList(serviceInfor);
		ServiceCenter.ServiceInformationList.addAll(objServiceInformation);
		System.out.println("service count = " + ServiceCenter.ServiceInformationList.size());
		return "true";
	}

	@Override
	public String getServiceList(String serviceName) {
		System.out.println("服务中心注册服务被启动: 获取服务列表 " + serviceName + 
				"ServiceInformationList .." + SerializeUtils.serializeServiceInformationList(ServiceCenter.ServiceInformationList));
		List<ServiceInformationEntity> resultList = new LinkedList<ServiceInformationEntity>();	
		for(ServiceInformationEntity objServiceInformation : ServiceCenter.ServiceInformationList){
			if(objServiceInformation.getServiceName().equals(serviceName))
			{
				resultList.add(objServiceInformation);
			}
		}
		System.out.println("service count = " + ServiceCenter.ServiceInformationList.size());
		return SerializeUtils.serializeServiceInformationList(resultList);
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
