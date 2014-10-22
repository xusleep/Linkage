package service.framework.io.master;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import service.framework.io.handlers.ClientReadWriteHandler;
import service.framework.io.handlers.ReadWriteHandler;
import service.framework.io.handlers.ServiceRegisterHandler;
import service.framework.io.server.Client;
import service.framework.io.server.Server;
import service.framework.monitor.MonitorThread;

public class ServiceBootStrap {
	private Client client;
	private static ServiceBootStrap instance = new ServiceBootStrap();
	private ApplicationContext applicationContext;
	private Server server;
	
	private ServiceBootStrap(){
        this.applicationContext = new ClassPathXmlApplicationContext("ServerServiceConfig.xml");
        this.server = (Server)this.applicationContext.getBean("defaultServer");
        this.client = (Client)this.applicationContext.getBean("defaultClient");
	}
	
	public static ServiceBootStrap getInstance() {
		return instance;
	}
	
	public void start(){
		server.getMasterHandler().registerHandler(new ReadWriteHandler(applicationContext));
		//server.getMasterHandler().registerHandler(new ServiceRegisterHandler(applicationContext));
		new Thread(server).start();
		new MonitorThread(server.getMasterHandler()).start();
		client.getMasterHandler().registerHandler(new ClientReadWriteHandler());
		new Thread(client).start();
	}
	
	public Client getClient() {
		return client;
	}

	public void stop(){
		
	}
	
    public static void main(String[] args) {
        try {
        	ServiceBootStrap.getInstance().start();
        }
        catch (Exception e) {
            System.out.println("Server error: " + e.getMessage());
            System.exit(-1);
        }
    }
}
