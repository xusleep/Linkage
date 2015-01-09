package service.middleware.linkage.framework.exception;

/**
 * this is a service  channel close exception defined for this frame work
 * the exception will be wrapped by it.
 * @author zhonxu
 *
 */
public class ServiceFileTransferErrorException extends ServiceException {

	public ServiceFileTransferErrorException(Exception innerException,
			String message) {
		super(innerException, message);
		// TODO Auto-generated constructor stub
	}

}
