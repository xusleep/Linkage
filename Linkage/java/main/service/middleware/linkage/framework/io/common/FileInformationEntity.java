package service.middleware.linkage.framework.io.common;

import java.io.File;

public class FileInformationEntity {
	private String fileName;
	private long fileSize;
	private File writeFile;
	private File readFile;
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

	public File getWriteFile() {
		return writeFile;
	}

	public File getReadFile() {
		return readFile;
	}

	public void setReadFile(File readFile) {
		this.readFile = readFile;
	}

	public void setWriteFile(File writeFile) {
		this.writeFile = writeFile;
	}
}
