package test.run;
import service.framework.bootstrap.ClientBootStrap;
import service.framework.bootstrap.ServerBootStrap;
import service.framework.handlers.ServiceRegisterHandler;


/**
 * <p>Title: ������</p>
 * @author starboy
 * @version 1.0
 */

public class StartService {

    public static void main(String[] args) {
        try {
        	ServerBootStrap serviceBootStrap = new ServerBootStrap("conf/service_server.properties", 5);
    		ClientBootStrap clientBootStrap = new ClientBootStrap("conf/service_client.properties", 5);
    		serviceBootStrap.getEventDistributionHandler().registerHandler(new ServiceRegisterHandler(clientBootStrap.getConsumerBean(), serviceBootStrap.getServicePropertyEntity()));
    		serviceBootStrap.run();
    		clientBootStrap.run();;
        }
        catch (Exception e) {
        	e.printStackTrace();
            System.out.println("Server error: " + e.getMessage());
            System.exit(-1);
        }
 
    }
}
