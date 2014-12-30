package service.middleware.framework.provider;

import service.middleware.framework.common.entity.RequestEntity;
import service.middleware.framework.common.entity.ResponseEntity;

/**
 * This class provide a service access point 
 * for all of the service
 * @author zhonxu
 *
 */
public interface Provider {
	/**
	 * process the request from the client
	 * @param request
	 * @return
	 */
	public ResponseEntity acceptServiceRequest(RequestEntity request);
}
