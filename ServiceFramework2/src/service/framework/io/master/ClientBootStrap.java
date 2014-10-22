package service.framework.io.master;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;
import java.util.LinkedList;
import java.util.List;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import service.framework.io.event.ServiceOnMessageWriteEvent;
import service.framework.io.fire.MasterHandler;
import service.framework.io.handlers.ClientReadWriteHandler;
import service.framework.io.handlers.Handler;
import service.framework.io.server.Client;
import service.framework.io.server.DefaultWorkerPool;
import service.framework.io.server.WorkingChannel;
import service.framework.monitor.MonitorThread;

public class ClientBootStrap {
	private final Client client;
	private static ClientBootStrap instance = new ClientBootStrap();
	private final ApplicationContext applicationContext;
	
	private ClientBootStrap(){
		this.applicationContext = new ClassPathXmlApplicationContext("ClientServiceConfig.xml");
		this.client = (Client)applicationContext.getBean("defaultClient");
	}
	
	public static ClientBootStrap getInstance(){
		return instance;
	}
	
	public Client getClient() {
		return client;
	}

	
	public ApplicationContext getApplicationContext() {
		return applicationContext;
	}

	public void start() throws IOException {
		new MonitorThread(client.getMasterHandler()).start();
		client.getMasterHandler().registerHandler(new ClientReadWriteHandler());
		new Thread(client).start();
	}
	
	public static void main(String[] args) throws IOException {
		ClientBootStrap.getInstance().start();
	}
}
