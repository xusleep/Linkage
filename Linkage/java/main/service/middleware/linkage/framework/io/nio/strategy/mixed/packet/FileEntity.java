package service.middleware.linkage.framework.io.nio.strategy.mixed.packet;

/**
 * This entity retain the file content
 * @author zhonxu
 *
 */
public class FileEntity extends ContentEntity{
	private long fileID;
	public long getFileID() {
		return fileID;
	}
	public void setFileID(long fileID) {
		this.fileID = fileID;
	}

}
