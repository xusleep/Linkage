package service.middleware.linkage.framework.io.common;

import java.io.File;

public class FileInformationEntity {
	private String fileName;
	private long fileSize;
	private File file;
	private  RequestFileState RequestFileState;
	
	public FileInformationEntity(){
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public long getFileSize() {
		return fileSize;
	}

	public void setFileSize(long fileSize) {
		this.fileSize = fileSize;
	}

	public RequestFileState getRequestFileState() {
		return RequestFileState;
	}

	public void setRequestFileState(RequestFileState requestFileState) {
		RequestFileState = requestFileState;
	}

	public File getFile() {
		return file;
	}

	public void setFile(File file) {
		this.file = file;
	}

	
}
