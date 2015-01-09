package service.middleware.linkage.framework.io.common;

public class FileTransferEntity {
	private final String fileGetPath;
	private final String fileSavePath;
	
	public FileTransferEntity(String fileGetPath, String fileSavePath) {
		this.fileGetPath = fileGetPath;
		this.fileSavePath = fileSavePath;
	}

	public String getFileGetPath() {
		return fileGetPath;
	}

	public String getFileSavePath() {
		return fileSavePath;
	}
	
	
}
