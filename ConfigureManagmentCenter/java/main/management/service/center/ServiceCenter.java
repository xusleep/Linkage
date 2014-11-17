package management.service.center;

import java.util.LinkedList;
import java.util.List;

import management.service.center.comsume.DefaultRouteConsume;
import service.framework.common.entity.ServiceInformationEntity;

public interface ServiceCenter {
	public static final List<ServiceInformationEntity> serviceInformationList = new LinkedList<ServiceInformationEntity>();
	public static final List<ServiceInformationEntity> serviceClientList = new LinkedList<ServiceInformationEntity>();
	public String register(String serviceInfor);
	public String registerClientList(String serviceInfor);
	public String getServiceList(String servicename);
	public String removeServiceList(String serviceName);
}
