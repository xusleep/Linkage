package service.framework.properties;

import static service.framework.common.ShareingData.CLIENT_ROUTE;
import static service.framework.common.ShareingData.CLIENT_START_STRING;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import service.framework.route.AbstractRoute;

public class WorkingClientPropertyEntity {
	private final List<ClientPropertyEntity> serviceClientList = new LinkedList<ClientPropertyEntity>();
	private final List<AbstractRoute> routeList = new LinkedList<AbstractRoute>();
	private AbstractRoute defaultRoute;
	
	public WorkingClientPropertyEntity(String propertyFileName){
		InputStream inputStream = WorkingClientPropertyEntity.class.getClassLoader().getResourceAsStream(propertyFileName);
		//创建一个Properties容器 
        Properties properties = new Properties(); 
        //从流中加载properties文件信息 
        try {
			properties.load(inputStream);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
        readRouteList(properties);
        readClientList(properties);
	}
	
	private void readRouteList(Properties properties){
	
		String defaultRouteID = properties.getProperty("route.default.id");
		String defaultRouteClass = properties.getProperty("route.default.class");
		List<String> defaultRouteProperties = new LinkedList<String>();
		for(int j = 1; j < 100; j++){
			String strProperty = properties.getProperty("route.default.property" + j);
			if(strProperty == null || strProperty == "")
				break;
			defaultRouteProperties.add(strProperty);
		}
		try {
			if(defaultRouteID != null || defaultRouteID != "")
			{
				this.defaultRoute = (AbstractRoute) Class.forName(defaultRouteClass).newInstance();
				this.defaultRoute.setRouteid(defaultRouteID);
				this.defaultRoute.setRouteProperties(defaultRouteProperties);
			}
			else
			{
				System.out.println("there is not default route defined.");
			}
		} catch (InstantiationException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IllegalAccessException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (ClassNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		for (int i = 1; i < 1000; i++) {
			String idString = CLIENT_ROUTE + i + ".id";
			String routeid = properties.getProperty(idString);
			if (routeid != null && routeid != "") {
				String routeClass = properties.getProperty(CLIENT_ROUTE + i + ".class");
				List<String> routeProperties = new LinkedList<String>();
				for(int j = 1; j < 100; j++){
					routeProperties.add(properties.getProperty(CLIENT_ROUTE + i + ".property" + j));
				}
				try {
					AbstractRoute route = (AbstractRoute) Class.forName(routeClass).newInstance();
					route.setRouteid(routeid);
					route.setRouteProperties(routeProperties);
					routeList.add(route);
				} catch (InstantiationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (ClassNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			if (routeid == null || routeid == "") {
				break;
			}
		}
	}
	
	private void readClientList(Properties properties){
		for (int i = 1; i < 1000; i++) {
			String clientName = properties.getProperty(CLIENT_START_STRING + i
					+ ".name");
			if (clientName != null && clientName != "") {
				ClientPropertyEntity entity = new ClientPropertyEntity();
				entity.setServiceName(clientName);
				entity.setServiceGroup(properties
						.getProperty(CLIENT_START_STRING + i + ".group"));
				entity.setServiceMethod(properties
						.getProperty(CLIENT_START_STRING + i + ".method"));
				entity.setServiceVersion(properties
						.getProperty(CLIENT_START_STRING + i + ".version"));
				entity.setId(properties.getProperty(CLIENT_START_STRING + i
						+ ".id"));
				entity.setRouteid(properties.getProperty(CLIENT_START_STRING + i
						+ ".routeid"));
				serviceClientList.add(entity);
			}
			if (clientName == null || clientName == "") {
				break;
			}
		}
	}

	public List<ClientPropertyEntity> getServiceClientList() {
		return serviceClientList;
	}

	public List<AbstractRoute> getRouteList() {
		return routeList;
	}

	public AbstractRoute getDefaultRoute() {
		return defaultRoute;
	}
}
