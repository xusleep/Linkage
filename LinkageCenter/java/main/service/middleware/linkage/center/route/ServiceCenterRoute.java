package service.middleware.linkage.center.route;

import static service.middleware.linkage.center.cache.ClientServiceInformationCache.addServiceInformationEntityList;
import static service.middleware.linkage.center.cache.ClientServiceInformationCache.getServiceInformationEntityList;
import static service.middleware.linkage.center.cache.ClientServiceInformationCache.removeServiceInformationEntity;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import service.middleware.linkage.center.client.ServiceCenterClientUtils;
import service.middleware.linkage.center.common.ServiceCenterUtils;
import service.middleware.linkage.center.route.filters.RouteFilter;
import service.middleware.linkage.framework.exception.ServiceException;
import service.middleware.linkage.framework.serviceaccess.ServiceAccess;
import service.middleware.linkage.framework.serviceaccess.entity.RequestEntity;
import service.middleware.linkage.framework.serviceaccess.entity.RequestResultEntity;
import service.middleware.linkage.framework.serviceaccess.entity.ServiceInformationEntity;
/**
 * This route is used for the service center
 * If will first access the service center, get the service list
 * then choose one service from the list.
 * If the service list exist in the cache, it will use it directly without access it again
 * @author zhonxu
 *
 */
public class ServiceCenterRoute implements Route {
	private final ServiceInformationEntity centerServiceInformationEntity;
	private final ServiceAccess consume;
	
	public ServiceCenterRoute(ServiceInformationEntity centerServiceInformationEntity, ServiceAccess consume){
		this.centerServiceInformationEntity = centerServiceInformationEntity;
		this.consume = consume;
	}

	@Override
	public ServiceInformationEntity chooseRoute(RequestEntity requestEntity) throws ServiceException {
		// get it from the cache first, if not exist get it from the service center then
		List<ServiceInformationEntity> serviceList = getServiceInformationEntityList(requestEntity.getServiceName());
		String result = null;
		if(serviceList == null || serviceList.size() == 0)
		{
			synchronized(this){
				serviceList = getServiceInformationEntityList(requestEntity.getServiceName());
				if(serviceList == null || serviceList.size() == 0)
				{
					List<String> list = new LinkedList<String>();
					list.add(requestEntity.getServiceName());
					// step 1, request the service center for the service list, 
					//         then it will go to the step 2 to get the service center's address
					RequestResultEntity objRequestResultEntity = consume.requestServicePerConnectSync(ServiceCenterClientUtils.SERVICE_CENTER_GET_SERVICE_ID, list, centerServiceInformationEntity);
					if(objRequestResultEntity.isException())
					{
						throw objRequestResultEntity.getException();
					}
					else
					{
						result = objRequestResultEntity.getResponseEntity().getResult();
						serviceList = ServiceCenterUtils.deserializeServiceInformationList(result);
						addServiceInformationEntityList(serviceList);
					}
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

	@Override
	public void clean(RequestResultEntity objRequestResultEntity) {
		// TODO Auto-generated method stub
		removeServiceInformationEntity(objRequestResultEntity.getServiceInformationEntity());
	}
}
