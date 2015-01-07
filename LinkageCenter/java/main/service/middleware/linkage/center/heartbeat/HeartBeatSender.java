package service.middleware.linkage.center.heartbeat;

import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;

import service.middleware.linkage.center.ServiceCenter;
import service.middleware.linkage.framework.common.ShareingData;
import service.middleware.linkage.framework.common.StringUtils;
import service.middleware.linkage.framework.common.entity.RequestResultEntity;
import service.middleware.linkage.framework.common.entity.ServiceInformationEntity;
import service.middleware.linkage.framework.serviceaccess.ServiceAccess;

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
			synchronized(ServiceCenter.serviceInformationList)
			{
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
						logger.debug("sucessfull request information : " + result.getServiceInformationEntity().toDetailString());
					}
				}
				ServiceCenter.serviceInformationList.removeAll(failedServiceInformationList);
			}
			try {
				Thread.sleep(2000);
				logger.debug("Sleeping for 2 seconds and loop the service again." );
				if(isShutdown)
					break;
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

}
