package service.middleware.linkage.center.run;

import service.middleware.linkage.center.heartbeat.HeartBeatSender;
import service.middleware.linkage.framework.bootstrap.NIOClientBootStrap;
import service.middleware.linkage.framework.bootstrap.NIOServerBootStrap;


/**
 * <p>Title: ∆Ù∂Ø¿‡</p>
 * @author starboy
 * @version 1.0
 */

public class StartLinkageCenter {

    public static void main(String[] args) {
    	try {
    		NIOServerBootStrap objServerBootStrap = new NIOServerBootStrap("conf/linkage_center_service.properties", 5);
    		objServerBootStrap.run();
			NIOClientBootStrap clientBootStrap = new NIOClientBootStrap("conf/linkage_center_client.properties", 5);
			clientBootStrap.run();
			HeartBeatSender objHeartBeatSender = new HeartBeatSender(clientBootStrap.getConsume());
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
