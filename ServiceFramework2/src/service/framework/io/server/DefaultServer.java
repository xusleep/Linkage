package service.framework.io.server;

import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

import service.framework.io.event.ServiceStartedEvent;
import service.framework.io.event.ServiceStartingEvent;
import service.framework.io.fire.MasterHandler;
import servicecenter.service.ServiceInformation;

/**
 * <p>
 * Title: ���ط����߳�
 * ��Ҫ���ڣ�������ͻ��˵�����
 * </p>
 * 
 * @author starboy
 * @version 1.0
 */

public class DefaultServer implements Server {
	private static Queue<SelectionKey> wpool = new ConcurrentLinkedQueue<SelectionKey>(); // ��Ӧ��
	private final Selector selector;
	private final ServerSocketChannel sschannel;
	private final InetSocketAddress address;
	private final WorkerPool workerPool;
	private final MasterHandler objMasterHandler;
	
	public DefaultServer(String strAddress, int port, MasterHandler objMasterHandler, WorkerPool workerPool) throws Exception {
		this.objMasterHandler = objMasterHandler;
		objMasterHandler.submitEventPool(new ServiceStartingEvent());
		// ���������������׽�
		selector = Selector.open();
		sschannel = ServerSocketChannel.open();
		sschannel.configureBlocking(false);
		System.out.println("Listening to " + strAddress + " port : " + port);
		address = new InetSocketAddress(strAddress, port);
		ServerSocket ss = sschannel.socket();
		ss.bind(address);
		sschannel.register(selector, SelectionKey.OP_ACCEPT);
		this.workerPool = workerPool;
		objMasterHandler.submitEventPool(new ServiceStartedEvent());
	}

	public WorkerPool getWorkerPool() {
		return workerPool;
	}
	
	

	public MasterHandler getMasterHandler() {
		return objMasterHandler;
	}

	public void run() {
		objMasterHandler.start();
		workerPool.start();
		workerPool.waitReady();
		// ����
		while (true) {
			try {
				int num = 0;
				num = selector.select();
				if (num > 0) {
					Set selectedKeys = selector.selectedKeys();
					Iterator it = selectedKeys.iterator();
					while (it.hasNext()) {
						SelectionKey key = (SelectionKey) it.next();
						it.remove();
						// ����IO�¼�
						if (key.isAcceptable()) {
							// Accept the new connection
							ServerSocketChannel ssc = (ServerSocketChannel) key.channel();
							SocketChannel sc = ssc.accept();
							sc.configureBlocking(false);
							// �����յ���channel���ŵ������̳߳���
							this.getWorkerPool().register(sc);
						} 
					}
				} 
			} catch (Exception e) {
				continue;
			}
		}
	}

	@Override
	public void waitReady() {
		// TODO Auto-generated method stub
		workerPool.waitReady();
	}
}
