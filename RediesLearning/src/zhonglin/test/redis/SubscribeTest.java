package zhonglin.test.redis;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.util.Pool;

public class SubscribeTest {

	public static Jedis jedis;
	
	static{
		JedisPoolConfig cfg = new JedisPoolConfig();
		cfg.setMaxActive(10);
		cfg.setMaxIdle(5);
		Pool<Jedis> jedisPool = new JedisPool(cfg, "127.0.0.1", new Integer(6379));	
		 jedis = jedisPool.getResource();
	}
	
	public static void main(String[] args){
		jedis.subscribe(new SubscribeEntity(), new String[]{"orderchannel1"});
	}
}
