package service.framework.io.handlers;

import static service.framework.io.fire.Fires.fireCommonEvent;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.context.ApplicationContext;

import service.framework.io.context.DefaultServiceContext;
import service.framework.io.context.ServiceContext;
import service.framework.io.event.ServiceEvent;
import service.framework.io.event.ServiceOnErrorEvent;
import service.framework.io.event.ServiceOnReadEvent;
import service.framework.io.event.ServiceOnWriteEvent;
import service.framework.io.event.ServiceStartedEvent;
import service.framework.io.event.ServiceStartingEvent;
import service.framework.io.fire.MasterHandler;
import service.framework.protocol.ShareingProtocolData;
import service.framework.provide.ProviderBean;
import service.framework.provide.entity.RequestEntity;
import service.framework.provide.entity.ResponseEntity;
import service.framework.serialization.SerializeUtils;

/**
 * ������Ĭ�ϵ��¼�����handler
 * 
 * @author zhonxu
 *
 */
public class ReadWriteHandler implements Handler {
	private AtomicInteger aint = new AtomicInteger(0);
	
	private final ApplicationContext applicationContext;
	
	public ReadWriteHandler(ApplicationContext applicationContext){
		this.applicationContext = applicationContext;
	}
	
	@Override
	public void handleRequest(ServiceContext context, ServiceEvent event) throws IOException {
		// TODO Auto-generated method stub
		if (event instanceof ServiceOnReadEvent) {
			System.out.println("enter read...... ");
			// TODO Auto-generated method stub
			ServiceOnReadEvent objServiceOnReadEvent = (ServiceOnReadEvent) event;
			SelectionKey key = objServiceOnReadEvent.getSelectionKey();
			DefaultServiceContext request = new DefaultServiceContext((SocketChannel)key.channel());
			key.attach(request);
			boolean succ = read(key);
			if(succ)
			{
				fireCommonEvent(new ServiceOnWriteEvent(key));
			}
			//�����ǿͻ��������ر������ӣ��������ҲҪ�ر�
			else
			{
				try {
					this.closeChanel((SocketChannel) key.channel());
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			System.out.println("exit read...... ");
		} else if (event instanceof ServiceOnWriteEvent) {
			System.out.println("enter write...... ");
			aint.incrementAndGet();
			ServiceOnWriteEvent objServiceOnReadEvent = (ServiceOnWriteEvent) event;
			SelectionKey key = objServiceOnReadEvent.getSelectionKey();
			try {
				DefaultServiceContext request = (DefaultServiceContext) key.attachment();
				String receiveData = new String(request.getDataInput(), ShareingProtocolData.FRAMEWORK_IO_ENCODING);
				RequestEntity objRequestEntity = SerializeUtils.deserializeRequest(receiveData);
				ProviderBean objProviderBean = (ProviderBean)applicationContext.getBean(objRequestEntity.getServiceName());
				ResponseEntity objResponseEntity = objProviderBean.prcessRequest(objRequestEntity);
				send(key, SerializeUtils.serializeResponse(objResponseEntity).getBytes(ShareingProtocolData.FRAMEWORK_IO_ENCODING));
				closeChanel((SocketChannel)key.channel());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			System.out.println("exit write...... ");
		}
		else if(event instanceof ServiceOnErrorEvent){
			System.out.println("�����˴���" + ((ServiceOnErrorEvent)event).getMsg());
		}
		else if(event instanceof ServiceStartingEvent){
            System.out.println("Server Service starting ...");
		}
		//����������������ע�ᵽ��������ȥ
		else if(event instanceof ServiceStartedEvent){
			//���ｫ���ע�ᵽ�������ĵĴ���
            System.out.println("Server Service started.");
		}
		System.out.println("���������Ϊ:" + aint.get());
		System.out.println("���г���Ϊ:" + MasterHandler.pool.size());
	}
	
	
	/**
     * ��ͻ���д����
     * @param data byte[]������Ӧ����
     */
    public void send(SelectionKey key, byte[] data) throws IOException {
    	SocketChannel sc = (SocketChannel) key.channel();
        ByteBuffer buffer = ByteBuffer.allocate(data.length);
        buffer.put(data, 0, data.length);
        buffer.flip();
        sc.write(buffer);
        sc.shutdownOutput();
    }

	 /**
     * ��ȡ�ͻ��˷�����������
     * @param sc �׽�ͨ��
     */
    private static int BUFFER_SIZE = 1024;
    public static boolean readRequest(SocketChannel sc, DefaultServiceContext request) throws IOException {
        System.out.println("enter readRequest ---------------------------------");
    	ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);
        int off = 0;
        int r = 0;
        byte[] data = new byte[BUFFER_SIZE * 10];
        buffer.clear();
        r = sc.read(buffer);
        // �����ȡ��������Ϊ-1����˵�����ͻ�����ͼ�رգ���ô����Ҳ�ر�
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
            //������ĩβ���˳�
            if (r == -1) 
            {
            	break;
            }
        }
        byte[] req = new byte[off];
        System.arraycopy(data, 0, req, 0, off);
        request.setDataInput(req);
        System.out.println("exit readRequest ---------------------------------");
        return true;
    }

    /**
     * �����������ݶ�ȡ
     * @param key SelectionKey
     * @throws IOException 
     */
    public boolean read(SelectionKey key) throws IOException {
        // ��ȡ�ͻ�������
        SocketChannel sc = (SocketChannel) key.channel();
        DefaultServiceContext request = (DefaultServiceContext)key.attachment();
        return readRequest(sc, request);
    }
    
    /**
     * ��������
     * @param src byte[] Դ��������
     * @param size int ���ݵ�������
     * @return byte[] ���ݺ������
     */
    public static byte[] grow(byte[] src, int size) {
        byte[] tmp = new byte[src.length + size];
        System.arraycopy(src, 0, tmp, 0, src.length);
        return tmp;
    }
    
    /**
     * �ر�ͨ������
     * @param channel
     * @throws IOException
     */
	private void closeChanel(SocketChannel channel) throws IOException{
		if(channel!=null)
		{
			channel.socket().close();
			channel.close();
		}
	}
}
