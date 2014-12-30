package service.middleware.linkage.framework.exception;

/**
 * this is a service  channel io exception defined for this frame work
 * the exception will be wrapped by it.
 * @author zhonxu
 *
 */
public class ServiceOnChanelIOException extends ServiceException {

	public ServiceOnChanelIOException(Exception innerException,
			String message) {
		super(innerException, message);
		// TODO Auto-generated constructor stub
	}

}
