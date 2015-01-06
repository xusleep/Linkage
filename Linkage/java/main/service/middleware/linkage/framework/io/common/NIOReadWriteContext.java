package service.middleware.linkage.framework.io.common;

import java.util.LinkedList;
import java.util.List;

import service.middleware.linkage.framework.distribution.EventDistributionMaster;

public class NIOReadWriteContext implements ReadWriteContext {

	private List<ReadWriteStrategy> readWriteStrategyList = new LinkedList<ReadWriteStrategy>();
	
	public NIOReadWriteContext(EventDistributionMaster eventDistributionHandler){
		readWriteStrategyList.add(new NIOMessageReadWriteStrategy(eventDistributionHandler));
		readWriteStrategyList.add(new NIOFileReadWriteStrategy(eventDistributionHandler));
	}
	
	/**
	 * find the proper strategy for the working mode
	 * @param workingMode
	 * @return
	 */
	private ReadWriteStrategy findStrategy(NIOWorkingMode workingMode){
		for(ReadWriteStrategy readWriteStrategy : readWriteStrategyList){
			if(workingMode == NIOWorkingMode.MessageMode 
					&& readWriteStrategy instanceof NIOMessageReadWriteStrategy){
				return readWriteStrategy;
			}
			else if(workingMode == NIOWorkingMode.FileMode 
					&& readWriteStrategy instanceof NIOFileReadWriteStrategy){
				return readWriteStrategy;
			}
		}
		return null;
	}
	
	@Override
	public boolean read(WorkingChannel workingChannel) {
		NIOWorkingChannel nioworkingChannel = (NIOWorkingChannel)workingChannel;
		return findStrategy(nioworkingChannel.getWorkingMode()).read(workingChannel);
	}

	@Override
	public boolean write(WorkingChannel workingChannel) {
		NIOWorkingChannel nioworkingChannel = (NIOWorkingChannel)workingChannel;
		return findStrategy(nioworkingChannel.getWorkingMode()).write(workingChannel);
	}

}
