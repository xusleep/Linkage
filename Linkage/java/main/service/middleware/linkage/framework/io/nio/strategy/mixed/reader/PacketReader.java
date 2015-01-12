package service.middleware.linkage.framework.io.nio.strategy.mixed.reader;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import org.apache.log4j.Logger;

import service.middleware.linkage.framework.io.nio.strategy.mixed.packet.ContentEntity;
import service.middleware.linkage.framework.io.nio.strategy.mixed.packet.PacketEntity;
import service.middleware.linkage.framework.utils.ConvertUtils;

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
