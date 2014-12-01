package management.service.center.heartbeat;

import java.util.LinkedList;
import java.util.List;

import management.service.center.ServiceCenter;

import org.apache.log4j.Logger;

import service.framework.common.ShareingData;
import service.framework.common.StringUtils;
import service.framework.common.entity.RequestResultEntity;
import service.framework.common.entity.ServiceInformationEntity;
import service.framework.comsume.Consume;
import service.framework.event.ServiceOnChannelCloseExeptionEvent;
import service.framework.exception.NoServiceRegisteredException;

public class HeartBeatSender implements Runnable {
	private static Logger  logger = Logger.getLogger(HeartBeatSender.class); 
	private final Consume consume;
	
	public HeartBeatSender(Consume consume){
		this.consume = consume;
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
			
			List<ServiceInformationEntity> failedServiceInformationList = new LinkedList<ServiceInformationEntity>();
			
			for(ServiceInformationEntity objServiceInformationEntity : ServiceCenter.serviceInformationList)
			{
				// TODO Auto-generated method stub
				List<String> args = new LinkedList<String>();
				args.add(ShareingData.HEART_BEAT_SEND);
				RequestResultEntity result = this.consume.requestServicePerConnectSync(ShareingData.HEART_BEAT_CLIENT_ID, args, objServiceInformationEntity);
				if(ShareingData.HEART_BEAT_REPLY.equals(result.getResponseEntity().getResult())){
					logger.debug("service :" + objServiceInformationEntity.toString() + " is available");
				}
				// if there is an exception when request the heart beat to the service
				// we have to remove the service from the service list
				if(result.isException())
				{
					if(result.getException().getInnerException() instanceof NoServiceRegisteredException){
						logger.debug("service :" + objServiceInformationEntity.toString() + " no service found ...");
						try {
							logger.debug("sleep 1000ms.");
							Thread.sleep(10000);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							logger.error("HeartBeatSender unexpected exception happned ..." + StringUtils.ExceptionStackTraceToString(e));
						}
					}
					logger.error("HeartBeatSender unexpected exception happned ..." + StringUtils.ExceptionStackTraceToString(result.getException()));
					if(result.getServiceInformationEntity() != null)
					{
						logger.debug("failed request information : " + result.getServiceInformationEntity().toString());
						logger.debug("will remove information : " + result.getServiceInformationEntity().toString());
						failedServiceInformationList.add(result.getServiceInformationEntity());
					}
				}
				else
				{
					logger.debug("sucessfull request information : " + result.getServiceInformationEntity().toString());
				}
			}
			
			ServiceCenter.serviceInformationList.removeAll(failedServiceInformationList);
		}
	}

}
