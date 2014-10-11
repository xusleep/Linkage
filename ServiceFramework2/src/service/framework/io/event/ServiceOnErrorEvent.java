package service.framework.io.event;

public class ServiceOnErrorEvent implements ServiceEvent {
	
	private String msg;
	
	public ServiceOnErrorEvent(String msg){
		this.msg = msg;
	}

	public String getMsg() {
		return msg;
	}
	
	
}
