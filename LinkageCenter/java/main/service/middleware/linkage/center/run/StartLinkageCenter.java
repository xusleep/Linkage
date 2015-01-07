package service.middleware.linkage.center.run;

import service.middleware.linkage.center.heartbeat.HeartBeatSender;
import service.middleware.linkage.framework.bootstrap.NIOMessageModeClientBootStrap;
import service.middleware.linkage.framework.bootstrap.NIOMessageModeServerBootStrap;


/**
 * <p>Title: ∆Ù∂Ø¿‡</p>
 * @author starboy
 * @version 1.0
 */

public class StartLinkageCenter {

    public static void main(String[] args) {
    	try {
    		NIOMessageModeServerBootStrap objServerBootStrap = new NIOMessageModeServerBootStrap("conf/linkage_center_service.properties", 5);
    		objServerBootStrap.run();
			NIOMessageModeClientBootStrap clientBootStrap = new NIOMessageModeClientBootStrap("conf/linkage_center_client.properties", 5);
			clientBootStrap.run();
			HeartBeatSender objHeartBeatSender = new HeartBeatSender(clientBootStrap.getServiceAccess());
			new Thread(objHeartBeatSender).start();
			//Thread.sleep(1000);
			//objServerBootStrap.shutdown();
			//objHeartBeatSender.shutdown();
			//clientBootStrap.shutdown();
    	} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
}
