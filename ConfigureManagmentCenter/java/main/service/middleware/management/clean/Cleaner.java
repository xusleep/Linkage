package service.middleware.management.clean;

import service.middleware.framework.common.entity.RequestResultEntity;

/**
 * this interface will deal with the clean job 
 * @author Smile
 *
 */
public interface Cleaner {
	public void clean(RequestResultEntity objRequestResultEntity);
}
