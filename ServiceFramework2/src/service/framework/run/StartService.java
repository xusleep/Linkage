package service.framework.run;
import service.framework.bootstrap.ServiceBootStrap;


/**
 * <p>Title: ∆Ù∂Ø¿‡</p>
 * @author starboy
 * @version 1.0
 */

public class StartService {

    public static void main(String[] args) {
        try {
        	new ServiceBootStrap("conf/service1.properties", 5, 5).run();
        }
        catch (Exception e) {
        	e.printStackTrace();
            System.out.println("Server error: " + e.getMessage());
            System.exit(-1);
        }
    }
}
