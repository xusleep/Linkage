package test.run;
import management.bootstrap.CenterClientBootStrap;
import management.service.client.ServiceCenterClientUtils;
import service.framework.bootstrap.ServerBootStrap;
import service.framework.common.entity.ServiceInformationEntity;


/**
 * <p>Title: ������</p>
 * @author starboy
 * @version 1.0
 */

public class StartService {

    public static void main(String[] args) {
        try {
        	ServerBootStrap serviceBootStrap = new ServerBootStrap("conf/service_server.properties", 5);
        	ServiceInformationEntity centerServiceInformationEntity = new ServiceInformationEntity();
        	centerServiceInformationEntity.setAddress("localhost");
        	centerServiceInformationEntity.setPort(5002);
        	CenterClientBootStrap clientBootStrap = new CenterClientBootStrap("conf/service_client.properties", 
        			5, centerServiceInformationEntity);
    		serviceBootStrap.run();
    		clientBootStrap.run();
    		ServiceCenterClientUtils.defaultRouteConsume = clientBootStrap.getConsume();
    		try {
    			ServiceCenterClientUtils.registerServiceList(ServiceCenterClientUtils.defaultRouteConsume, centerServiceInformationEntity, serviceBootStrap.getServicePropertyEntity());
    		}
    		catch (Exception e) {}
    		
    		Thread.sleep(1000);
    		serviceBootStrap.shutdown();
    		clientBootStrap.shutdown();
        }
        catch (Exception e) {
        	e.printStackTrace();
            System.out.println("Server error: " + e.getMessage());
        }
 
    }
}
