package service.frame.web.servlets;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import management.bootstrap.CenterClientBootStrap;
import management.service.client.ServiceCenterClientUtils;
import service.framework.bootstrap.ServerBootStrap;
import service.framework.common.entity.ServiceInformationEntity;

/**
 * This servlet is used to start the service from the web
 * Servlet implementation class ServiceProvideServlet
 */
@WebServlet("/ServiceProvideServlet")
public class ServiceProvideServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public ServiceProvideServlet() {
        super();
        // TODO Auto-generated constructor stub
        initServiceProvide();
    }
    
    private void initServiceProvide(){
    	 try {
         	ServerBootStrap serviceBootStrap = new ServerBootStrap("conf/service_server.properties", 5);
         	ServiceInformationEntity centerServiceInformationEntity = new ServiceInformationEntity();
         	centerServiceInformationEntity.setAddress("localhost");
         	centerServiceInformationEntity.setPort(5002);
         	CenterClientBootStrap clientBootStrap = new CenterClientBootStrap("conf/service_client.properties", 
         			5, centerServiceInformationEntity);
     		serviceBootStrap.run();
     		clientBootStrap.run();
     		ServiceCenterClientUtils.defaultRouteConsume = clientBootStrap.getConsume();
     		try {
     			ServiceCenterClientUtils.registerServiceList(ServiceCenterClientUtils.defaultRouteConsume, centerServiceInformationEntity, serviceBootStrap.getServicePropertyEntity());
     		}
     		catch (Exception e) {
     			
     		}
     		
     		//Thread.sleep(1000);
     		//serviceBootStrap.shutdownImediate();
     		//clientBootStrap.shutdownImediate();
         }
         catch (Exception e) {
         	e.printStackTrace();
             System.out.println("Server error: " + e.getMessage());
         }
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
	}

}
