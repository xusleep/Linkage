package test.run;
import service.middleware.linkage.center.bootstrap.NIOCenterClientBootStrap;
import service.middleware.linkage.framework.bootstrap.NIOMessageModeServerBootStrap;
import service.middleware.linkage.framework.common.entity.ServiceInformationEntity;
import service.middleware.linkage.framework.io.nio.FileInformationEntity;
import service.middleware.linkage.framework.io.nio.strategy.mixed.NIOFileMessageMixStrategy;


/**
 * 
 * @author Smile
 *
 */
public class StartService {

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
    		FileInformationEntity fileInformationEntity = new FileInformationEntity();
    		fileInformationEntity.setFileID(1000);
    		fileInformationEntity.setFilePath("E:\\testworkingfolder\\downloadClient\\1.mkv");
    		NIOFileMessageMixStrategy.addFileInformationEntity(fileInformationEntity);
//    		ServiceCenterClientUtils.defaultRouteConsume = clientBootStrap.getServiceAccess();
//    		try {
//    			ServiceCenterClientUtils.registerServiceList(ServiceCenterClientUtils.defaultRouteConsume, centerServiceInformationEntity, serviceBootStrap.getServicePropertyEntity());
//    		}
//    		catch (Exception e) {
//    			
//    		}
    		
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
