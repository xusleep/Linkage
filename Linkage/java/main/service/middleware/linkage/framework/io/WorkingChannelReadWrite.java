package service.middleware.linkage.framework.io;

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
	public WorkingChannelOperationResult readChannel();
	/**
	 * write the data from the channel
	 * @return
	 */
	public WorkingChannelOperationResult writeChannel();
}
