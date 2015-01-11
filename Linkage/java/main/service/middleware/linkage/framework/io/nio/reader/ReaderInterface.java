package service.middleware.linkage.framework.io.nio.reader;

import java.io.IOException;
import java.nio.channels.SocketChannel;

import service.middleware.linkage.framework.io.nio.ContentEntity;

public interface ReaderInterface {
	public boolean read(SocketChannel sc, ContentEntity contentEntity) throws IOException;
}
