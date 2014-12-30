package service.middleware.management.center.heartbeat;

import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;

import service.middleware.framework.common.ShareingData;
import service.middleware.framework.common.StringUtils;
import service.middleware.framework.common.entity.RequestResultEntity;
import service.middleware.framework.common.entity.ServiceInformationEntity;
import service.middleware.framework.serviceaccess.ServiceAccess;
import service.middleware.management.service.center.ServiceCenter;

public class HeartBeatSender implements Runnable {
	private static Logger  logger = Logger.getLogger(HeartBeatSender.class); 
	private final ServiceAccess consume;
	private volatile boolean isShutdown = false;
	
	public HeartBeatSender(ServiceAccess consume){
		this.consume = consume;
	}
	
	/**
	 * shutdown 
	 */
	public void shutdown()
	{
		logger.debug("shutdown heat beat sender.");
		isShutdown = true;
	}
	
	@Override
	public void run() {
		while(true){
			try {
				Thread.sleep(2000);
				if(isShutdown)
					break;
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
					logger.debug("service :" + objServiceInformationEntity.toString() + " no service found ...");
					try {
						logger.debug("sleep 1000ms.");
						Thread.sleep(10000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						logger.error("HeartBeatSender unexpected exception happned ..." + StringUtils.ExceptionStackTraceToString(e));
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
