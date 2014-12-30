package service.middleware.linkage.framework.provider;

import service.middleware.linkage.framework.common.entity.RequestEntity;
import service.middleware.linkage.framework.common.entity.ResponseEntity;

/**
 * This class provide a service access point 
 * for all of the service
 * @author zhonxu
 *
 */
public interface ServiceProvider {
	/**
	 * process the request from the client
	 * @param request
	 * @return
	 */
	public ResponseEntity acceptServiceRequest(RequestEntity request);
}
