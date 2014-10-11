package service.framework.io.handlers;

import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.context.ApplicationContext;

import service.framework.io.client.comsume.ConsumerBean;
import service.framework.io.context.DefaultServiceContext;
import service.framework.io.context.ServiceContext;
import service.framework.io.event.ServiceEvent;
import service.framework.io.event.ServiceStartedEvent;
import service.framework.provide.ProviderBean;
import service.framework.serialization.SerializeUtils;
import servicecenter.service.ServiceInformation;

/***
 * ���handlerֻ������÷������ĵ�ʱ��ʹ��
 * ����������ʱ�򣬻ᷢ��һ��ServiceStartedEvent��������ܵ����Ժ�
 * ���д���������������ģ����͵�ǰ����ķ����б���Ϣ
 * @author zhonxu
 *
 */
public class ServiceRegisterHandler implements Handler {
	private AtomicInteger aint = new AtomicInteger(0);
	
	private final ApplicationContext applicationContext;
	
	public ServiceRegisterHandler(ApplicationContext applicationContext){
		this.applicationContext = applicationContext;
	}
	
	@Override
	public void handleRequest(ServiceContext context, ServiceEvent event) throws IOException {
		// TODO Auto-generated method stub
		if (event instanceof ServiceStartedEvent) {
			// TODO Auto-generated method stub
			//ִ��
			ServiceStartedEvent objServiceOnReadEvent = (ServiceStartedEvent) event;
			ServiceInformation serviceInformation = (ServiceInformation)applicationContext.getBean("serviceInformation");
			List<ServiceInformation> serviceInformationList = new LinkedList<ServiceInformation>();
			//��ȡ���з��񣬽�����ע�ᵽע������
			Map serviceList = applicationContext.getBeansOfType(ProviderBean.class);
			Iterator keys = serviceList.keySet().iterator();
			while(keys.hasNext()){
				String beanName = (String) keys.next();
				ProviderBean bean = (ProviderBean) serviceList.get(beanName);
				String interfaceName = bean.getInterfaceName();
				try {
					Class interfaceclass = Class.forName(interfaceName);
					Method[] methods = interfaceclass.getMethods();
					for(int i = 0; i < methods.length; i++){
						ServiceInformation subServiceInformation = new ServiceInformation();
						subServiceInformation.setAddress(serviceInformation.getAddress());
						subServiceInformation.setPort(serviceInformation.getPort());
						subServiceInformation.setServiceMethod(methods[i].getName());
						subServiceInformation.setServiceName(beanName);
						subServiceInformation.setServiceVersion(bean.getVersion());
						serviceInformationList.add(subServiceInformation);
						System.out.println("service name : " + beanName + " method name : " + methods[i].getName());
					}
				} catch (ClassNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
			String strServiceInformation = SerializeUtils.serializeServiceInformationList(serviceInformationList);
			ConsumerBean objConsumerBean = (ConsumerBean)applicationContext.getBean("linkToServiceCenter");
			List<String> args = new LinkedList<String>();
			args.add(strServiceInformation);
			try {
				objConsumerBean.prcessRequest(args);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ExecutionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
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
    
	private void closeChanel(SocketChannel channel) throws IOException{
		if(channel!=null)
		{
			channel.socket().close();
			channel.close();
		}
	}
}
