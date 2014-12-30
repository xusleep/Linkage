package service.middleware.linkage.center.comsume;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import service.middleware.linkage.center.route.Route;
import service.middleware.linkage.center.route.ServiceCenterRoute;
import service.middleware.linkage.framework.common.StringUtils;
import service.middleware.linkage.framework.common.entity.RequestEntity;
import service.middleware.linkage.framework.common.entity.RequestResultEntity;
import service.middleware.linkage.framework.common.entity.ServiceInformationEntity;
import service.middleware.linkage.framework.exception.ServiceException;
import service.middleware.linkage.framework.io.common.NIOWorkingChannel;
import service.middleware.linkage.framework.io.common.WorkerPool;
import service.middleware.linkage.framework.serviceaccess.NIOServiceAccess;
import service.middleware.linkage.framework.setting.reader.ClientSettingReader;

public class DefaultRouteConsume extends NIOServiceAccess implements RouteConsume {
	private final Route route;
	private Log logger = LogFactory.getFactory().getInstance(DefaultRouteConsume.class);
	public DefaultRouteConsume(ClientSettingReader workingClientPropertyEntity, WorkerPool workerPool, ServiceInformationEntity centerServiceInformationEntity) {
		super(workingClientPropertyEntity, workerPool);
		this.route = new ServiceCenterRoute(centerServiceInformationEntity, this);
	}
	
	@Override
	public RequestResultEntity requestService(String clientID, List<String> args) {
		return requestService(clientID, args, true);
	}

	@Override
	public RequestResultEntity requestService(String clientID,
			List<String> args, boolean channelFromCached) {
		final RequestEntity objRequestEntity = consumeEngine.createRequestEntity(clientID, args);
        RequestResultEntity result = new RequestResultEntity();
        result.setRequestID(objRequestEntity.getRequestID());
    	// Find the service information from the route, set the information into the result entity as well
		ServiceInformationEntity serviceInformationEntity = null;
		try {
			serviceInformationEntity = route.chooseRoute(objRequestEntity);
			result.setServiceInformationEntity(serviceInformationEntity);
			if(serviceInformationEntity == null)
			{
				NIOWorkingChannel.setExceptionToRuquestResult(result, new ServiceException(new Exception("Can not find the service"), "Can not find the service"));
				return result;
			}
		} 
		catch(Exception ex)
		{
			logger.error(StringUtils.ExceptionStackTraceToString(ex));
			//logger.log(Level.WARNING, ex.getMessage());
			//System.out.println("ComsumerBean ... exception happend " + ex.getMessage());
			//ex.printStackTrace();
			NIOWorkingChannel.setExceptionToRuquestResult(result, new ServiceException(ex, "ComsumerBean ... exception happend"));
        	route.clean(result);
        	return result;
        }
		return consumeEngine.basicProcessRequest(objRequestEntity, result, serviceInformationEntity, channelFromCached);
	}

	@Override
	public RequestResultEntity requestServicePerConnect(String clientID,
			List<String> args) {
		return requestService(clientID, args, false);
	}

	@Override
	public RequestResultEntity requestServicePerConnectSync(String clientID,
			List<String> args) {
		RequestResultEntity result = requestService(clientID, args, false);
		result.getResponseEntity();
		this.closeChannelByRequestResult(result);
		return result;
	}
}
