package management.service.client;

import java.util.List;

import management.service.center.common.ServiceCenterUtils;
import service.framework.common.entity.ServiceInformationEntity;

public class ClientServiceImpl implements ClientService {

	@Override
	public String removeService(String serviceInfor) {
		List<ServiceInformationEntity> objServiceInformationList = ServiceCenterUtils.deserializeServiceInformationList(serviceInfor);
		// TODO Auto-generated method stub
		return null;
	}

}
