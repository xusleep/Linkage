package service.framework.route.filters;

import java.util.List;

import servicecenter.service.ServiceInformation;

public class ClientRouteFilter implements RouteFilter{

	@Override
	public List<ServiceInformation> filter(List<ServiceInformation> serviceList) {
		// TODO Auto-generated method stub
		System.out.println("pass the client route filter ..");
		return serviceList;
	}

}
