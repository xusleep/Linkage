package test.run;

import java.util.LinkedList;
import java.util.List;

import service.middleware.linkage.center.bootstrap.NIOCenterClientBootStrap;
import service.middleware.linkage.center.serviceaccess.NIORouteServiceAccess;
import service.middleware.linkage.framework.bootstrap.NIOMessageModeServerBootStrap;
import service.middleware.linkage.framework.io.nio.strategy.mixed.NIOMixedStrategy;
import service.middleware.linkage.framework.io.nio.strategy.mixed.packet.FileInformationEntity;
import service.middleware.linkage.framework.serviceaccess.entity.RequestResultEntity;
import service.middleware.linkage.framework.serviceaccess.entity.ServiceInformationEntity;

/**
 * 
 * @author zhonxu
 *
 */
public class StartStorageClient {

	public static void main(String[] args) throws Exception {
    	ServiceInformationEntity centerServiceInformationEntity = new ServiceInformationEntity();
    	centerServiceInformationEntity.setAddress("localhost");
    	centerServiceInformationEntity.setPort(5002);
		final NIOCenterClientBootStrap clientBootStrap = new NIOCenterClientBootStrap("conf/client_client.properties", 5, centerServiceInformationEntity);
		clientBootStrap.run();
    	NIOMessageModeServerBootStrap serviceBootStrap = new NIOMessageModeServerBootStrap("conf/client_server.properties", 5);
    	serviceBootStrap.run();
    	NIORouteServiceAccess cb = clientBootStrap.getServiceAccess();
		List<String> args1 = new LinkedList<String>();
		args1.add("1.jar");
		RequestResultEntity result = clientBootStrap.getServiceAccess().requestServicePerConnectSync("storage", args1);
		System.out.println("result is : " + result.getResponseEntity().getResult());
		//clientBootStrap.shutdownImediate();
		//serviceBootStrap.shutdownImediate();
	}
}
