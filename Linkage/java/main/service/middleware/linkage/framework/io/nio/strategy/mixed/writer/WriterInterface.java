package service.middleware.linkage.framework.io.nio.strategy.mixed.writer;

import java.io.IOException;
import java.nio.channels.SocketChannel;

import service.middleware.linkage.framework.io.nio.strategy.mixed.packet.ContentEntity;

public interface WriterInterface {
	public boolean write(SocketChannel sc, ContentEntity contentEntity) throws IOException;
}
