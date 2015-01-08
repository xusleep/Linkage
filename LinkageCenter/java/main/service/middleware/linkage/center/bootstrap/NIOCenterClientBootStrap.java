package service.middleware.linkage.center.bootstrap;

import java.io.IOException;

import service.middleware.linkage.center.serviceaccess.NIORouteServiceAccess;
import service.middleware.linkage.framework.bootstrap.AbstractBootStrap;
import service.middleware.linkage.framework.common.entity.ServiceInformationEntity;
import service.middleware.linkage.framework.handlers.NIOMessageAccessClientHandler;
import service.middleware.linkage.framework.handlers.NIOMessageEventDistributionMaster;
import service.middleware.linkage.framework.handlers.NIOSinkHandler;
import service.middleware.linkage.framework.io.client.Client;
import service.middleware.linkage.framework.io.client.DefaultClient;
import service.middleware.linkage.framework.setting.reader.ClientSettingPropertyReader;
import service.middleware.linkage.framework.setting.reader.ClientSettingReader;

/**
 * client side boot strap
 * it will init a worker pool and a distribution master
 * @author zhonxu
 *
 */
public class NIOCenterClientBootStrap extends AbstractBootStrap implements Runnable {
	private final Client client;
	private final NIORouteServiceAccess serviceAccess;
	
	/**
	 * 
	 * @param propertyPath the property configured for the client
	 * @param clientTaskThreadPootSize the client 
	 */
	public NIOCenterClientBootStrap(String propertyPath, int clientTaskThreadPootSize, ServiceInformationEntity centerServiceInformationEntity){
		super(new NIOMessageEventDistributionMaster(clientTaskThreadPootSize));
		// read the configuration from the properties
		ClientSettingReader objServicePropertyEntity = null;
		try {
			objServicePropertyEntity = new ClientSettingPropertyReader(propertyPath);
		} catch (IOException e) {
			e.printStackTrace();
		}
		// this is a client, in this client it will be a gather place where we will start the worker pool & task handler 
		this.client = new DefaultClient(this.getWorkerPool());
		this.serviceAccess = new NIORouteServiceAccess(objServicePropertyEntity, this.getWorkerPool(), centerServiceInformationEntity);
		this.getWorkerPool().getEventDistributionHandler().addHandler(new NIOSinkHandler());
		this.getWorkerPool().getEventDistributionHandler().addHandler(new NIOMessageAccessClientHandler(this.getServiceAccess()));
	}
	
	/**
	 * the user can use this cosumer bean to request service
	 * from the client
	 * @return
	 */
	public NIORouteServiceAccess getServiceAccess() {
		return serviceAccess;
	}


	@Override
	public void run() {
		new Thread(client).start();
	}
	
	/**
	 * shutdown 
	 */
	public void shutdown()
	{
		client.shutdown();
	}
	/**
	 * shutdown imediate
	 */
	public void shutdownImediate()
	{
		client.shutdownImediate();
	}
}
