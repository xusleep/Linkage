package service.middleware.linkage.center.heartbeat;

import org.apache.log4j.Logger;

import service.middleware.linkage.framework.utils.ShareingData;

public class DefaultHeartBeatReceiver implements HeartBeatReceiver {
	private static Logger  logger = Logger.getLogger(HeartBeatSender.class); 
	@Override
	public String receive(String msg) {
		logger.debug("DefaultHeartBeatReceiver.receive " + msg);
		if(ShareingData.HEART_BEAT_SEND.equals(msg))
		{
			return ShareingData.HEART_BEAT_REPLY;
		}
		return ShareingData.HEART_BEAT_ERROR;
	}

}
