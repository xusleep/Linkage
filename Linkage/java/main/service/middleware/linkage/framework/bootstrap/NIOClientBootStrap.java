package service.middleware.linkage.framework.bootstrap;

import java.io.IOException;

import service.middleware.linkage.framework.handlers.MessageClientReadWriteHandler;
import service.middleware.linkage.framework.io.client.Client;
import service.middleware.linkage.framework.io.client.DefaultClient;
import service.middleware.linkage.framework.serviceaccess.NIOServiceAccess;
import service.middleware.linkage.framework.serviceaccess.ServiceAccess;
import service.middleware.linkage.framework.setting.reader.ClientSettingPropertyReader;
import service.middleware.linkage.framework.setting.reader.ClientSettingReader;

/**
 * client side boot strap
 * it will init a worker pool and a distribution master
 * @author zhonxu
 *
 */
public class NIOClientBootStrap extends AbstractBootStrap implements Runnable {
	private final Client client;
	private final ServiceAccess serviceAccess;
	
	/**
	 * 
	 * @param propertyPath the property configured for the client
	 * @param clientTaskThreadPootSize the client 
	 */
	public NIOClientBootStrap(String propertyPath, int clientTaskThreadPootSize){
		super(clientTaskThreadPootSize);
		// read the configuration from the properties
		ClientSettingReader objServicePropertyEntity = null;
		try {
			objServicePropertyEntity = new ClientSettingPropertyReader(propertyPath);
		} catch (IOException e) {
			e.printStackTrace();
		}
		// this is a client, in this client it will be a gather place where we will start the worker pool & task handler 
		this.client = new DefaultClient(this.getEventDistributionHandler(), this.getWorkerPool());
		this.serviceAccess = new NIOServiceAccess(objServicePropertyEntity, this.getWorkerPool());
		this.getEventDistributionHandler().registerHandler(new MessageClientReadWriteHandler(this.getServiceAccess()));
	}
	
	/**
	 * the user can use this cosumer bean to request service
	 * from the client
	 * @return
	 */
	public ServiceAccess getServiceAccess() {
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
