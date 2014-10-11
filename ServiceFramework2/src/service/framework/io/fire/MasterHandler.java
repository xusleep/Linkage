package service.framework.io.fire;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

import service.framework.io.event.ServiceEvent;
import service.framework.io.handlers.Handler;

/**
 * ��listener�У�����շ�����¼��������������processRequest���������¼����뵽������
 * �¼����У����ҵ���ע���handler�������¼��Ĵ���
 * @author zhonxu
 *
 */
public class MasterHandler extends Thread {
	public static BlockingQueue<ServiceEvent> pool = new LinkedBlockingQueue<ServiceEvent>();
	private final ExecutorService objExecutorService;
	private final List<Handler> eventHandlerList;
	
	public MasterHandler(int taskThreadPootSize, List<Handler> eventHandlerList){
		this.objExecutorService = Executors.newFixedThreadPool(taskThreadPootSize);
		this.eventHandlerList = eventHandlerList;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
        while (true) {
            try 
            {
            	final ServiceEvent event = pool.take();
	            System.out.println("pool.take() ... ");	
            	// ���¼������������̳߳�ִ�У������߼�����������consumer�������
            	handleEvent(event);
            }
            catch (Exception e) {
            	e.printStackTrace();
                continue;
            }
        }
	}

	/**
	 * ����ͻ�����,�����û��������,�����Ѷ����е��߳̽��д���
	 */
	public static void submitEventPool(ServiceEvent event) {
		try {
			pool.put(event);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * ע��˷������ѵĶ��У���ʼ
	 * @param event
	 */
	public void handleEvent(final ServiceEvent event){
//		this.objExecutorService.execute(new Runnable(){
//			@Override
//			public void run() {
            	try {
            		System.out.println("this.objExecutorService.execute() ...");
            		for(Handler handler : eventHandlerList)
            		{
            			System.out.println("handler.handleRequest(null, event); ... ");
            			handler.handleRequest(null, event);
            		}
				} catch (IOException e) {
					e.printStackTrace();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
//			}
//    	});
	}
}
