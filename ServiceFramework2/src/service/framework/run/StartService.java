package service.framework.run;
import java.util.LinkedList;
import java.util.List;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import service.framework.io.fire.MasterHandler;
import service.framework.io.handlers.ClientReadWriteHandler;
import service.framework.io.handlers.ReadWriteHandler;
import service.framework.io.handlers.Handler;
import service.framework.io.handlers.ServiceRegisterHandler;
import service.framework.io.master.ServiceBootStrap;
import service.framework.io.server.Client;
import service.framework.io.server.Server;


/**
 * <p>Title: ∆Ù∂Ø¿‡</p>
 * @author starboy
 * @version 1.0
 */

public class StartService {

    public static void main(String[] args) {
        try {
        	ServiceBootStrap.getInstance().start();
        }
        catch (Exception e) {
            System.out.println("Server error: " + e.getMessage());
            System.exit(-1);
        }
    }
}
