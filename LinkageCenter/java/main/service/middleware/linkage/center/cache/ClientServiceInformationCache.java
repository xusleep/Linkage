package service.middleware.linkage.center.cache;

import java.util.LinkedList;
import java.util.List;

import service.middleware.linkage.framework.serviceaccess.entity.ServiceInformationEntity;
import service.middleware.linkage.framework.utils.StringUtils;

/**
 * 
 * @author Smile
 *
 */
public class ClientServiceInformationCache {
	private static final List<ServiceInformationEntity> serviceListCache = new LinkedList<ServiceInformationEntity>();
	
	/**
	 * add the service information entity to the cache
	 * @param objServiceInformationEntity
	 */
	public static synchronized void addServiceInformationEntity(ServiceInformationEntity objServiceInformationEntity){
		if(objServiceInformationEntity != null)
			serviceListCache.add(objServiceInformationEntity);
	}
	
	/**
	 * add the service information entity to the cache
	 * @param objServiceInformationEntity
	 */
	public static synchronized void addServiceInformationEntityList(List<ServiceInformationEntity> objServiceInformationEntityList){
		if(objServiceInformationEntityList != null)
			serviceListCache.addAll(objServiceInformationEntityList);
	}
	
	/**
	 * remove the service information entity from the cache
	 * @param objServiceInformationEntity
	 */
	public static synchronized void removeServiceInformationEntity(ServiceInformationEntity objServiceInformationEntity){
		if(objServiceInformationEntity != null)
			serviceListCache.remove(objServiceInformationEntity);
	}
	
	/**
	 * remove the service information entity list from the cache
	 * @param objServiceInformationEntity
	 */
	public static synchronized void removeServiceInformationEntityList(List<ServiceInformationEntity> objServiceInformationEntityList){
		if(objServiceInformationEntityList != null)
			serviceListCache.removeAll(objServiceInformationEntityList);
	}
	
	
	/**
	 * clear the service information entity
	 */
	public static synchronized void clearServiceInformationEntity(){
		serviceListCache.clear();
	}
	
	/**
	 * get the service list from the cache by service name
	 * @param serviceName
	 * @return
	 */
	public static synchronized List<ServiceInformationEntity> getServiceInformationEntityList(String serviceName){
		List<ServiceInformationEntity> resultList = new LinkedList<ServiceInformationEntity>();
		if(StringUtils.isEmpty(serviceName))
			return resultList;
		if(serviceListCache == null || serviceListCache.size() == 0)
			return resultList;
		for(ServiceInformationEntity objServiceInformationEntity:serviceListCache)
		{
			if(serviceName.equals(objServiceInformationEntity.getServiceName()))
			{
				resultList.add(objServiceInformationEntity);
			}
		}
		return resultList;
	}
}
