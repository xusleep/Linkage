package service.middleware.management.run;

import service.middleware.framework.bootstrap.NIOClientBootStrap;
import service.middleware.framework.bootstrap.NIOServerBootStrap;
import service.middleware.management.center.heartbeat.HeartBeatSender;


/**
 * <p>Title: ∆Ù∂Ø¿‡</p>
 * @author starboy
 * @version 1.0
 */

public class StartConfigureManagement {

    public static void main(String[] args) {
    	try {
    		NIOServerBootStrap objServerBootStrap = new NIOServerBootStrap("conf/configure_management_center.properties", 5);
    		objServerBootStrap.run();
			NIOClientBootStrap clientBootStrap = new NIOClientBootStrap("conf/configure_management_center_client.properties", 5);
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
