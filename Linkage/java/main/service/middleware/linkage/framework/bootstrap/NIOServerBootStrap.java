package service.middleware.linkage.framework.bootstrap;

import service.middleware.linkage.framework.handlers.MessageServiceReadWriteHandler;
import service.middleware.linkage.framework.io.server.NIOServer;
import service.middleware.linkage.framework.io.server.Server;
import service.middleware.linkage.framework.provider.DefaultServiceProvider;
import service.middleware.linkage.framework.provider.ServiceProvider;
import service.middleware.linkage.framework.setting.reader.ServiceSettingPropertyReader;
import service.middleware.linkage.framework.setting.reader.ServiceSettingReader;

/**
 * this is boot strap used from the server side
 * @author zhonxu
 *
 */
public class NIOServerBootStrap extends AbstractBootStrap implements Runnable {
	private final Server server;
	private final ServiceProvider serviceProvider;
	private final ServiceSettingReader servicePropertyEntity;
	
	public NIOServerBootStrap(String propertyPath, int serviceTaskThreadPootSize) throws Exception{
		super(serviceTaskThreadPootSize);
		// read the configuration from the properties
		this.servicePropertyEntity = new ServiceSettingPropertyReader(propertyPath);
		// this is a provider which provides the service point access from the io layer
		// in this provider, all of the service information will load into the bean
		// when there is a request, the provider will find the service, init it & execute the service
		this.serviceProvider = new DefaultServiceProvider(servicePropertyEntity);
		// this is a handler for the service, which will read the requestion information & call the provider 
		// to handle further
		this.getEventDistributionHandler().registerHandler(new MessageServiceReadWriteHandler(serviceProvider));
		
		// this is the server, it will accept all of the connection & register the channel into the worker pool
		this.server = new NIOServer(servicePropertyEntity.getServiceAddress(), servicePropertyEntity.getServicePort(),
				this.getEventDistributionHandler(), this.getWorkerPool());
	}

	public ServiceProvider getServiceProvider() {
		return serviceProvider;
	}

	public ServiceSettingReader getServicePropertyEntity() {
		return servicePropertyEntity;
	}

	@Override
	public void run() {
		new Thread(server).start();
	}
	
	/**
	 * shutdown 
	 */
	public void shutdown()
	{
		server.shutdown();
	}
	
	/**
	 * shutdown imediate
	 */
	public void shutdownImediate()
	{
		server.shutdownImediate();
	}
}
