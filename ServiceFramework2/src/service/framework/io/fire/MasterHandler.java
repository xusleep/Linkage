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
 * 在listener中，会接收服务的事件，并调用这里的processRequest方法，将事件加入到队列中
 * 事件队列，并且调用注册的handler，进行事件的处理
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
	public static void submitEventPool(ServiceEvent event) {
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
