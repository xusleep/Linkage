package service.middleware.linkage.framework.setting.reader;

import static service.middleware.linkage.framework.common.ShareingData.SERVICE_ADDRESS;
import static service.middleware.linkage.framework.common.ShareingData.SERVICE_PORT;
import static service.middleware.linkage.framework.common.ShareingData.SERVICE_START_STRING;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import service.middleware.linkage.framework.setting.ServiceSettingEntity;

/**
 * this class used to read the properties from the service
 * @author zhonxu
 *
 */
public interface ServiceSettingReader {
	public String getServiceAddress();
	public int getServicePort();
	public List<ServiceSettingEntity> getServiceList();
}
