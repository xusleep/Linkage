package management.service.cache;

import java.util.LinkedList;
import java.util.List;

import service.framework.common.StringUtils;
import service.framework.common.entity.ServiceInformationEntity;

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
	public static void addServiceInformationEntity(ServiceInformationEntity objServiceInformationEntity){
		if(objServiceInformationEntity != null)
			serviceListCache.add(objServiceInformationEntity);
	}
	
	/**
	 * add the service information entity to the cache
	 * @param objServiceInformationEntity
	 */
	public static void addServiceInformationEntityList(List<ServiceInformationEntity> objServiceInformationEntityList){
		if(objServiceInformationEntityList != null)
			serviceListCache.addAll(objServiceInformationEntityList);
	}
	
	/**
	 * remove the service information entity from the cache
	 * @param objServiceInformationEntity
	 */
	public static void removeServiceInformationEntity(ServiceInformationEntity objServiceInformationEntity){
		if(objServiceInformationEntity != null)
			serviceListCache.remove(objServiceInformationEntity);
	}
	
	/**
	 * remove the service information entity list from the cache
	 * @param objServiceInformationEntity
	 */
	public static void removeServiceInformationEntityList(List<ServiceInformationEntity> objServiceInformationEntityList){
		if(objServiceInformationEntityList != null)
			serviceListCache.removeAll(objServiceInformationEntityList);
	}
	
	
	/**
	 * clear the service information entity
	 */
	public static void clearServiceInformationEntity(){
		serviceListCache.clear();
	}
	
	/**
	 * get the service list from the cache by service name
	 * @param serviceName
	 * @return
	 */
	public static List<ServiceInformationEntity> getServiceInformationEntityList(String serviceName){
		List<ServiceInformationEntity> resultList = new LinkedList<ServiceInformationEntity>();
		if(StringUtils.isEmpty(serviceName))
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
