package service.middleware.linkage.framework.io.common;

public class FileInformation {
	private final String fileName;
	private final long fileSize;
	
	public FileInformation(String fileName, long fileSize){
		this.fileName = fileName;
		this.fileSize = fileSize;
	}
	
	public String getFileName() {
		return fileName;
	}
	public long getFileSize() {
		return fileSize;
	}
}
