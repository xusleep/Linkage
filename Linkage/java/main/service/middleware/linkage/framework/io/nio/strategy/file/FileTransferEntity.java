package service.middleware.linkage.framework.io.nio.strategy.file;


public class FileTransferEntity {
	private String fileGetPath;
	private String fileSavePath;
	private FileRequestState RequestFileState;
	
	public FileTransferEntity(){
		
	}
	
	public FileTransferEntity(String fileGetPath, String fileSavePath){
		this.fileGetPath = fileGetPath;
		this.fileSavePath = fileSavePath;
	}
	
	public String getFileGetPath() {
		return fileGetPath;
	}

	public String getFileSavePath() {
		return fileSavePath;
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
