package service.middleware.linkage.framework.io.common;


public class FileRequestEntity {
	private String fileName;
	private long   fileSize;
	private String fileGetPath;
	private String fileSavePath;
	private FileRequestState RequestFileState;
	
	public FileRequestEntity(){
		
	}
	
	public FileRequestEntity(String fileGetPath, String fileSavePath){
		this.fileGetPath = fileGetPath;
		this.fileSavePath = fileSavePath;
	}
	
	public String getFileGetPath() {
		return fileGetPath;
	}

	public String getFileSavePath() {
		return fileSavePath;
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

	public FileRequestState getRequestFileState() {
		return RequestFileState;
	}

	public void setRequestFileState(FileRequestState requestFileState) {
		RequestFileState = requestFileState;
	}

	public void setFileGetPath(String fileGetPath) {
		this.fileGetPath = fileGetPath;
	}

	public void setFileSavePath(String fileSavePath) {
		this.fileSavePath = fileSavePath;
	}
	
	
}
