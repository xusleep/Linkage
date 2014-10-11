package service.framework.route;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import service.framework.provide.entity.RequestEntity;
import servicecenter.service.ServiceInformation;

public interface Route {
	public ServiceInformation chooseRoute(String serviceName) throws IOException, InterruptedException, ExecutionException;
}
