package service.framework.io.server;

import static service.framework.io.fire.Fires.fireCommonEvent;

import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import service.framework.io.event.ServiceOnClosedEvent;
import service.framework.io.event.ServiceOnReadEvent;
/**
 * <p>
 * Title: ���ط����߳�
 * ��Ҫ���ڣ�������ͻ��˵�����
 * </p>
 * 
 * @author starboy
 * @version 1.0
 */

public class DefaultWorker implements Worker {
	// ע��أ����д�ע�������channel�����ŵ�����
	private Queue<SocketChannel> wpool = new ConcurrentLinkedQueue<SocketChannel>(); 
	private final Selector selector;

	public DefaultWorker() throws Exception {
		// ���������������׽�
		selector = Selector.open();
	}

	public void run() {
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
						if (key.isReadable()) {
							fireCommonEvent(new ServiceOnReadEvent(key, this));
							key.cancel();
						} 
						else
						{
							addReadWriterRegister();
						}
					}
				} 
				else
				{
					addReadWriterRegister();
				}
			} catch (Exception e) {
				continue;
			}
		}
	}

	/**
	 * ����µ�ͨ��ע��
	 */
	public void addReadWriterRegister() {
		while (!wpool.isEmpty()) {
			SocketChannel schannel = wpool.poll();
			try {
				schannel.register(selector, SelectionKey.OP_READ);
			} catch (Exception e) {
				try {
					schannel.finishConnect();
					schannel.close();
					schannel.socket().close();
					fireCommonEvent(new ServiceOnClosedEvent());
				} 
				catch (Exception e1) {
				}
			}
		}
	}
	
	/**
	 * �ύ�µĿͻ���д�������������̵߳Ļ�Ӧ����
	 */
	public void submitOpeRegister(SocketChannel schannel) {
		System.out.println("submitOpeRegister ...");
		wpool.offer(schannel);
		selector.wakeup(); // ���selector������״̬���Ա�ע���µ�ͨ��
	}
}
