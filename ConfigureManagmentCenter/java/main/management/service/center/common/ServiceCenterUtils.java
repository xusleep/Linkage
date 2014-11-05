package management.service.center.common;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringBufferInputStream;
import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import service.framework.common.entity.RequestResultEntity;
import service.framework.common.entity.ServiceInformationEntity;
import service.framework.comsume.ConsumerBean;
import service.framework.exception.ServiceException;
import service.framework.properties.ServicePropertyEntity;
import service.framework.properties.WorkingServicePropertyEntity;

public final class ServiceCenterUtils {
	public static final String SERVICE_CENTER_SERVICE_NAME = "serviceCenter";
	public static final String SERVICE_CENTER_REGISTER_ID = "serviceCenterRegister";
	public static final String SERVICE_CENTER_UNREGISTER_ID = "serviceCenterUnregister";
	public static final String SERVICE_CENTER_GET_SERVICE_ID = "serviceCenterGetServiceList";
	
	public static boolean registerService(ConsumerBean consumerBean, WorkingServicePropertyEntity workingServicePropertyEntity) throws ServiceException{
		List<ServiceInformationEntity> serviceInformationList = new LinkedList<ServiceInformationEntity>();
		
		//获取所有服务，将服务注册到注册中心
		List<ServicePropertyEntity> serviceList = workingServicePropertyEntity.getServiceList();
		for(ServicePropertyEntity serviceEntity: serviceList)
		{
			String interfaceName = serviceEntity.getServiceInterface();
			try {
				Class interfaceclass = Class.forName(interfaceName);
				Method[] methods = interfaceclass.getMethods();
				for(int i = 0; i < methods.length; i++){
					ServiceInformationEntity subServiceInformation = new ServiceInformationEntity();
					subServiceInformation.setAddress(workingServicePropertyEntity.getServiceAddress());
					subServiceInformation.setPort(workingServicePropertyEntity.getServicePort());
					subServiceInformation.setServiceMethod(methods[i].getName());
					subServiceInformation.setServiceName(serviceEntity.getServiceName());
					subServiceInformation.setServiceVersion(serviceEntity.getServiceVersion());
					serviceInformationList.add(subServiceInformation);
					System.out.println("service name : " + serviceEntity.getServiceName() + " method name : " + methods[i].getName());
				}
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		String strServiceInformation = ServiceCenterUtils.serializeServiceInformationList(serviceInformationList);
		List<String> args = new LinkedList<String>();
		args.add(strServiceInformation);
		RequestResultEntity result = consumerBean.prcessRequestPerConnectSync(SERVICE_CENTER_REGISTER_ID, args);
		if(result.isException()){
			result.getException().printStackTrace();
			throw result.getException();
		}
		return true;
	}
	
	/**
	 * 将ServiceInformation实体序列化为字符串
	 * 
	 * @param objServiceInformation
	 * @return
	 */
	public static String serializeServiceInformation(
			ServiceInformationEntity objServiceInformation) {
		StringBuilder sb = new StringBuilder();
		sb.append("<serviceInformation>");
		sb.append("<address>");
		sb.append(objServiceInformation.getAddress());
		sb.append("</address>");
		sb.append("<port>");
		sb.append(objServiceInformation.getPort());
		sb.append("</port>");
		sb.append("<serviceName>");
		sb.append(objServiceInformation.getServiceName());
		sb.append("</serviceName>");
		sb.append("<serviceMethod>");
		sb.append(objServiceInformation.getServiceMethod());
		sb.append("</serviceMethod>");
		sb.append("<serviceVersion>");
		sb.append(objServiceInformation.getServiceVersion());
		sb.append("</serviceVersion>");
		sb.append("</serviceInformation>");
		return sb.toString();
	}

	/**
	 * 将request实体序列化为字符串
	 * 
	 * @param request
	 * @return
	 */
	public static ServiceInformationEntity deserializeServiceInformation(
			String receiveData) {
		try {
			InputStream is = new StringBufferInputStream(receiveData);
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document document = db.parse(is);
			NodeList childs = document.getChildNodes().item(0).getChildNodes();
			String address = "";
			String port = "";
			String serviceName = "";
			String serviceMethod = "";
			String serviceVersion = "";
			for (int i = 0; i < childs.getLength(); i++) {
				Node node = childs.item(i);
				if (node.getNodeName().equals("address")) {
					address = node.getTextContent();
				} else if (node.getNodeName().equals("port")) {
					port = node.getTextContent();
				} else if (node.getNodeName().equals("serviceName")) {
					serviceName = node.getTextContent();
				} else if (node.getNodeName().equals("serviceMethod")) {
					serviceMethod = node.getTextContent();
				} else if (node.getNodeName().equals("serviceVersion")) {
					serviceVersion = node.getTextContent();
				}
			}
			ServiceInformationEntity objServiceInformation = new ServiceInformationEntity(
					address, Integer.parseInt(port), serviceName,
					serviceMethod, serviceVersion);
			return objServiceInformation;
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 将request实体序列化为字符串
	 * 
	 * @param request
	 * @return
	 */
	public static List<ServiceInformationEntity> deserializeServiceInformationList(
			String receiveData) {
		try {
			System.out.println("deserializeServiceInformationList :"
					+ receiveData);
			List<ServiceInformationEntity> list = new LinkedList<ServiceInformationEntity>();
			InputStream is = new StringBufferInputStream(receiveData);
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document document = db.parse(is);
			NodeList childs = document.getChildNodes().item(0).getChildNodes();
			String address = "";
			String port = "";
			String serviceName = "";
			String serviceMethod = "";
			String serviceVersion = "";
			for (int i = 0; i < childs.getLength(); i++) {
				Node node = childs.item(i);
				if (node.getNodeName().equals("serviceInformation")) {
					NodeList childs1 = node.getChildNodes();
					for (int j = 0; j < childs1.getLength(); j++) {
						Node node1 = childs1.item(j);
						if (node1.getNodeName().equals("address")) {
							address = node1.getTextContent();
						} else if (node1.getNodeName().equals("port")) {
							port = node1.getTextContent();
						} else if (node1.getNodeName().equals("serviceName")) {
							serviceName = node1.getTextContent();
						} else if (node1.getNodeName().equals("serviceMethod")) {
							serviceMethod = node1.getTextContent();
						} else if (node1.getNodeName().equals("serviceVersion")) {
							serviceVersion = node1.getTextContent();
						}
					}
					ServiceInformationEntity objServiceInformation = new ServiceInformationEntity(
							address, Integer.parseInt(port), serviceName,
							serviceMethod, serviceVersion);
					list.add(objServiceInformation);
				}
			}
			return list;
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 将request实体序列化为字符串
	 * 
	 * @param request
	 * @return
	 */
	public static String serializeServiceInformationList(
			List<ServiceInformationEntity> serviceInformationList) {
		StringBuilder sb = new StringBuilder();
		sb.append("<serviceInformationList>");
		for (ServiceInformationEntity objServiceInformation : serviceInformationList) {
			sb.append("<serviceInformation>");
			sb.append("<address>");
			sb.append(objServiceInformation.getAddress());
			sb.append("</address>");
			sb.append("<port>");
			sb.append(objServiceInformation.getPort());
			sb.append("</port>");
			sb.append("<serviceName>");
			sb.append(objServiceInformation.getServiceName());
			sb.append("</serviceName>");
			sb.append("<serviceMethod>");
			sb.append(objServiceInformation.getServiceMethod());
			sb.append("</serviceMethod>");
			sb.append("<serviceVersion>");
			sb.append(objServiceInformation.getServiceVersion());
			sb.append("</serviceVersion>");
			sb.append("</serviceInformation>");
		}
		sb.append("</serviceInformationList>");
		return sb.toString();
	}
}
