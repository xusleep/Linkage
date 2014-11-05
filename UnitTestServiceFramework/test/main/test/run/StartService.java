package test.run;
import management.service.center.common.ServiceCenterClientUtils;
import service.framework.bootstrap.ClientBootStrap;
import service.framework.bootstrap.ServerBootStrap;


/**
 * <p>Title: ∆Ù∂Ø¿‡</p>
 * @author starboy
 * @version 1.0
 */

public class StartService {

    public static void main(String[] args) {
        try {
        	ServerBootStrap serviceBootStrap = new ServerBootStrap("conf/service_server.properties", 5);
    		ClientBootStrap clientBootStrap = new ClientBootStrap("conf/service_client.properties", 5);
    		serviceBootStrap.run();
    		clientBootStrap.run();
    		ServiceCenterClientUtils.registerService(clientBootStrap.getConsumerBean(), serviceBootStrap.getServicePropertyEntity());
        }
        catch (Exception e) {
        	e.printStackTrace();
            System.out.println("Server error: " + e.getMessage());
            System.exit(-1);
        }
 
    }
}
