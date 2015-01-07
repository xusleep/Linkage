package service.middleware.linkage.framework.io.common;

import service.middleware.linkage.framework.handlers.NIOFileEventDistributionMaster;

/**
 * this strategy is only used for the file mode only
 * @author zhonxu
 *
 */
public class NIOFileWorkingChannelStrategy implements WorkingChannelStrategy {

	public NIOFileWorkingChannelStrategy(NIOWorkingChannelContext nioWorkingChannelContext,
			NIOFileEventDistributionMaster eventDistributionHandler) {
		// TODO Auto-generated constructor stub
	}

	@Override
	public WorkingChannelOperationResult readChannel() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public WorkingChannelOperationResult writeChannel() {
		// TODO Auto-generated method stub
		return null;
	}

}
