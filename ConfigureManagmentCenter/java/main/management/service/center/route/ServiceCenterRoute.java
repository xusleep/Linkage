package management.service.center.route;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import management.service.center.common.ServiceCenterUtils;
import service.framework.common.entity.RequestEntity;
import service.framework.common.entity.RequestResultEntity;
import service.framework.common.entity.ServiceInformationEntity;
import service.framework.comsume.ConsumerBean;
import service.framework.exception.ServiceException;
import service.framework.route.AbstractRoute;
import service.framework.route.filters.RouteFilter;
/**
 * This route is used for the service center
 * If will first access the service center, get the service list
 * then choose one service from the list.
 * If the service list exist in the cache, it will use it directly without access it again
 * @author zhonxu
 *
 */
public class ServiceCenterRoute extends AbstractRoute {
	private final ConcurrentHashMap<String, List<ServiceInformationEntity>> serviceListCache = new ConcurrentHashMap<String, List<ServiceInformationEntity>>(16);
	
	public ServiceCenterRoute(){
	}
	

	@Override
	public ServiceInformationEntity chooseRoute(RequestEntity requestEntity, ConsumerBean serviceCenterConsumerBean) throws ServiceException {
		// get it from the cache first, if not exist get it from the service center then
		List<ServiceInformationEntity> serviceList = serviceListCache.get(requestEntity.getServiceName());
		String result = null;
		if(serviceList == null)
		{
			// step 2, If request the service center's address, then we could return it back directly
			if(ServiceCenterUtils.SERVICE_CENTER_SERVICE_NAME.equals(requestEntity.getServiceName()))
			{
				ServiceInformationEntity serviceCenter = new ServiceInformationEntity();
				serviceCenter.setAddress(this.getRouteProperties().get(0));
				serviceCenter.setPort(Integer.parseInt(this.getRouteProperties().get(1)));
				return serviceCenter;
			}
			else
			{
				List<String> list = new LinkedList<String>();
				list.add(requestEntity.getServiceName());
				// step 1, request the service center for the service list, 
				//         then it will go to the step 2 to get the service center's address
				RequestResultEntity objRequestResultEntity = serviceCenterConsumerBean.prcessRequestPerConnectSync(ServiceCenterUtils.SERVICE_CENTER_GET_SERVICE_ID, list);
				if(objRequestResultEntity.isException())
				{
					throw objRequestResultEntity.getException();
				}
				else
				{
					result = objRequestResultEntity.getResponseEntity().getResult();
					serviceList = ServiceCenterUtils.deserializeServiceInformationList(result);
					serviceListCache.put(requestEntity.getServiceName(), serviceList);
				}
			}
		}
		// if configure the filter, we would use it the filter the route
		if(filters != null)
		{
			for(RouteFilter filter : filters){
				serviceList = filter.filter(serviceList);
			}
		}
		if(serviceList.size() == 0)
			return null;
		// if there are more than one service exist, choose it random
		Random r = new Random();
		ServiceInformationEntity service = serviceList.get(r.nextInt(serviceList.size()));
		while(service == null)
		{
			service = serviceList.get(r.nextInt(serviceList.size()));
		}
		return service;
	}
	
	private List<RouteFilter> filters;
	
	public List<RouteFilter> getFilters() {
		return filters;
	}

	public void setFilters(List<RouteFilter> filters) {
		this.filters = filters;
	}
}
