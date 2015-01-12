package clould.storage.service;

import java.util.concurrent.atomic.AtomicLong;

import service.middleware.linkage.framework.FileInformationStorageList;
import service.middleware.linkage.framework.io.nio.strategy.mixed.packet.FileInformationEntity;


public class FileStorageServiceImpl implements FileStorageService{
	private static AtomicLong fileIDGenerator = new AtomicLong(0);
	@Override
	public String getUploadFileID(String fileSavePath) {
		long fileID = fileIDGenerator.incrementAndGet();
		FileInformationEntity fileInformationEntity = new FileInformationEntity();
		fileInformationEntity.setFileID(fileID);
		fileInformationEntity.setFilePath(fileSavePath);
		FileInformationStorageList.addFileInformationEntity(fileInformationEntity);
		return "" + fileID;
	}

}
