package service.framework.event;

/**
 * this event will be triggered when there is an exception happen
 * @author zhonxu
 *
 */
public class ServiceOnErrorEvent implements ServiceEvent {
	
	private String msg;
	
	public ServiceOnErrorEvent(String msg){
		this.msg = msg;
	}

	public String getMsg() {
		return msg;
	}
	
	
}
