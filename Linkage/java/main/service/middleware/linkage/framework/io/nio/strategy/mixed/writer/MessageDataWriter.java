package service.middleware.linkage.framework.io.nio.strategy.mixed.writer;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import org.apache.log4j.Logger;

import service.middleware.linkage.framework.exception.ServiceOnChanelIOException;
import service.middleware.linkage.framework.io.WorkingChannelOperationResult;
import service.middleware.linkage.framework.io.nio.strategy.mixed.events.ServiceExeptionEvent;
import service.middleware.linkage.framework.io.nio.strategy.mixed.packet.ContentEntity;
import service.middleware.linkage.framework.io.nio.strategy.mixed.packet.MessageEntity;
import service.middleware.linkage.framework.utils.StringUtils;

public class MessageDataWriter extends WriterDecorator {
	private static Logger logger = Logger.getLogger(MessageDataWriter.class);
	public MessageDataWriter(WriterInterface wrappedWriter) {
		super(wrappedWriter);
		// TODO Auto-generated constructor stub
	}

	@Override
	public boolean write(SocketChannel sc, ContentEntity contentEntity)
			throws IOException {
		logger.debug("MessageDataWriter start.");
		MessageEntity messageEntity = (MessageEntity)contentEntity;
		ByteBuffer buffer = ByteBuffer.allocate((int) messageEntity.getLength());
		buffer.put(messageEntity.getData(), 0, (int) messageEntity.getLength());
		buffer.flip();
		int writtenCount = 0;
		int totalCount = (int) messageEntity.getLength();
		while(writtenCount != totalCount)
		{
			writtenCount = writtenCount + sc.write(buffer);
			logger.debug("MessageDataWriter write bytes count : " + writtenCount);
		}
		logger.debug("MessageDataWriter end.");
		return false;
	}

}
