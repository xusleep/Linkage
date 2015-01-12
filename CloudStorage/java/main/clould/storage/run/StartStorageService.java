package clould.storage.run;

import clould.storage.service.handler.FileHandler;
import service.middleware.linkage.center.bootstrap.NIOCenterClientBootStrap;
import service.middleware.linkage.center.client.ServiceCenterClientUtils;
import service.middleware.linkage.framework.bootstrap.NIOMessageModeServerBootStrap;
import service.middleware.linkage.framework.io.nio.strategy.mixed.NIOMixedStrategy;
import service.middleware.linkage.framework.io.nio.strategy.mixed.packet.FileInformationEntity;
import service.middleware.linkage.framework.serviceaccess.entity.ServiceInformationEntity;


/**
 * 
 * @author Smile
 *
 */
public class StartStorageService {

    public static void main(String[] args) {
        try {
        	NIOMessageModeServerBootStrap serviceBootStrap = new NIOMessageModeServerBootStrap("conf/service_server.properties", 5);
        	ServiceInformationEntity centerServiceInformationEntity = new ServiceInformationEntity();
        	centerServiceInformationEntity.setAddress("localhost");
        	centerServiceInformationEntity.setPort(5002);
        	NIOCenterClientBootStrap clientBootStrap = new NIOCenterClientBootStrap("conf/service_client.properties", 
        			5, centerServiceInformationEntity);
    		serviceBootStrap.run();
    		clientBootStrap.run();
    		clientBootStrap.getEventDistributionMaster().addHandler(new FileHandler());
    		ServiceCenterClientUtils.defaultRouteConsume = clientBootStrap.getServiceAccess();
    		try {
    			ServiceCenterClientUtils.registerServiceList(ServiceCenterClientUtils.defaultRouteConsume, centerServiceInformationEntity, serviceBootStrap.getServicePropertyEntity());
    		}
    		catch (Exception e) {
    			
    		}
        }
        catch (Exception e) {
        	e.printStackTrace();
            System.out.println("Server error: " + e.getMessage());
        }
 
    }
}
