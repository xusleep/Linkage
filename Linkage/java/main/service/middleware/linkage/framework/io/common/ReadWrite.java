package service.middleware.linkage.framework.io.common;
/**
 * this is the interface of read&wirte from the channel
 * @author zhonxu
 *
 */
public interface ReadWrite {
	public WorkingChannelOperationResult read();
	public WorkingChannelOperationResult write();
}
