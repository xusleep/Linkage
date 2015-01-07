package service.middleware.linkage.framework.io.common;

/**
 * this is the interface of read&wirte from the channel
 * @author zhonxu
 *
 */
public interface WorkingChannelReadWrite {
	/**
	 * read the data from the channel
	 * @return
	 */
	public WorkingChannelOperationResult read();
	/**
	 * write the data from the channel
	 * @return
	 */
	public WorkingChannelOperationResult write();
}
