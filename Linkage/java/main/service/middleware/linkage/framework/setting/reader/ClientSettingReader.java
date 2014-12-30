package service.middleware.linkage.framework.setting.reader;

import java.util.List;

import service.middleware.linkage.framework.setting.ClientSettingEntity;

/**
 * this class used to read the properties from the client
 * @author zhonxu
 *
 */
public interface ClientSettingReader {
	public List<ClientSettingEntity> getServiceClientList();
}
