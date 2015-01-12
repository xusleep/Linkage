package service.middleware.linkage.framework.io.nio.strategy.mixed.writer;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import org.apache.log4j.Logger;

import service.middleware.linkage.framework.io.nio.strategy.mixed.packet.ContentEntity;
import service.middleware.linkage.framework.io.nio.strategy.mixed.packet.PacketEntity;
import service.middleware.linkage.framework.utils.ConvertUtils;

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
