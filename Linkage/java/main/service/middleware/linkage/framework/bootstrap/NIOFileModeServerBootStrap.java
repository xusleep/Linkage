package service.middleware.linkage.framework.bootstrap;

import service.middleware.linkage.framework.handlers.DefaultEventDistributionMaster;
import service.middleware.linkage.framework.io.Server;
import service.middleware.linkage.framework.io.nio.NIOServer;
import service.middleware.linkage.framework.setting.reader.ServiceSettingPropertyReader;
import service.middleware.linkage.framework.setting.reader.ServiceSettingReader;

/**
 * this is boot strap used from the server side for file mode only 
 * transform the file 
 * @author zhonxu
 *
 */
public class NIOFileModeServerBootStrap extends AbstractBootStrap implements Runnable {
	private final Server server;
	private final ServiceSettingReader servicePropertyEntity;
	
	public NIOFileModeServerBootStrap(String propertyPath, int serviceTaskThreadPootSize) throws Exception{
		super(new DefaultEventDistributionMaster(serviceTaskThreadPootSize));
		// read the configuration from the properties
		this.servicePropertyEntity = new ServiceSettingPropertyReader(propertyPath);
		
		// this is the server, it will accept all of the connection & register the channel into the worker pool
		this.server = new NIOServer(servicePropertyEntity.getServiceAddress(),  servicePropertyEntity.getServicePort(),
				this.getWorkerPool());
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
