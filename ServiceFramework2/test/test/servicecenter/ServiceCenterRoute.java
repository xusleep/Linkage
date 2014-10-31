package test.servicecenter;

import java.util.List;

import service.framework.common.entity.ServiceInformationEntity;
import service.framework.route.Route;

public class ServiceCenterRoute implements Route {
	private List<ServiceInformationEntity> serviceList;
	
	

	public List<ServiceInformationEntity> getServiceList() {
		return serviceList;
	}



	public void setServiceList(List<ServiceInformationEntity> serviceList) {
		this.serviceList = serviceList;
	}



	@Override
	public ServiceInformationEntity chooseRoute(String serviceName) {
		// TODO Auto-generated method stub
		return this.serviceList.get(0);
	}

}
