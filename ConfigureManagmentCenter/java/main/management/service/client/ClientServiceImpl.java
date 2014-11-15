package management.service.client;

import java.util.List;

import management.service.center.common.ServiceCenterUtils;
import service.framework.common.entity.ServiceInformationEntity;
import static management.service.cache.ClientServiceInformationCache.addServiceInformationEntityList;
import static management.service.cache.ClientServiceInformationCache.getServiceInformationEntityList;
import static management.service.cache.ClientServiceInformationCache.removeServiceInformationEntityList;
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
