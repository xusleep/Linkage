package management.service.center.route;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import management.service.center.common.ConsistentHash;
import management.service.center.common.HashFunction;
import management.service.center.common.ServiceCenterUtils;
import service.framework.common.ShareingData;
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
 * 
 * this route can be used in the cache list get
 * all of the caches can register their information in the service center
 * by using this route, we could get the service list then create a consisten hash circle
 * we could get the service from the circle by using the object hash value
 * @author zhonxu
 *
 */
public class ServiceCenterConsistentHashRoute extends AbstractRoute {
	private final ConcurrentHashMap<String, ConsistentHash> serviceListCache = new ConcurrentHashMap<String, ConsistentHash>(16);
	
	public ServiceCenterConsistentHashRoute(){
	}
	

	@Override
	public ServiceInformationEntity chooseRoute(RequestEntity requestEntity, ConsumerBean serviceCenterConsumerBean) throws ServiceException {
		// get it from the cache first, if not exist get it from the service center then
		ConsistentHash<?> consistentHash = serviceListCache.get(requestEntity.getServiceName());
		String result = null;
		if(consistentHash == null)
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
					List<ServiceInformationEntity> serviceList = ServiceCenterUtils.deserializeServiceInformationList(result);
					consistentHash = createConsistentHash(serviceList);
					serviceListCache.put(requestEntity.getServiceName(), consistentHash);
				}
			}
		}
		// get the service through the arguments's route path
		return (ServiceInformationEntity) consistentHash.get(requestEntity.getArgs().get(0));
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
