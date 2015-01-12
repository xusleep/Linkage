package service.middleware.linkage.framework.io.nio.strategy.mixed.reader;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import org.apache.log4j.Logger;

import service.middleware.linkage.framework.io.nio.strategy.mixed.packet.ContentEntity;
import service.middleware.linkage.framework.io.nio.strategy.mixed.packet.PacketEntity;
import service.middleware.linkage.framework.utils.ConvertUtils;

/**
 * when reading a packet data from the channel
 * we are gona to use the decorator pattern to read.
 * the packet reader is a decorator which will wapper 
 * the data type reader, the data type reader will
 * wapper the file reader and message reader
 * we read from the packet reader, read the size of the packet
 * then goto the data type reader, read the datatype of the packet
 * if the data type is message , we goto the message reader
 * else we goto the file reader.
 * @author zhonxu
 *
 */
public class PacketReader extends ReaderDecorator {
	private static Logger logger = Logger.getLogger(PacketReader.class);
	
	public PacketReader(ReaderInterface wrappedReader) {
		super(wrappedReader);
		// TODO Auto-generated constructor stub
	}

	@Override
	public boolean read(SocketChannel sc, ContentEntity contentEntity)
			throws IOException {
		logger.debug("PacketReader start.");
		PacketEntity packetEntity = (PacketEntity)contentEntity;
		ByteBuffer bb = ByteBuffer.allocate(8);
		long readCount = 0;
		long ret = 0;
		long totalCount = 8;
        while (totalCount > readCount) {
        	ret = sc.read(bb);
            if (ret < 0 ) {
            	return false;
            }
        	readCount = readCount + ret;
        	logger.debug("PacketReader read bytes count : " + readCount);
        }
        packetEntity.setLength(ConvertUtils.bytesToLong(bb.array()));
        logger.debug("PacketReader packet length:" + packetEntity.getLength());
        logger.debug("PacketReader end.");
        return this.getWrappedReader().read(sc, packetEntity);
	}

}
