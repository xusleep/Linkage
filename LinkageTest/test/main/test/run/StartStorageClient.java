package test.run;

import java.io.File;

import service.middleware.linkage.center.bootstrap.NIOCenterClientBootStrap;
import service.middleware.linkage.center.client.ServiceCenterClientUtils;
import service.middleware.linkage.center.serviceaccess.NIORouteServiceAccess;
import service.middleware.linkage.framework.bootstrap.NIOMessageModeServerBootStrap;
import service.middleware.linkage.framework.common.entity.ServiceInformationEntity;

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
		NIOCenterClientBootStrap clientBootStrap = new NIOCenterClientBootStrap("conf/client_client.properties", 5, centerServiceInformationEntity);
		clientBootStrap.run();
    	NIOMessageModeServerBootStrap serviceBootStrap = new NIOMessageModeServerBootStrap("conf/client_server.properties", 5);
    	serviceBootStrap.run();
    	NIORouteServiceAccess cb = clientBootStrap.getServiceAccess();
		ServiceInformationEntity clientServiceInformationEntity = new ServiceInformationEntity();
		clientServiceInformationEntity.setAddress(serviceBootStrap.getServicePropertyEntity().getServiceAddress());
		clientServiceInformationEntity.setPort(serviceBootStrap.getServicePropertyEntity().getServicePort());
		ServiceCenterClientUtils.registerClientInformation(cb, clientServiceInformationEntity, centerServiceInformationEntity);
//		for(int i = 1; i < 10; i++)
//		{
//			List<String> args1 = new LinkedList<String>();
//			args1.add(i + ".txt");
//			args1.add(HexUtils.fileToHexString("E:\\Storage\\transeformfile.txt"));
//			RequestResultEntity result = clientBootStrap.getServiceAccess().requestServicePerConnectSync("storage", args1);
//			System.out.println("result is : " + result.getResponseEntity().getResult());
//		}
//		List<String> args1 = new LinkedList<String>();
//		args1.add("1.jar");
//		args1.add(HexUtils.fileToHexString("E:\\OSGI\\cxf-dosgi-ri-samples-ds-interface-1.2.jar"));
//		RequestResultEntity result = clientBootStrap.getServiceAccess().requestServicePerConnectSync("storage", args1);
//		System.out.println("result is : " + result.getResponseEntity().getResult());
		ServiceInformationEntity serviceInformationEntity = new ServiceInformationEntity();
		serviceInformationEntity.setAddress("localhost");
		serviceInformationEntity.setPort(5003);
		clientBootStrap.getServiceAccess().getServiceAccessEngine().writeFile(new File("E:\\testfolder\\1.txt"), serviceInformationEntity, true);
		//clientBootStrap.getServiceAccess().getServiceAccessEngine().writeFile(new File("E:\\testfolder\\2.txt"), serviceInformationEntity, true);
		//clientBootStrap.shutdownImediate();
		//serviceBootStrap.shutdownImediate();
	}
}
