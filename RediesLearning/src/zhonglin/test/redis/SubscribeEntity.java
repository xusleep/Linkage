package zhonglin.test.redis;

import redis.clients.jedis.JedisPubSub;

public class SubscribeEntity extends JedisPubSub {

	@Override
	public void onMessage(String arg0, String arg1) {
		// TODO Auto-generated method stub
		System.out.println(arg1);
	}

	@Override
	public void onPMessage(String arg0, String arg1, String arg2) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onPSubscribe(String arg0, int arg1) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onPUnsubscribe(String arg0, int arg1) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onSubscribe(String arg0, int arg1) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onUnsubscribe(String arg0, int arg1) {
		// TODO Auto-generated method stub

	}

}
