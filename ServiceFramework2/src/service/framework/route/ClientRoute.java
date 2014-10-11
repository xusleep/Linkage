package service.framework.route;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutionException;

import service.framework.io.client.comsume.ConsumerBean;
import service.framework.localcache.CacheElement;
import service.framework.localcache.ICacheElement;
import service.framework.localcache.IMemoryCache;
import service.framework.localcache.LRUMemoryCache;
import service.framework.route.filters.RouteFilter;
import service.framework.serialization.SerializeUtils;
import servicecenter.service.ServiceInformation;

public class ClientRoute implements Route {
	private List<RouteFilter> filters;
	private ConsumerBean serviceCenterConsumerBean;
	
	public ClientRoute(){
		
	}
	
	public List<RouteFilter> getFilters() {
		return filters;
	}

	public void setFilters(List<RouteFilter> filters) {
		this.filters = filters;
	}

	public ConsumerBean getServiceCenterConsumerBean() {
		return serviceCenterConsumerBean;
	}

	public void setServiceCenterConsumerBean(ConsumerBean serviceCenterConsumerBean) {
		this.serviceCenterConsumerBean = serviceCenterConsumerBean;
	}

	@Override
	public ServiceInformation chooseRoute(String serviceName) throws IOException, InterruptedException, ExecutionException {
		//首先从cache中取得服务列表，cache中没有的话，再从服务中心获取
		IMemoryCache cache = LRUMemoryCache.getInstance();
		ICacheElement ce = cache.get(serviceName);
		List<ServiceInformation> serviceList = null;
		String result = null;
		if(ce == null)
		{
			List<String> list = new LinkedList<String>();
			list.add(serviceName);
			long id = this.getServiceCenterConsumerBean().prcessRequest(list);
			result = this.getServiceCenterConsumerBean().getResult(id);
			ce = new CacheElement(serviceName, result);
			cache.update(ce);;
		}
		else
		{
			result = (String) ce.getVal();
		}
		serviceList = SerializeUtils.deserializeServiceInformationList(result);
		
		for(RouteFilter filter : filters){
			serviceList = filter.filter(serviceList);
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
}
