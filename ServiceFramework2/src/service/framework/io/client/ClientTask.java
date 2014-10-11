package service.framework.io.client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicLong;

import service.framework.io.context.DefaultServiceContext;
import service.framework.protocol.ShareingProtocolData;
import service.framework.provide.entity.RequestEntity;
import service.framework.provide.entity.ResponseEntity;
import service.framework.serialization.SerializeUtils;

public class ClientTask implements Callable {
    private final RequestEntity objRequestEntity;
	public static AtomicLong idCounter = new AtomicLong(0);
    //通道管理器  
    private Selector selector;  
    
    
	public ClientTask(String address, int port, RequestEntity objRequestData) throws IOException
	{
		this.objRequestEntity = objRequestData;
        // 获得一个Socket通道  
        SocketChannel channel = SocketChannel.open();  
        // 设置通道为非阻塞  
        channel.configureBlocking(false);  
        // 获得一个通道管理器  
        this.selector = Selector.open();  
          
        // 客户端连接服务器,其实方法执行并没有实现连接，需要在listen（）方法中调  
        //用channel.finishConnect();才能完成连接  
        channel.connect(new InetSocketAddress(address, port));  
        //将通道管理器和该通道绑定，并为该通道注册SelectionKey.OP_CONNECT事件。  
        channel.register(selector, SelectionKey.OP_CONNECT);  
	}
	
	private void writeMessage(SocketChannel channel) throws IOException{
		System.out.println("enter writeMessage ------------------------------------  ");
		String sendData = SerializeUtils.serializeRequest(this.objRequestEntity);
        byte[] data = sendData.getBytes(ShareingProtocolData.FRAMEWORK_IO_ENCODING);
        //在这里可以给服务端发送信息
        ByteBuffer buffer = ByteBuffer.allocate(data.length);
        buffer.put(data, 0, data.length);
        buffer.flip();
        while (buffer.hasRemaining()) {  
            channel.write(buffer);  
        } 
        System.out.println("exit writeMessage ------------------------------------  ");
	}

	@Override
	public Object call() {
		ResponseEntity result = null;
		try
		{
			boolean exitFlag = false;
			 // 轮询访问selector  
	        while (true) {  
	        	if(exitFlag || Thread.currentThread().interrupted())
	        		break;
	            selector.select();  
	            // 获得selector中选中的项的迭代器  
	            Iterator ite = this.selector.selectedKeys().iterator();  
	            while (ite.hasNext()) {  
	                SelectionKey key = (SelectionKey) ite.next();  
	                // 删除已选的key,以防重复处理  
	                ite.remove();  
	                // 连接事件发生  
	                if (key.isConnectable()) {  
	                    SocketChannel channel = (SocketChannel) key  
	                            .channel();  
	                    // 如果正在连接，则完成连接  
	                    if(channel.isConnectionPending()){  
	                        channel.finishConnect();  
	                    }  
	                    // 设置成非阻塞  
	                    channel.configureBlocking(false);  
	                    writeMessage(channel);
	                    //在这里可以给服务端发送信息哦  
	                    channel.shutdownOutput();
	                    //在和服务端连接成功之后，为了可以接收到服务端的信息，需要给通道设置读的权限。  
	                    DefaultServiceContext request = new DefaultServiceContext(channel);
	                    channel.register(this.selector, SelectionKey.OP_READ, request); 
	                    // 获得了可读的事件  
	                } else if (key.isReadable()) {
                        boolean succ = read(key); 
                        if(!succ)
                        {        	
                        	closeChanel((SocketChannel)key.channel());
                        	key.cancel();
                        	exitFlag = true;
                        	System.out.println("close connection count = " + idCounter.incrementAndGet());
                        }
                        else
                        {
	                        DefaultServiceContext request = (DefaultServiceContext)key.attachment();
	                        result = SerializeUtils.deserializeResponse(new String(request.getDataInput(), ShareingProtocolData.FRAMEWORK_IO_ENCODING));
                        } 
	                }  
	            }
	        }
		}
		catch(IOException ex){
			System.out.println("send error: " +ex.getMessage());
			ex.printStackTrace();
		}
		return result;
	}
	
	private void closeChanel(SocketChannel channel) throws IOException{
		if(channel!=null)
		{
			channel.socket().close();
			channel.close();
		}
	}
	 /**
     * 读取客户端发出请求数据
     * @param sc 套接通道
     */
    private static int BUFFER_SIZE = 1024;
    public static boolean readRequest(SocketChannel sc, DefaultServiceContext request) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);
        int off = 0;
        int r = 0;
        byte[] data = new byte[BUFFER_SIZE * 10];
        buffer.clear();
        r = sc.read(buffer);
        // 如果读取到的数据为-1长度说明，客户端试图关闭，那么我们也关闭
        if (r == -1) 
        {
        	return false;
        }
        while ( true ) {
            if ( (off + r) > data.length) {
                data = grow(data, BUFFER_SIZE * 10);
            }
            byte[] buf = buffer.array();
            System.arraycopy(buf, 0, data, off, r);
            off += r;
            buffer.clear();
            r = sc.read(buffer);
            //读到了末尾，退出
            if (r == -1) 
            {
            	break;
            }
        }
        byte[] req = new byte[off];
        System.arraycopy(data, 0, req, 0, off);
        request.setDataInput(req);
        return true;
    }
    
	 /**
     * 处理连接数据读取
     * @param key SelectionKey
     */
    public boolean read(SelectionKey key) {
        try {
            // 读取客户端数据
            SocketChannel sc = (SocketChannel) key.channel();
            DefaultServiceContext request = (DefaultServiceContext)key.attachment();
            return readRequest(sc, request);
        }
        catch (Exception e) {
        	e.printStackTrace();
        	return false;
        }
    }
    
    /**
     * 数组扩容
     * @param src byte[] 源数组数据
     * @param size int 扩容的增加量
     * @return byte[] 扩容后的数组
     */
    public static byte[] grow(byte[] src, int size) {
        byte[] tmp = new byte[src.length + size];
        System.arraycopy(src, 0, tmp, 0, src.length);
        return tmp;
    }
}
