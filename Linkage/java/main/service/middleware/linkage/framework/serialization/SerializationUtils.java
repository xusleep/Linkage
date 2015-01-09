package service.middleware.linkage.framework.serialization;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringBufferInputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import service.middleware.linkage.framework.common.entity.RequestEntity;
import service.middleware.linkage.framework.common.entity.ResponseEntity;
import service.middleware.linkage.framework.io.common.FileRequestEntity;
import service.middleware.linkage.framework.io.common.FileRequestState;

public class SerializationUtils {
	
	public static String serilizationFileInformationEntity(FileRequestEntity objFileInformation){
		StringBuilder sb = new StringBuilder();
		sb.append("<RequestFile>");
		sb.append("<RequestState>");
		sb.append(escapeForXML(objFileInformation.getRequestFileState().toString()));
		sb.append("</RequestState>");
		sb.append("<fileName>");
		sb.append(escapeForXML(objFileInformation.getFileName()));
		sb.append("</fileName>");
		sb.append("<fileSize>");
		sb.append(escapeForXML("" + objFileInformation.getFileSize()));
		sb.append("</fileSize>");
		sb.append("<fileGetPath>");
		sb.append(escapeForXML("" + objFileInformation.getFileGetPath()));
		sb.append("</fileGetPath>");
		sb.append("<fileSavePath>");
		sb.append(escapeForXML("" + objFileInformation.getFileSavePath()));
		sb.append("</fileSavePath>");
		sb.append("</RequestFile>");
		return sb.toString();
	}
	
	public static FileRequestEntity deserilizationFileInformationEntity(String receiveData){
		try {
			InputStream is = new StringBufferInputStream(receiveData);
			FileRequestEntity objectFileInformationEntity = new FileRequestEntity();
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance(); 
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document document = db.parse(is);
			NodeList childs = document.getChildNodes().item(0).getChildNodes(); 
			for(int i = 0; i < childs.getLength(); i++){
				Node node = childs.item(i);
				if(node.getNodeName().equals("RequestState")){
					objectFileInformationEntity.setRequestFileState(FileRequestState.valueOf(FileRequestState.class, node.getTextContent()));
				}
				else if(node.getNodeName().equals("fileName")){
					objectFileInformationEntity.setFileName(node.getTextContent());
				}
				else if(node.getNodeName().equals("fileSize")){
					objectFileInformationEntity.setFileSize(Long.parseLong(node.getTextContent()));
				}
				else if(node.getNodeName().equals("fileGetPath")){
					objectFileInformationEntity.setFileGetPath(node.getTextContent());
				}
				else if(node.getNodeName().equals("fileSavePath")){
					objectFileInformationEntity.setFileSavePath(node.getTextContent());
				}
			}
			return objectFileInformationEntity;
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
	 * serialize request entity
	 * @param request
	 * @return
	 */
	public static String serializeRequest(RequestEntity request) {
		StringBuilder sb = new StringBuilder();
		sb.append("<request>");
		sb.append("<requestid>");
		sb.append(request.getRequestID());
		sb.append("</requestid>");
		sb.append("<serviceName>");
		sb.append(escapeForXML(request.getServiceName()));
		sb.append("</serviceName>");
		sb.append("<methodName>");
		sb.append(escapeForXML(request.getMethodName()));
		sb.append("</methodName>");
		sb.append("<version>");
		sb.append(escapeForXML(request.getVersion()));
		sb.append("</version>");
		sb.append("<group>");
		sb.append(escapeForXML(request.getGroup()));
		sb.append("</group>");
		sb.append("<list>");
		if(request.getArgs() != null){
			for (int i = 0; i < request.getArgs().size(); i++) {
				sb.append("<arg>");
				sb.append(escapeForXML(request.getArgs().get(i)));
				sb.append("</arg>");
			}
		}
		sb.append("</list>");
		sb.append("</request>");
		return sb.toString();
	}
	
	/**
	 * deserialize request entity
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
				if(node.getNodeName().equals("requestid")){
					request.setRequestID(node.getTextContent());
				}
				else if(node.getNodeName().equals("serviceName")){
					request.setServiceName(node.getTextContent());
				}
				else if(node.getNodeName().equals("methodName")){
					request.setMethodName(node.getTextContent());
				}
				else  if(node.getNodeName().equals("version")){
					request.setVersion(node.getTextContent());
				}
				else if(node.getNodeName().equals("group")){
					request.setGroup(node.getTextContent());
				}
				else if(node.getNodeName().equals("list")){
					NodeList childs1 = node.getChildNodes();
					for(int j = 0; j < childs1.getLength(); j++){
						Node listNode = childs1.item(j);
						if(listNode.getNodeName().equals("arg")){
							request.getArgs().add(listNode.getTextContent());
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
	 * serialize Response entity
	 * @param request
	 * @return
	 */
	public static String serializeResponse(ResponseEntity response) {
		StringBuilder sb = new StringBuilder();
		sb.append("<response>");
		sb.append("<requestid>");
		sb.append(response.getRequestID());
		sb.append("</requestid>");
		sb.append("<result>");
		sb.append(escapeForXML(response.getResult()));
		sb.append("</result>");
		sb.append("</response>");
		return sb.toString();
	}
	
	/**
	 * deserialize Response entity
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
					response.setResult(node.getTextContent());
				}
				else if(node.getNodeName().equals("requestid")){
					response.setRequestID(node.getTextContent());
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
}
