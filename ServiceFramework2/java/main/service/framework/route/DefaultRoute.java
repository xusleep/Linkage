package service.framework.route;

import java.util.List;

import service.framework.common.entity.ServiceInformationEntity;
import service.framework.comsume.ConsumerBean;
import service.framework.exception.ServiceException;
import service.framework.route.filters.RouteFilter;
/**
 * this route will access the service directly 
 * @author zhonxu
 *
 */
public class DefaultRoute extends AbstractRoute {

	public DefaultRoute(){
	}

	@Override
	public ServiceInformationEntity chooseRoute(String serviceName, ConsumerBean serviceCenterConsumerBean) throws ServiceException {
		ServiceInformationEntity serviceCenter = new ServiceInformationEntity();
		serviceCenter.setAddress(this.getRouteProperties().get(0));
		serviceCenter.setPort(Integer.parseInt(this.getRouteProperties().get(1)));
		return serviceCenter;	
	}
	
	private List<RouteFilter> filters;
	
	public List<RouteFilter> getFilters() {
		return filters;
	}

	public void setFilters(List<RouteFilter> filters) {
		this.filters = filters;
	}
}
