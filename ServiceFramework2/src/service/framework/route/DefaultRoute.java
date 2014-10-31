package service.framework.route;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import service.framework.io.client.comsume.ConsumerBean;
import service.framework.io.client.comsume.RequestResultEntity;
import service.framework.route.filters.RouteFilter;
import service.framework.serialization.SerializeUtils;
import service.properties.ServicePropertyEntity;
import servicecenter.service.ServiceInformation;

public class DefaultRoute implements Route {
	private final ServicePropertyEntity servicePropertyEntity;
	private final ConsumerBean serviceCenterConsumerBean;
	private final String SERVICE_CENTER = "serviceCenter";
	private final ConcurrentHashMap<String, List<ServiceInformation>> serviceListCache = new ConcurrentHashMap<String, List<ServiceInformation>>(16);
	
	public DefaultRoute(ServicePropertyEntity servicePropertyEntity, ConsumerBean serviceCenterConsumerBean){
		this.servicePropertyEntity = servicePropertyEntity;
		this.serviceCenterConsumerBean = serviceCenterConsumerBean;
	}

	@Override
	public ServiceInformation chooseRoute(String serviceName) throws Exception {
		//首先从cache中取得服务列表，cache中没有的话，再从服务中心获取
		List<ServiceInformation> serviceList = serviceListCache.get(serviceName);
		String result = null;
		if(serviceList == null)
		{
			if(serviceName.equals(SERVICE_CENTER))
			{
				ServiceInformation serviceCenter = new ServiceInformation();
				serviceCenter.setAddress(this.servicePropertyEntity.getServiceCenterAddress());
				serviceCenter.setPort(this.servicePropertyEntity.getServiceCenterPort());
				return serviceCenter;
			}
			else
			{
				List<String> list = new LinkedList<String>();
				list.add(serviceName);
				RequestResultEntity objRequestResultEntity  = this.serviceCenterConsumerBean.prcessRequest(SERVICE_CENTER, list);
				result = objRequestResultEntity.getResponseEntity().getResult();
				serviceList = SerializeUtils.deserializeServiceInformationList(result);
				serviceListCache.put(serviceName, serviceList);
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
		ServiceInformation service = serviceList.get(r.nextInt(serviceList.size()));
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
