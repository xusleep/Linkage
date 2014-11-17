package management.service.center.heartbeat;

import java.util.LinkedList;
import java.util.List;

import management.service.center.ServiceCenter;
import service.framework.common.ShareingData;
import service.framework.common.entity.RequestResultEntity;
import service.framework.comsume.ConsumerBean;
import service.framework.exception.NoServiceRegisteredException;

public class HeartBeatSender implements Runnable {

	private final ConsumerBean consumerBean;
	
	public HeartBeatSender(ConsumerBean consumerBean){
		this.consumerBean = consumerBean;
	}
	
	@Override
	public void run() {
		while(true){
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			// TODO Auto-generated method stub
			List<String> args = new LinkedList<String>();
			args.add(ShareingData.HEART_BEAT_SEND);
			RequestResultEntity result = this.consumerBean.prcessRequestPerConnectSync(ShareingData.HEART_BEAT_CLIENT_ID, args);
			if(ShareingData.HEART_BEAT_REPLY.equals(result.getResponseEntity().getResult())){
				System.out.println("Service available");
			}
			// if there is an exception when request the heart beat to the service
			// we have to remove the service from the service list
			if(result.isException())
			{
				if(result.getException().getInnerException() instanceof NoServiceRegisteredException){
					System.out.println("no service found ...");
					try {
						Thread.sleep(10000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				result.getException().printStackTrace();
				if(result.getServiceInformationEntity() != null)
				{
					System.out.println("failed request information : " + result.getServiceInformationEntity().toString());
					System.out.println("remove information : " + result.getServiceInformationEntity().toString());
					ServiceCenter.serviceInformationList.remove(result.getServiceInformationEntity());
				}
			}
			else
			{
				System.out.println("sucessfull request information : " + result.getServiceInformationEntity().toString());
			}
		}
	}

}
