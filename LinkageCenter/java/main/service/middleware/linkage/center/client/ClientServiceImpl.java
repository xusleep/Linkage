package service.middleware.linkage.center.client;

import static service.middleware.linkage.center.cache.ClientServiceInformationCache.addServiceInformationEntityList;
import static service.middleware.linkage.center.cache.ClientServiceInformationCache.removeServiceInformationEntityList;

import java.util.List;

import service.middleware.linkage.center.common.ServiceCenterUtils;
import service.middleware.linkage.framework.common.entity.ServiceInformationEntity;
/**
 * this interface is used in the client.
 * the configure management center will notify the client
 * that new service is joined or the service is not available
 * @author Smile
 *
 */
public class ClientServiceImpl implements ClientService {

	@Override
	public String removeService(String serviceInfor) {
		List<ServiceInformationEntity> objServiceInformationList = ServiceCenterUtils.deserializeServiceInformationList(serviceInfor);
		removeServiceInformationEntityList(objServiceInformationList);
		return null;
	}

	@Override
	public String addService(String serviceInfor) {
		List<ServiceInformationEntity> objServiceInformationList = ServiceCenterUtils.deserializeServiceInformationList(serviceInfor);
		addServiceInformationEntityList(objServiceInformationList);
		return "true";
	}
}
