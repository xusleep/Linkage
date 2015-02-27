package service.middleware.linkage.framework.connection.pool;

public interface ConnectionPoolManager {
	public IOConnection getIOConnection(String connectionString);
	public IOConnection getIOConnection(String ip, int port);
}
