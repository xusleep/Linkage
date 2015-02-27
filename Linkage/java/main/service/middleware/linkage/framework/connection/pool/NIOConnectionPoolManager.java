package service.middleware.linkage.framework.connection.pool;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;

import service.middleware.linkage.framework.exception.ServiceException;

public class NIOConnectionPoolManager{
	private static Logger  logger = Logger.getLogger(NIOConnectionPoolManager.class);  
	private static int DEFAULT_POOL_SIZE = 32;
	private static ConcurrentHashMap<String, SocketChannel> socketPool = new ConcurrentHashMap(DEFAULT_POOL_SIZE);
	
	/**
	 * remove the connect from the cached pool
	 * @param connectionString
	 * @return
	 */
	public static SocketChannel removeConnectionFromPool(String connectionString){
		return socketPool.remove(connectionString);
	}
	
	/**
	 * connect to the server by the ip address and port
	 * @param ip
	 * @param port
	 * @return
	 * @throws IOException
	 */
	public static SocketChannel getIOConnection(String ip, int port) throws ServiceException{
		return getIOConnection(ip + ":" + port);
	}
	
	/**
	 * 
	 * @param connectionString 127.0.0.1:8080
	 * @return
	 */
	public static SocketChannel getIOConnection(String connectionString) throws ServiceException{
		String[] params = connectionString.split(":");
		if(params.length < 2){
			throw new ServiceException(new Exception("the passed parameter is not right, it should be 127.0.0.1:8080 likwise"), 
					"the passed parameter is not right, it should be 127.0.0.1:8080 likwise");
		}
		SocketChannel cachedChannel = socketPool.get(connectionString);
		if(cachedChannel != null && cachedChannel.isConnected() && cachedChannel.isOpen()){
			logger.debug("get the connection from the cached pool. connection string: " + connectionString);
			return cachedChannel;
		}
		else
		{
			removeConnectionFromPool(connectionString);
		}
		String ip = params[0];
		int port = Integer.parseInt(params[1]);
		try {
			synchronized(socketPool){
				cachedChannel = socketPool.get(connectionString);
				if(cachedChannel != null && cachedChannel.isConnected() && cachedChannel.isOpen()){
					logger.debug("get the connection from the cached pool. connection string: " + connectionString);
					return cachedChannel;
				}
				else
				{
					removeConnectionFromPool(connectionString);
				}
				SocketChannel channel = createNIOConnection(ip, port);
				logger.debug("created the connection and put it into the cached pool. connection string: " + connectionString);
				socketPool.put(connectionString, channel);
				return channel;
			}
		} catch (IOException e) {
			throw new ServiceException(e, e.getMessage());
		}
	}
	

	/**
	 * create a new NIO connection to the ip with port
	 * @param ip
	 * @param port
	 * @return
	 * @throws IOException
	 */
	private static SocketChannel createNIOConnection(String ip, int port) throws IOException{
		// get a Socket channel
        SocketChannel channel = SocketChannel.open();  
        // connect
        channel.connect(new InetSocketAddress(ip, port));
        // finish the connect
        if(channel.isConnectionPending()){  
            channel.finishConnect();  
        } 
        return channel;
	}
}
