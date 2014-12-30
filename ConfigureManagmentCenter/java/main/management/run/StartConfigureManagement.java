package management.run;

import management.service.center.heartbeat.HeartBeatSender;
import service.framework.bootstrap.NIOClientBootStrap;
import service.framework.bootstrap.NIOServerBootStrap;


/**
 * <p>Title: ������</p>
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
