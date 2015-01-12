package service.middleware.linkage.center.route;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import service.middleware.linkage.center.clean.Cleaner;
import service.middleware.linkage.framework.serviceaccess.entity.RequestEntity;
import service.middleware.linkage.framework.serviceaccess.entity.ServiceInformationEntity;
/**
 *  this is interface for the route,
 *  the client will call a route to get the service list and choose the service
 * @author zhonxu
 *
 */
public interface Route extends Cleaner{
	public ServiceInformationEntity chooseRoute(RequestEntity requestEntity) throws IOException, InterruptedException, ExecutionException, Exception;
}
