package servicecenter.service;

import java.util.LinkedList;
import java.util.List;

public interface ServiceCenter {
	public static List<ServiceInformation> ServiceInformationList = new LinkedList<ServiceInformation>();
	
	public String register(String serviceInfor);
	public String getServiceList(String servicename);
}
