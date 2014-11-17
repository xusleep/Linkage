package service.framework.clean;

import service.framework.common.entity.RequestResultEntity;

/**
 * this interface will deal with the clean job 
 * @author Smile
 *
 */
public interface Cleaner {
	public void clean(RequestResultEntity objRequestResultEntity);
}
