package service.middleware.linkage.framework.io.common;
/**
 * this is the interface of strategy of read&wirte from the channel
 * @author zhonxu
 *
 */
public interface ReadWriteStrategy {
	public boolean read(WorkingChannel workingChannel);
	public boolean write(WorkingChannel workingChannel);
}
