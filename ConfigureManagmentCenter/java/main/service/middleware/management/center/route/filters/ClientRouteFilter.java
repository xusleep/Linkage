package service.middleware.management.center.route.filters;

import java.util.List;

import service.middleware.framework.common.entity.ServiceInformationEntity;

public class ClientRouteFilter implements RouteFilter{

	@Override
	public List<ServiceInformationEntity> filter(List<ServiceInformationEntity> serviceList) {
		// TODO Auto-generated method stub
		System.out.println("pass the client route filter ..");
		return serviceList;
	}

}
