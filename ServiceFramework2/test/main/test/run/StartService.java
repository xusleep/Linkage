package test.run;
import service.framework.bootstrap.ServiceBootStrap;


/**
 * <p>Title: ������</p>
 * @author starboy
 * @version 1.0
 */

public class StartService {

    public static void main(String[] args) {
    	Runtime.getRuntime().addShutdownHook(new Thread(){

			@Override
			public void run() {
				// TODO Auto-generated method stub
				System.out.println("run");
			}
    		
    	});
        try {
        	new ServiceBootStrap("conf/service.properties", 5, 5).run();
        	
        }
        catch (Exception e) {
        	e.printStackTrace();
            System.out.println("Server error: " + e.getMessage());
            System.exit(-1);
        }
 
    }
}
