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
			new ServerBootStrap("conf/configure_management_center.properties", 5).run();
			ClientBootStrap clientBootStrap = new ClientBootStrap("conf/configure_management_center_client.properties", 5);
			clientBootStrap.run();
			new Thread(new HeartBeatSender(clientBootStrap.getConsumerBean())).start();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
}
