package management.service.center.route;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import management.service.clean.Cleaner;
import service.framework.common.entity.RequestEntity;
import service.framework.common.entity.ServiceInformationEntity;
/**
 *  this is interface for the route,
 *  the client will call a route to get the service list and choose the service
 * @author zhonxu
 *
 */
public interface Route extends Cleaner{
	public ServiceInformationEntity chooseRoute(RequestEntity requestEntity) throws IOException, InterruptedException, ExecutionException, Exception;
}
