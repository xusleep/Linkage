package service.middleware.linkage.framework.io.nio.strategy.mixed.writer;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import org.apache.log4j.Logger;

import service.middleware.linkage.framework.common.ConvertUtils;
import service.middleware.linkage.framework.io.nio.strategy.mixed.packet.ContentEntity;
import service.middleware.linkage.framework.io.nio.strategy.mixed.packet.FileEntity;
import service.middleware.linkage.framework.io.nio.strategy.mixed.packet.MessageEntity;
import service.middleware.linkage.framework.io.nio.strategy.mixed.packet.PacketDataType;
import service.middleware.linkage.framework.io.nio.strategy.mixed.packet.PacketEntity;

public class DataTypeWriter extends WriterDecorator {
	private static Logger logger = Logger.getLogger(DataTypeWriter.class);
	private WriterInterface wrappedFileWriter;
	private WriterInterface wrappedMessageWriter;
	
	public DataTypeWriter(WriterInterface wrappedMessageWriter, WriterInterface wrappedFileWriter) {
		super(wrappedMessageWriter);
		this.wrappedFileWriter = wrappedFileWriter;
		this.wrappedMessageWriter = wrappedMessageWriter;
	}

	@Override
	public boolean write(SocketChannel sc, ContentEntity contentEntity)
			throws IOException {
		logger.debug("DataTypeWriter start.");
		PacketEntity packetEntity = (PacketEntity)contentEntity;
		ByteBuffer buffer = ByteBuffer.allocate(1);
		int idataType = packetEntity.getPacketDataType().ordinal();
		byte data = ConvertUtils.intToByte(idataType);
		buffer.put(data);
		buffer.flip();
		int writtenCount = 0;
		int totalCount = 1;
		while(writtenCount != totalCount)
		{
			writtenCount = writtenCount + sc.write(buffer);
			logger.debug("DataTypeWriter write bytes count : " + writtenCount);
		}
		logger.debug("DataTypeWriter end.");
		if(packetEntity.getPacketDataType() == PacketDataType.MESSAGE){
			return wrappedMessageWriter.write(sc, packetEntity.getContentEntity());
		}
		else if(packetEntity.getPacketDataType() == PacketDataType.FILE){
			FileEntity fileEntity = new FileEntity();
			fileEntity.setLength(contentEntity.getLength() - 1);
			return wrappedFileWriter.write(sc, packetEntity.getContentEntity());
		}
		return false;
	}
}
