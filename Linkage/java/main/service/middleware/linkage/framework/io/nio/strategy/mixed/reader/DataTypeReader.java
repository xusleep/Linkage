package service.middleware.linkage.framework.io.nio.strategy.mixed.reader;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import org.apache.log4j.Logger;

import service.middleware.linkage.framework.io.nio.strategy.mixed.packet.ContentEntity;
import service.middleware.linkage.framework.io.nio.strategy.mixed.packet.FileEntity;
import service.middleware.linkage.framework.io.nio.strategy.mixed.packet.MessageEntity;
import service.middleware.linkage.framework.io.nio.strategy.mixed.packet.PacketDataType;
import service.middleware.linkage.framework.io.nio.strategy.mixed.packet.PacketEntity;
import service.middleware.linkage.framework.utils.ConvertUtils;

/**
 * read the data type, if is message packet
 * goto the message reader
 * else goto the file reader
 * @author zhonxu
 *
 */
public class DataTypeReader extends ReaderDecorator {
	private ReaderInterface wrappedFileReader;
	private ReaderInterface wrappedMessageReader;
	private static Logger logger = Logger.getLogger(DataTypeReader.class);
	
	public DataTypeReader(ReaderInterface wrappedMessageReader, ReaderInterface wrappedFileReader) {
		super(wrappedMessageReader);
		this.wrappedFileReader = wrappedFileReader;
		this.wrappedMessageReader = wrappedMessageReader;
	}

	@Override
	public boolean read(SocketChannel sc, ContentEntity contentEntity)
			throws IOException {
		logger.debug("DataTypeReader start.");
		PacketEntity packetEntity = (PacketEntity)contentEntity;
		ByteBuffer bb = ByteBuffer.allocate(1);
		long readCount = 0;
		long ret = 0;
		long totalCount = 1;
        while (totalCount > readCount) {
        	ret = sc.read(bb);
            if (ret < 0 ) {
            	return false;
            }
        	readCount = readCount + ret;
        	logger.debug("DataTypeReader read bytes count : " + readCount);
        }
		int iDataType = ConvertUtils.byteToInt(bb.get(0));
		bb.clear();
		PacketDataType dataType = PacketDataType.values()[iDataType];
		logger.debug("DataTypeReader datType:" + dataType);
		packetEntity.setPacketDataType(dataType);
		if(dataType == PacketDataType.MESSAGE){
			MessageEntity messageEntity = new MessageEntity();
			messageEntity.setLength(contentEntity.getLength() - 1 - 8);
			packetEntity.setContentEntity(messageEntity);
			return wrappedMessageReader.read(sc, messageEntity);
		}
		else if(dataType == PacketDataType.FILE){
			FileEntity fileEntity = new FileEntity();
			fileEntity.setLength(contentEntity.getLength() - 1 - 8);
			packetEntity.setContentEntity(fileEntity);
			return wrappedFileReader.read(sc, fileEntity);
		}
		logger.debug("DataTypeReader end.");
		return false;
	}
}
