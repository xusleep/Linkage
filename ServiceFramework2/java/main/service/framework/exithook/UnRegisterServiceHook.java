//package service.framework.exithook;
//
//import java.util.LinkedList;
//import java.util.List;
//
//import service.framework.common.ShareingData;
//import service.framework.common.entity.RequestResultEntity;
//import service.framework.comsume.ConsumerBean;
//
//public class UnRegisterServiceHook extends Thread{
//
//	private ConsumerBean consumerBean;
//	public UnRegisterServiceHook(ConsumerBean consumerBean){
//		this.consumerBean = consumerBean;
//	}
//	
//	@Override
//	public void run() {
//		// TODO Auto-generated method stub
//		List<String> list = new LinkedList<String>();
//		list.add(ShareingData.SERVICE_CENTER);
//		RequestResultEntity objRequestResultEntity = this.consumerBean.prcessRequest(ShareingData.SERVICE_CENTER_UNREGISTER_ID, list);
//		if(objRequestResultEntity.isException())
//		{
//			objRequestResultEntity.getException().printStackTrace();
//		}
//	}
//}
