package service.framework.io.fire;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

import service.framework.io.event.ServiceEvent;
import service.framework.io.handlers.Handler;

/**
 * 在listener中，会接收服务的事件，并调用这里的processRequest方法，将事件加入到队列中
 * 事件队列，并且调用注册的handler，进行事件的处理
 * @author zhonxu
 *
 */
public class MasterHandler extends Thread {
	public  BlockingQueue<ServiceEvent> pool = new LinkedBlockingQueue<ServiceEvent>();
	public final ExecutorService objExecutorService;
	private final List<Handler> eventHandlerList;
	
	public MasterHandler(int taskThreadPootSize){
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
            	// 将事件处理任务交由线程池执行，处理逻辑独立处理在consumer里面完成
            	handleEvent(event);
            }
            catch (Exception e) {
            	e.printStackTrace();
                continue;
            }
        }
	}

	/**
	 * 处理客户请求,管理用户的联结池,并唤醒队列中的线程进行处理
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
	 * 注意此方法消费的队列，开始
	 * @param event
	 */
	public void handleEvent(final ServiceEvent event){
		this.objExecutorService.execute(new Runnable(){
			@Override
			public void run() {
            	try {
            		for(Handler handler : eventHandlerList)
            		{
            			handler.handleRequest(null, event);
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
