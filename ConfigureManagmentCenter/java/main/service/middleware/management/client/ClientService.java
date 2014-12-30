package service.middleware.management.client;

/**
 * this interface is used in the client.
 * the configure management center will notify the client
 * that new service is joined or the service is not available
 * @author Smile
 *
 */
public interface ClientService {
	public String removeService(String serviceInfor);
	public String addService(String serviceInfor);
}
