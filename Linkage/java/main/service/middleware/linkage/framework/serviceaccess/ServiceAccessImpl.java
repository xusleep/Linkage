package service.middleware.linkage.framework.serviceaccess;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import service.middleware.linkage.framework.exception.ServiceException;
import service.middleware.linkage.framework.io.WorkerPool;
import service.middleware.linkage.framework.serviceaccess.entity.RequestEntity;
import service.middleware.linkage.framework.serviceaccess.entity.RequestResultEntity;
import service.middleware.linkage.framework.serviceaccess.entity.ResponseEntity;
import service.middleware.linkage.framework.serviceaccess.entity.ServiceInformationEntity;
import service.middleware.linkage.framework.setting.reader.ClientSettingReader;

/**
 * the default consume which wrapped the method request
 * @author zhonxu
 *
 */
public class ServiceAccessImpl implements ServiceAccess{
	
	protected ServiceAccessEngine serviceEngine;
	 // use the concurrent hash map to store the request result list {@link RequestResultEntity}
	private final ConcurrentHashMap<String, RequestResultEntity> resultList = new ConcurrentHashMap<String, RequestResultEntity>(2048);

	public ServiceAccessImpl(ClientSettingReader workingClientPropertyEntity, WorkerPool workerPool) {
		serviceEngine = new ServiceAccessEngineImpl(workingClientPropertyEntity, workerPool);
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
		RequestEntity objRequestEntity = serviceEngine.createRequestEntity(clientID, args);
        RequestResultEntity result = new RequestResultEntity();
        result.setRequestID(objRequestEntity.getRequestID());
		return this.serviceEngine.basicProcessRequest(objRequestEntity, result, serviceInformationEntity, channelFromCached);
	}

	@Override
	public void closeChannelByRequestResult(
			RequestResultEntity objRequestResultEntity) {
		// TODO Auto-generated method stub
		serviceEngine.closeChannelByRequestResult(objRequestResultEntity);
	}

	@Override
	public ServiceAccessEngine getServiceAccessEngine() {
		// TODO Auto-generated method stub
		return serviceEngine;
	}

	/**
	 * when the response comes, use this method to set it. 
	 * @param objResponseEntity
	 */
	public RequestResultEntity setRequestResult(ResponseEntity objResponseEntity){
		return serviceEngine.setRequestResult(objResponseEntity);
	}
	
	/**
	 * clear the result
	 */
	public void clearAllResult(ServiceException exception){
		serviceEngine.clearAllResult(exception);
	}
}
