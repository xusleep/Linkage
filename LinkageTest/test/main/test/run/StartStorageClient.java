package test.run;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.util.LinkedList;
import java.util.List;

import clould.storage.service.utils.HexUtils;
import service.middleware.linkage.center.bootstrap.NIOCenterClientBootStrap;
import service.middleware.linkage.center.client.ServiceCenterClientUtils;
import service.middleware.linkage.center.comsume.NIORouteServiceAccess;
import service.middleware.linkage.framework.bootstrap.NIOServerBootStrap;
import service.middleware.linkage.framework.common.entity.RequestResultEntity;
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
    	NIOServerBootStrap serviceBootStrap = new NIOServerBootStrap("conf/client_server.properties", 5);
    	serviceBootStrap.run();
    	NIORouteServiceAccess cb = clientBootStrap.getServiceAccess();
		ServiceInformationEntity clientServiceInformationEntity = new ServiceInformationEntity();
		clientServiceInformationEntity.setAddress(serviceBootStrap.getServicePropertyEntity().getServiceAddress());
		clientServiceInformationEntity.setPort(serviceBootStrap.getServicePropertyEntity().getServicePort());
		ServiceCenterClientUtils.registerClientInformation(cb, clientServiceInformationEntity, centerServiceInformationEntity);
		for(int i = 1; i < 10; i++)
		{
			List<String> args1 = new LinkedList<String>();
			args1.add(i + ".txt");
			args1.add(HexUtils.fileToHexString("E:\\Storage\\transeformfile.txt"));
			RequestResultEntity result = clientBootStrap.getServiceAccess().requestServicePerConnectSync("storage", args1);
			System.out.println("result is : " + result.getResponseEntity().getResult());
		}
		List<String> args1 = new LinkedList<String>();
		args1.add("1.jar");
		args1.add(HexUtils.fileToHexString("E:\\OSGI\\cxf-dosgi-ri-samples-ds-interface-1.2.jar"));
		RequestResultEntity result = clientBootStrap.getServiceAccess().requestServicePerConnectSync("storage", args1);
		System.out.println("result is : " + result.getResponseEntity().getResult());
		clientBootStrap.shutdownImediate();
		serviceBootStrap.shutdownImediate();
	}
}
