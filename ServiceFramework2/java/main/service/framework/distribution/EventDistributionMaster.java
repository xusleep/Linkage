package service.framework.distribution;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

import service.framework.event.ServiceEvent;
import service.framework.handlers.Handler;

/**
 * ��listener�У�����շ�����¼��������������processRequest���������¼����뵽������
 * �¼����У����ҵ���ע���handler�������¼��Ĵ���
 * @author zhonxu
 *
 */
public class EventDistributionMaster extends Thread {
	public  BlockingQueue<ServiceEvent> pool = new LinkedBlockingQueue<ServiceEvent>();
	public final ExecutorService objExecutorService;
	private final List<Handler> eventHandlerList;
	
	public EventDistributionMaster(int taskThreadPootSize){
		this.objExecutorService = Executors.newFixedThreadPool(taskThreadPootSize);
		this.eventHandlerList = new LinkedList<Handler>();
	}
	
	
	public void registerHandler(Handler handler) {
		this.eventHandlerList.add(handler);
	}



	@Override
	public void run() {
		// TODO Auto-generated method stub
        while (true) {
        	
            try 
            {
            	final ServiceEvent event = pool.take();
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
	 * �����ͻ�����,�����û��������,�����Ѷ����е��߳̽��д���
	 */
	public void submitEventPool(ServiceEvent event) {
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
		this.objExecutorService.execute(new Runnable(){
			@Override
			public void run() {
            	try {
            		for(Handler handler : eventHandlerList)
            		{
            			handler.handleRequest(event);
            		}
				} catch (IOException e) {
					e.printStackTrace();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
    	});
	}
}