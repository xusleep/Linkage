package test.run;

import service.framework.bootstrap.ServiceCenterBootStrap;


/**
 * <p>Title: ∆Ù∂Ø¿‡</p>
 * @author starboy
 * @version 1.0
 */

public class StartCenter {

    public static void main(String[] args) {
    	new ServiceCenterBootStrap("conf/servicecenter.properties", 5).run();
    }
}
