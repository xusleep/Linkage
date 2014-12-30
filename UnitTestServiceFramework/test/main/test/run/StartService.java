package test.run;
import management.bootstrap.NIOCenterClientBootStrap;
import management.service.client.ServiceCenterClientUtils;
import service.framework.bootstrap.NIOServerBootStrap;
import service.framework.common.entity.ServiceInformationEntity;


/**
 * <p>Title: ∆Ù∂Ø¿‡</p>
 * @author starboy
 * @version 1.0
 */

public class StartService {

    public static void main(String[] args) {
        try {
        	NIOServerBootStrap serviceBootStrap = new NIOServerBootStrap("conf/service_server.properties", 5);
        	ServiceInformationEntity centerServiceInformationEntity = new ServiceInformationEntity();
        	centerServiceInformationEntity.setAddress("localhost");
        	centerServiceInformationEntity.setPort(5002);
        	NIOCenterClientBootStrap clientBootStrap = new NIOCenterClientBootStrap("conf/service_client.properties", 
        			5, centerServiceInformationEntity);
    		serviceBootStrap.run();
    		clientBootStrap.run();
    		ServiceCenterClientUtils.defaultRouteConsume = clientBootStrap.getConsume();
    		try {
    			ServiceCenterClientUtils.registerServiceList(ServiceCenterClientUtils.defaultRouteConsume, centerServiceInformationEntity, serviceBootStrap.getServicePropertyEntity());
    		}
    		catch (Exception e) {
    			
    		}
    		
    		//Thread.sleep(1000);
    		//serviceBootStrap.shutdownImediate();
    		//clientBootStrap.shutdownImediate();
        }
        catch (Exception e) {
        	e.printStackTrace();
            System.out.println("Server error: " + e.getMessage());
        }
 
    }
}
