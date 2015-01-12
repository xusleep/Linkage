package service.middleware.linkage.center.route.filters;

import java.util.List;

import service.middleware.linkage.framework.serviceaccess.entity.ServiceInformationEntity;

public class ClientRouteFilter implements RouteFilter{

	@Override
	public List<ServiceInformationEntity> filter(List<ServiceInformationEntity> serviceList) {
		// TODO Auto-generated method stub
		System.out.println("pass the client route filter ..");
		return serviceList;
	}

}
