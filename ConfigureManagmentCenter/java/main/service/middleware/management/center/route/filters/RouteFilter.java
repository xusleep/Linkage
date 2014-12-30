package service.middleware.management.center.route.filters;

import java.util.List;

import service.middleware.framework.common.entity.ServiceInformationEntity;

public interface RouteFilter {
	public List<ServiceInformationEntity> filter(List<ServiceInformationEntity> serviceList);
}
