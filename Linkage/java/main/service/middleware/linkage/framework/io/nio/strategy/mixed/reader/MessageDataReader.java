package service.middleware.linkage.framework.io.nio.strategy.mixed.reader;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import org.apache.log4j.Logger;

import service.middleware.linkage.framework.io.nio.strategy.mixed.packet.ContentEntity;
import service.middleware.linkage.framework.io.nio.strategy.mixed.packet.MessageEntity;

/**
 * read the message data
 * @author zhonxu
 *
 */
public class MessageDataReader extends ReaderDecorator {
	private static Logger logger = Logger.getLogger(MessageDataReader.class);
	
	
	public MessageDataReader(ReaderInterface wrappedReader) {
		super(wrappedReader);
	}
	
	@Override
	public boolean read(SocketChannel sc, ContentEntity contentEntity) throws IOException {
		logger.debug("MessageReader start.");
		MessageEntity messageEntity = (MessageEntity)contentEntity;

	    ByteBuffer bb = ByteBuffer.allocate((int) (contentEntity.getLength()));
	    long readCount = 0;
	    long ret = 0;
	    long totalCount = contentEntity.getLength();
        while (totalCount > readCount) {
        	ret = sc.read(bb);
            if (ret < 0 ) {
            	return false;
            }
        	readCount = readCount + ret;
        	logger.debug("MessageReader read bytes count : " + readCount);
        }

        messageEntity.setData(bb.array());
        logger.debug("MessageReader end.");
		return true;
	}
}
