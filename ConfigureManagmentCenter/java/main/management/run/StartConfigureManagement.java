package management.run;

import management.service.center.heartbeat.HeartBeatSender;
import service.framework.bootstrap.ClientBootStrap;
import service.framework.bootstrap.ServerBootStrap;


/**
 * <p>Title: ∆Ù∂Ø¿‡</p>
 * @author starboy
 * @version 1.0
 */

public class StartConfigureManagement {

    public static void main(String[] args) {
    	try {
    		ServerBootStrap objServerBootStrap = new ServerBootStrap("conf/configure_management_center.properties", 5);
    		objServerBootStrap.run();
			ClientBootStrap clientBootStrap = new ClientBootStrap("conf/configure_management_center_client.properties", 5);
			clientBootStrap.run();
			HeartBeatSender objHeartBeatSender = new HeartBeatSender(clientBootStrap.getConsume());
			new Thread(objHeartBeatSender).start();
			Thread.sleep(1000);
			objServerBootStrap.shutdown();
			objHeartBeatSender.shutdown();
			clientBootStrap.shutdown();
    	} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
}
