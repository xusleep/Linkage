package service.middleware.linkage.framework;

import java.util.LinkedList;
import java.util.List;

import service.middleware.linkage.framework.io.nio.strategy.mixed.packet.FileInformationEntity;

public class FileInformationStorageList {
	
	// transfered file list, wait for transfering or receiveing
	private static List<FileInformationEntity> transferFileList = new LinkedList<FileInformationEntity>();
	/**
	 * 
	 * @param fileID
	 */
	public static synchronized void removeFileInformationEntity(long fileID){
		int index = -1;
		for(index = 0; index < transferFileList.size(); index++){
			if(transferFileList.get(index).getFileID() == fileID){
				break;
			}
		}
		transferFileList.remove(index);
	}
	
	/**
	 * 
	 * @param fileInformationEntity
	 */
	public static synchronized void addFileInformationEntity(FileInformationEntity fileInformationEntity){
		transferFileList.add(fileInformationEntity);
	}
	
	/**
	 * 
	 * @param fileID
	 * @return
	 */
	public static synchronized FileInformationEntity findFileInformationEntity(long fileID){
		for(FileInformationEntity fileInformationEntity : transferFileList){
			if(fileInformationEntity.getFileID() == fileID){
				return fileInformationEntity;
			}
		}
		return null;
	}
}
