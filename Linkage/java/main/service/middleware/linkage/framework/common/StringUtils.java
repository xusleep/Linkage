package service.middleware.linkage.framework.common;

import java.io.PrintWriter;
import java.io.StringWriter;

public class StringUtils {
	
	/**
	 * check the string is empty or null
	 * @param str
	 * @return
	 */
	public static boolean isEmpty(String str){
		return str == null || str == "";
	}
	
	
	/**
	 * get the stack trace to string
	 * @param ex
	 * @return
	 */
	public static String ExceptionStackTraceToString(Exception ex){
		StringWriter sw = new StringWriter();  
		PrintWriter pw = new PrintWriter(sw); 
		pw.append("stack message: \r\n");
		ex.printStackTrace(pw);
		return sw.toString();
	}
}
