package management.service.center.route.filters;

import java.util.List;

import service.framework.common.entity.ServiceInformationEntity;

public interface RouteFilter {
	public List<ServiceInformationEntity> filter(List<ServiceInformationEntity> serviceList);
}
