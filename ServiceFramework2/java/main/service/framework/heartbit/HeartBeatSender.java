package service.framework.heartbit;

import java.util.LinkedList;
import java.util.List;

import service.framework.common.ShareingData;
import service.framework.common.entity.RequestResultEntity;
import service.framework.comsume.ConsumerBean;

public class HeartBeatSender implements Runnable {

	private final ConsumerBean consumerBean;
	
	public HeartBeatSender(ConsumerBean consumerBean){
		this.consumerBean = consumerBean;
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		List<String> args = new LinkedList<String>();
		args.add(ShareingData.HEART_BEAT_SEND);
		RequestResultEntity result = this.consumerBean.prcessRequestPerConnectSync(ShareingData.HEART_BEAT_CLIENT_ID, args);
		if(ShareingData.HEART_BEAT_REPLY.equals(result.getResponseEntity().getResult())){
			// successful
		}
	}

}
