package management.service.center.heartbeat;

import service.framework.common.ShareingData;

public class DefaultHeartBeatReceiver implements HeartBeatReceiver {

	@Override
	public String receive(String msg) {
		System.out.println("DefaultHeartBeatReceiver.receive " + msg);
		if(ShareingData.HEART_BEAT_SEND.equals(msg))
		{
			return ShareingData.HEART_BEAT_REPLY;
		}
		// TODO Auto-generated method stub
		return ShareingData.HEART_BEAT_ERROR;
	}

}
