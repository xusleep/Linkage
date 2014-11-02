package service.framework.route;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import service.framework.common.SerializeUtils;
import service.framework.common.ShareingData;
import service.framework.common.entity.RequestResultEntity;
import service.framework.common.entity.ServiceInformationEntity;
import service.framework.comsume.ConsumerBean;
import service.framework.exception.ServiceException;
import service.framework.route.filters.RouteFilter;

public class ServiceCenterRoute extends AbstractRoute {
	private final ConcurrentHashMap<String, List<ServiceInformationEntity>> serviceListCache = new ConcurrentHashMap<String, List<ServiceInformationEntity>>(16);
	
	public ServiceCenterRoute(){
	}
	

	@Override
	public ServiceInformationEntity chooseRoute(String serviceName, ConsumerBean serviceCenterConsumerBean) throws ServiceException {
		//首先从cache中取得服务列表，cache中没有的话，再从服务中心获取
		List<ServiceInformationEntity> serviceList = serviceListCache.get(serviceName);
		String result = null;
		if(serviceList == null)
		{
			if(serviceName.equals(ShareingData.SERVICE_CENTER))
			{
				ServiceInformationEntity serviceCenter = new ServiceInformationEntity();
				serviceCenter.setAddress(this.getRouteProperties().get(0));
				serviceCenter.setPort(Integer.parseInt(this.getRouteProperties().get(1)));
				return serviceCenter;
			}
			else
			{
				List<String> list = new LinkedList<String>();
				list.add(serviceName);
				RequestResultEntity objRequestResultEntity = serviceCenterConsumerBean.prcessRequestPerConnectSync(ShareingData.SERVICE_CENTER, list);
				if(objRequestResultEntity.isException())
				{
					throw objRequestResultEntity.getException();
				}
				else
				{
					result = objRequestResultEntity.getResponseEntity().getResult();
					serviceList = SerializeUtils.deserializeServiceInformationList(result);
					serviceListCache.put(serviceName, serviceList);
				}
			}
		}
		if(filters != null)
		{
			for(RouteFilter filter : filters){
				serviceList = filter.filter(serviceList);
			}
		}
		if(serviceList.size() == 0)
			return null;
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
