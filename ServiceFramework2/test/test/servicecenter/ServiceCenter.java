package test.servicecenter;

import java.util.LinkedList;
import java.util.List;

import service.framework.common.entity.ServiceInformationEntity;

public interface ServiceCenter {
	public static List<ServiceInformationEntity> ServiceInformationList = new LinkedList<ServiceInformationEntity>();
	
	public String register(String serviceInfor);
	public String getServiceList(String servicename);
}
