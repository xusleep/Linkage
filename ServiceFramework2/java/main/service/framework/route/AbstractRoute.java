package service.framework.route;

import java.util.List;

/**
 *  All of the route class should extend this class
 * @author Smile
 *
 */
public abstract class AbstractRoute implements Route {

	private String routeid;
	private List<String> routeProperties;
	
	public String getRouteid() {
		return routeid;
	}
	public void setRouteid(String routeid) {
		this.routeid = routeid;
	}
	public List<String> getRouteProperties() {
		return routeProperties;
	}
	public void setRouteProperties(List<String> routeProperties) {
		this.routeProperties = routeProperties;
	}
}
