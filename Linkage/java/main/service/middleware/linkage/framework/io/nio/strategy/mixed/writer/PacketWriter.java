package service.middleware.linkage.framework.io.nio.strategy.mixed.writer;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import org.apache.log4j.Logger;

import service.middleware.linkage.framework.io.nio.strategy.mixed.packet.ContentEntity;
import service.middleware.linkage.framework.io.nio.strategy.mixed.packet.PacketEntity;
import service.middleware.linkage.framework.utils.ConvertUtils;

/**
 * when writting a packet data from the channel
 * we are gona to use the decorator pattern to write.
 * the packet writer is a decorator which will wapper 
 * the data type writer, the data type writer will
 * wapper the file writer and message writer
 * we write from the packet writer, write the size of the packet
 * then goto the data type writer, write the datatype of the packet
 * if the data type is message , we goto the message writer
 * else we goto the file writer.
 * @author zhonxu
 *
 */
public class PacketWriter extends WriterDecorator {
	private static Logger logger = Logger.getLogger(PacketWriter.class);
	public PacketWriter(WriterInterface wrappedWriter) {
		super(wrappedWriter);
		// TODO Auto-generated constructor stub
	}

	@Override
	public boolean write(SocketChannel sc, ContentEntity contentEntity)
			throws IOException {
		logger.debug("PacketWriter start.");
		PacketEntity packetEntity = (PacketEntity)contentEntity;
		byte[] data = ConvertUtils.longToBytes(packetEntity.getLength());
		ByteBuffer buffer = ByteBuffer.allocate(8);
		buffer.put(data, 0, 8);
		buffer.flip();
		int writtenCount = 0;
		int totalCount = 8;
		while(writtenCount !=totalCount)
		{
			writtenCount = writtenCount + sc.write(buffer);
			logger.debug("PacketWriter write bytes count : " + writtenCount);
		}
		logger.debug("PacketWriter end.");
		return this.getWrappedWriter().write(sc, contentEntity);
	}

}
