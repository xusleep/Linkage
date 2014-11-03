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
import service.framework.route.consistent.hash.ConsistentHash;
import service.framework.route.consistent.hash.HashFunction;
import service.framework.route.filters.RouteFilter;
/**
 * This route is used for the service center
 * If will first access the service center, get the service list
 * then choose one service from the list.
 * If the service list exist in the cache, it will use it directly without access it again
 * @author zhonxu
 *
 */
public class ServiceCenterConsistentHashRoute extends AbstractRoute {
	private final ConcurrentHashMap<String, ConsistentHash> serviceListCache = new ConcurrentHashMap<String, ConsistentHash>(16);
	
	public ServiceCenterConsistentHashRoute(){
	}
	

	@Override
	public ServiceInformationEntity chooseRoute(String serviceName, ConsumerBean serviceCenterConsumerBean) throws ServiceException {
		// get it from the cache first, if not exist get it from the service center then
		ConsistentHash consistentHash = serviceListCache.get(serviceName);
		String result = null;
		if(consistentHash == null)
		{
			// step 2, If request the service center's address, then we could return it back directly
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
				// step 1, request the service center for the service list, 
				//         then it will go to the step 2 to get the service center's address
				RequestResultEntity objRequestResultEntity = serviceCenterConsumerBean.prcessRequestPerConnectSync(ShareingData.SERVICE_CENTER, list);
				if(objRequestResultEntity.isException())
				{
					throw objRequestResultEntity.getException();
				}
				else
				{
					result = objRequestResultEntity.getResponseEntity().getResult();
					List<ServiceInformationEntity> serviceList = SerializeUtils.deserializeServiceInformationList(result);
					consistentHash = createConsistentHash(serviceList);
					serviceListCache.put(serviceName, consistentHash);
				}
			}
		}
		return consistentHash.get(serviceName);
	}
	
	private ConsistentHash<ServiceInformationEntity> createConsistentHash(
			List<ServiceInformationEntity> serviceList) {
		// TODO Auto-generated method stub
		return new ConsistentHash<ServiceInformationEntity>(new HashFunction(), ShareingData.CONSISTENT_HASH_CIRCLE_REPLICATE, serviceList);
	}

	private List<RouteFilter> filters;
	
	public List<RouteFilter> getFilters() {
		return filters;
	}

	public void setFilters(List<RouteFilter> filters) {
		this.filters = filters;
	}
}
