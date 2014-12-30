package service.middleware.management.center;

import java.util.LinkedList;
import java.util.List;

import service.middleware.framework.common.entity.ServiceInformationEntity;
import service.middleware.management.center.comsume.DefaultRouteConsume;

public interface ServiceCenter {
	public static final List<ServiceInformationEntity> serviceInformationList = new LinkedList<ServiceInformationEntity>();
	public static final List<ServiceInformationEntity> serviceClientList = new LinkedList<ServiceInformationEntity>();
	public String register(String serviceInfor);
	public String registerClientList(String serviceInfor);
	public String getServiceList(String servicename);
	public String removeServiceList(String serviceName);
}
