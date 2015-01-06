package service.middleware.linkage.framework.io.common;

import java.nio.channels.SocketChannel;

import org.apache.log4j.Logger;

import service.middleware.linkage.framework.distribution.EventDistributionMaster;

public class NIOFileReadWriteStrategy implements ReadWriteStrategy {
	private static Logger  logger = Logger.getLogger(NIOFileReadWriteStrategy.class);
	
	public NIOFileReadWriteStrategy(EventDistributionMaster eventDistributionHandler){
	}
	
	@Override
	public boolean read(WorkingChannel workingChannel) {
		final NIOWorkingChannel nioWorkingChannel = (NIOWorkingChannel)workingChannel;
		SocketChannel ch = (SocketChannel) nioWorkingChannel.getChannel();
		int readBytes = 0;
		int ret = 0;
		boolean success = false;
		return success;
	}

	@Override
	public boolean write(WorkingChannel workingChannel) {
		return false;
	}

}
