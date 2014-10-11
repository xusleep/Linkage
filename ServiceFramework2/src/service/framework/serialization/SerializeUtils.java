package service.framework.serialization;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringBufferInputStream;
import java.util.LinkedList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import service.framework.provide.entity.RequestEntity;
import service.framework.provide.entity.ResponseEntity;
import servicecenter.service.ServiceInformation;

public class SerializeUtils {
	/**
	 * 将request实体序列化为字符串
	 * @param request
	 * @return
	 */
	public static String serializeRequest(RequestEntity request) {
		StringBuilder sb = new StringBuilder();
		sb.append("<request>");
		sb.append("<serviceName>");
		sb.append(request.getServiceName());
		sb.append("</serviceName>");
		sb.append("<methodName>");
		sb.append(request.getMethodName());
		sb.append("</methodName>");
		sb.append("<version>");
		sb.append(request.getVersion());
		sb.append("</version>");
		sb.append("<group>");
		sb.append(request.getGroup());
		sb.append("</group>");
		sb.append("<list>");
		for (int i = 0; i < request.getArgs().size(); i++) {
			sb.append("<arg>");
			sb.append(escapeForXML(request.getArgs().get(i)));
			sb.append("</arg>");
		}
		sb.append("</list>");
		sb.append("</request>");
		return sb.toString();
	}
	
	/**
	 * 将request实体序列化为字符串
	 * @param request
	 * @return
	 */
	public static RequestEntity deserializeRequest(String receiveData) {
		try {
			InputStream is = new StringBufferInputStream(receiveData);
			RequestEntity request = new RequestEntity();
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance(); 
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document document = db.parse(is);
			NodeList childs = document.getChildNodes().item(0).getChildNodes(); 
			for(int i = 0; i < childs.getLength(); i++){
				Node node = childs.item(i);
				if(node.getNodeName().equals("serviceName")){
					request.setServiceName(node.getTextContent());
				}
				if(node.getNodeName().equals("methodName")){
					request.setMethodName(node.getTextContent());
				}
				if(node.getNodeName().equals("version")){
					request.setVersion(node.getTextContent());
				}
				if(node.getNodeName().equals("group")){
					request.setGroup(node.getTextContent());
				}
				if(node.getNodeName().equals("list")){
					NodeList childs1 = node.getChildNodes();
					for(int j = 0; j < childs1.getLength(); j++){
						Node listNode = childs1.item(j);
						if(listNode.getNodeName().equals("arg")){
							request.getArgs().add(unEscapeForXML(listNode.getTextContent()));
						}
					}
				}
			}
			return request;
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
	 * @param request
	 * @return
	 */
	public static String serializeResponse(ResponseEntity response) {
		StringBuilder sb = new StringBuilder();
		sb.append("<response>");
		sb.append("<result>");
		sb.append(escapeForXML(response.getResult()));
		sb.append("</result>");
		sb.append("</response>");
		return sb.toString();
	}
	
	/**
	 * 将request实体序列化为字符串
	 * @param request
	 * @return
	 */
	public static ResponseEntity deserializeResponse(String receiveData) {
		try {
			InputStream is = new StringBufferInputStream(receiveData);
			ResponseEntity response = new ResponseEntity();
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance(); 
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document document = db.parse(is);
			NodeList childs = document.getChildNodes().item(0).getChildNodes(); 
			for(int i = 0; i < childs.getLength(); i++){
				Node node = childs.item(i);
				if(node.getNodeName().equals("result")){
					response.setResult(unEscapeForXML(node.getTextContent()));
				}
			}
			return response;
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
     * Escapes all necessary characters in the String so that it can be used
     * in an XML doc.
     *
     * @param string the string to escape.
     * @return the string with appropriate characters escaped.
     */
    public static final String escapeForXML ( String string )
    {

        // Check if the string is null or zero length -- if so, return
        // what was sent in.
        if ( ( string == null ) || ( string.length() == 0 ) )
        {
            return string;
        }

        char[]       sArray = string.toCharArray();
        StringBuffer buf    = new StringBuffer( sArray.length );
        char         ch;

        // according to http://www.w3.org/TR/REC-xml/#charsets
        // a valid xml character is defined as:
        // #x9 | #xA | #xD | [#x20-#xD7FF] | [#xE000-#xFFFD] | [#x10000-#x10FFFF]
        for ( int i = 0; i < sArray.length; i++ )
        {
            ch = sArray [ i ];

            if ( ch == '<' )
            {
                buf.append( "&lt;" );
            }
            else if ( ch == '&' )
            {
                buf.append( "&amp;" );
            }
            else if ( ch == '>' )
            {
                buf.append( "&gt;" );
            }
            else if ( ch == '"' )
            {
                buf.append( "&quot;" );
            }
            else if ( ch == '\'' )
            {
                buf.append( "&apos;" );
            }
            else if ( ch == '\n' )
            {
                buf.append( ch );
            }
            else if ( ch == '\r' )
            {
                buf.append( ch );
            }
            else if ( ch == '\t' )
            {
                buf.append( ch );
            }
            else if ( (int)ch < 0x20 )
            {
                buf.append( " " );
            }
            else
            {
                buf.append( ch );
            }
        }

        return buf.toString();
    }
    
    /**
     * Escapes all necessary characters in the String so that it can be used
     * in an XML doc.
     *
     * @param string the string to escape.
     * @return the string with appropriate characters escaped.
     */
    public static final String unEscapeForXML ( String string )
    {

        // Check if the string is null or zero length -- if so, return
        // what was sent in.
        if ( ( string == null ) || ( string.length() == 0 ) )
        {
            return string;
        }

        char[]       sArray = string.toCharArray();
        StringBuffer buf    = new StringBuffer( sArray.length );
        char         ch;

        // according to http://www.w3.org/TR/REC-xml/#charsets
        // a valid xml character is defined as:
        // #x9 | #xA | #xD | [#x20-#xD7FF] | [#xE000-#xFFFD] | [#x10000-#x10FFFF]
        for ( int i = 0; i < sArray.length; i++ )
        {
            ch = sArray [ i ];
            if(ch == '&')
            {
            	String nextString = string.substring(i, sArray.length > i + 6 ? i + 6 : sArray.length);
            	if(nextString.contains("&lt;"))
            	{
            		buf.append('<');
            		i = i + 3;
            	}
            	else if(nextString.contains("&amp;"))
            	{
            		buf.append('&');
            		i = i + 4;
            	}
            	else if(nextString.contains("&gt;"))
            	{
            		buf.append('>');
            		i = i + 3;
            	}
            	else if(nextString.contains("&quot;"))
            	{
            		buf.append('"');
            		i = i + 4;
            	}
            	else if(nextString.contains("&apos;"))
            	{
            		buf.append('\'');
            		i = i + 5;
            	}
            	else if(nextString.contains("&quot;"))
            	{
            		buf.append('"');
            		i = i + 5;
            	}
            }
            else
            {
            	buf.append(ch);
            }
        }

        return buf.toString();
    }
	
    /**
	 * 将ServiceInformation实体序列化为字符串
	 * @param objServiceInformation
	 * @return
	 */
	public static String serializeServiceInformation(ServiceInformation objServiceInformation) {
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
	 * @param request
	 * @return
	 */
	public static ServiceInformation deserializeServiceInformation(String receiveData) {
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
			for(int i = 0; i < childs.getLength(); i++){
				Node node = childs.item(i);
				if(node.getNodeName().equals("address")){
					address = node.getTextContent();
				}
				else if(node.getNodeName().equals("port")){
					port = node.getTextContent();
				}
				else if(node.getNodeName().equals("serviceName")){
					serviceName  = node.getTextContent();
				}
				else if(node.getNodeName().equals("serviceMethod")){
					serviceMethod  = node.getTextContent();
				}
				else if(node.getNodeName().equals("serviceVersion")){
					serviceVersion  = node.getTextContent();
				}
			}
			ServiceInformation objServiceInformation = new ServiceInformation(address, Integer.parseInt(port), serviceName, serviceMethod, serviceVersion);
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
	 * @param request
	 * @return
	 */
	public static List<ServiceInformation> deserializeServiceInformationList(String receiveData) {
		try {
			System.out.println("deserializeServiceInformationList :" + receiveData);
			List<ServiceInformation> list = new LinkedList<ServiceInformation>();
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
			for(int i = 0; i < childs.getLength(); i++){
				Node node = childs.item(i);
				if(node.getNodeName().equals("serviceInformation")){
					NodeList childs1 = node.getChildNodes();
					for(int j = 0; j < childs1.getLength(); j++){
						Node node1 = childs1.item(j);
						if(node1.getNodeName().equals("address")){
							address = node1.getTextContent();
						}
						else if(node1.getNodeName().equals("port")){
							port = node1.getTextContent();
						}
						else if(node1.getNodeName().equals("serviceName")){
							serviceName  = node1.getTextContent();
						}
						else if(node1.getNodeName().equals("serviceMethod")){
							serviceMethod  = node1.getTextContent();
						}
						else if(node1.getNodeName().equals("serviceVersion")){
							serviceVersion  = node1.getTextContent();
						}
					}
					ServiceInformation objServiceInformation = new ServiceInformation(address, Integer.parseInt(port), serviceName, serviceMethod, serviceVersion);
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
	 * @param request
	 * @return
	 */
	public static String serializeServiceInformationList(List<ServiceInformation> serviceInformationList) {
		StringBuilder sb = new StringBuilder();
		sb.append("<serviceInformationList>");
		for(ServiceInformation objServiceInformation : serviceInformationList){
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
	
	
	public static void main(String[] args) {
//		RequestEntity request = new RequestEntity();
//		request.setGroup("dsfsdf");
//		request.getArgs().add("sdfsdfdsfds121");
//        String str = SerializeUtils.serializeRequest(request);
//        System.out.println(str);
//        RequestEntity request1 = SerializeUtils.deserializeRequest(str);
//        System.out.println(request1.getArgs().get(0));
		String s = escapeForXML("<t>sd</t>");
		System.out.println(s);
		System.out.println(unEscapeForXML(s));
	}
}
