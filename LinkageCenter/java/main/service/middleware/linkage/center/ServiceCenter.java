package service.middleware.linkage.center;

import java.util.LinkedList;
import java.util.List;

import service.middleware.linkage.framework.serviceaccess.entity.ServiceInformationEntity;

public interface ServiceCenter {
	public static final List<ServiceInformationEntity> serviceInformationList = new LinkedList<ServiceInformationEntity>();
	public static final List<ServiceInformationEntity> serviceClientList = new LinkedList<ServiceInformationEntity>();
	public String register(String serviceInfor);
	public String registerClientList(String serviceInfor);
	public String getServiceList(String servicename);
	public String removeServiceList(String serviceName);
}
