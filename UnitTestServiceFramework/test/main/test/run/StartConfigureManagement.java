package test.run;

import service.framework.bootstrap.ServerBootStrap;


/**
 * <p>Title: ������</p>
 * @author starboy
 * @version 1.0
 */

public class StartConfigureManagement {

    public static void main(String[] args) {
    	try {
			new ServerBootStrap("conf/servicecenter_server.properties", 5).run();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
}