package service.framework.io.context;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

/**
 * <p>
 * Title: 客户端请求信息类
 * </p>
 * 
 * @author starboy
 * @version 1.0
 */

public class DefaultServiceContext implements ServiceContext {
	private SocketChannel sc;
	private byte[] dataInput = null;;
	Object obj;

	public DefaultServiceContext() {
	}

	public DefaultServiceContext(SocketChannel sc) {
		this.sc = sc;
	}

	public SocketChannel getSc() {
		return sc;
	}

	public void setSc(SocketChannel sc) {
		this.sc = sc;
	}

	public void attach(Object obj) {
		this.obj = obj;
	}

	public Object attachment() {
		return obj;
	}

	public byte[] getDataInput() {
		return dataInput;
	}

	public void setDataInput(byte[] dataInput) {
		this.dataInput = dataInput;
	}
}
