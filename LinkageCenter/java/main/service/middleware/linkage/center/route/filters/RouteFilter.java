package service.middleware.linkage.center.route.filters;

import java.util.List;

import service.middleware.linkage.framework.serviceaccess.entity.ServiceInformationEntity;

public interface RouteFilter {
	public List<ServiceInformationEntity> filter(List<ServiceInformationEntity> serviceList);
}
