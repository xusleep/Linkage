package service.middleware.linkage.framework.serviceaccess;

import java.util.List;

import service.middleware.linkage.framework.common.entity.RequestEntity;
import service.middleware.linkage.framework.common.entity.RequestResultEntity;
import service.middleware.linkage.framework.common.entity.ServiceInformationEntity;
import service.middleware.linkage.framework.io.common.WorkerPool;
import service.middleware.linkage.framework.io.common.WorkingChannel;
import service.middleware.linkage.framework.setting.reader.ClientSettingReader;

/**
 * the default consume which wrapped the method request
 * @author zhonxu
 *
 */
public class NIOServiceAccess implements ServiceAccess {
	
	protected NIOServiceAccessEngine consumeEngine;

	public NIOServiceAccess(ClientSettingReader workingClientPropertyEntity, WorkerPool workerPool) {
		consumeEngine = new NIOServiceAccessEngine(workingClientPropertyEntity, workerPool);
	}

	@Override
	public RequestResultEntity requestService(String clientID, List<String> args, ServiceInformationEntity serviceInformationEntity) {
		return requestService(clientID, args, serviceInformationEntity, true);
	}

	@Override
	public RequestResultEntity requestServicePerConnect(String clientID,
			List<String> args, ServiceInformationEntity serviceInformationEntity) {
		// TODO Auto-generated method stub
		return requestService(clientID, args, serviceInformationEntity, false);
	}

	@Override
	public RequestResultEntity requestServicePerConnectSync(String clientID,
			List<String> args, ServiceInformationEntity serviceInformationEntity) {
		RequestResultEntity result = requestService(clientID, args, serviceInformationEntity, false);
		result.getResponseEntity();
		result.setServiceInformationEntity(serviceInformationEntity);
		this.closeChannelByRequestResult(result);
		return result;
	}

	@Override
	public RequestResultEntity requestService(String clientID, List<String> args, 
			ServiceInformationEntity serviceInformationEntity, boolean channelFromCached) {
		RequestEntity objRequestEntity = consumeEngine.createRequestEntity(clientID, args);
        RequestResultEntity result = new RequestResultEntity();
        result.setRequestID(objRequestEntity.getRequestID());
		return this.consumeEngine.basicProcessRequest(objRequestEntity, result, serviceInformationEntity, channelFromCached);
	}

	@Override
	public void closeChannelByRequestResult(
			RequestResultEntity objRequestResultEntity) {
		// TODO Auto-generated method stub
		consumeEngine.closeChannelByRequestResult(objRequestResultEntity);
	}

	@Override
	public void removeCachedChannel(WorkingChannel objWorkingChannel) {
		// TODO Auto-generated method stub
		consumeEngine.removeCachedChannel(objWorkingChannel);
	}

	@Override
	public NIOServiceAccessEngine getConsumeEngine() {
		// TODO Auto-generated method stub
		return consumeEngine;
	}
}
