package service.middleware.linkage.framework.io.nio.strategy.mixed.reader;

import java.io.IOException;
import java.nio.channels.SocketChannel;

import service.middleware.linkage.framework.io.nio.strategy.mixed.packet.ContentEntity;

/**
 * an interface for all of the reader
 * @author zhonxu
 *
 */
public interface ReaderInterface {
	public boolean read(SocketChannel sc, ContentEntity contentEntity) throws IOException;
}
