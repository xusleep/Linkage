package test.run;

import service.framework.bootstrap.ServiceBootStrap;


/**
 * <p>Title: ∆Ù∂Ø¿‡</p>
 * @author starboy
 * @version 1.0
 */

public class StartCenter {

    public static void main(String[] args) {
    	try {
			new ServiceBootStrap("conf/servicecenter.properties", 5, 5).run();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
}
