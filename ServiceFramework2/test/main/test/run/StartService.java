package test.run;
import service.framework.bootstrap.ClientBootStrap;
import service.framework.bootstrap.ServiceBootStrap;
import service.framework.handlers.ServiceRegisterHandler;


/**
 * <p>Title: ∆Ù∂Ø¿‡</p>
 * @author starboy
 * @version 1.0
 */

public class StartService {

    public static void main(String[] args) {
        try {
        	ServiceBootStrap serviceBootStrap = new ServiceBootStrap("conf/service.properties", 5, 5);
    		ClientBootStrap clientBootStrap = new ClientBootStrap("conf/service.properties", 5);
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
