package service.framework.route;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import service.framework.common.entity.ServiceInformationEntity;
import service.framework.comsume.ConsumerBean;
/**
 *  this is interface for the route,
 *  the client will call a route to get the service list and choose the service
 * @author zhonxu
 *
 */
public interface Route {
	public ServiceInformationEntity chooseRoute(String serviceName, ConsumerBean cb) throws IOException, InterruptedException, ExecutionException, Exception;
}
