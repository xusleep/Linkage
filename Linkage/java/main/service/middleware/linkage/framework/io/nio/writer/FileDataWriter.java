package service.middleware.linkage.framework.io.nio.writer;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SocketChannel;

import org.apache.log4j.Logger;

import service.middleware.linkage.framework.common.ConvertUtils;
import service.middleware.linkage.framework.io.common.NIOFileMessageMixStrategy;
import service.middleware.linkage.framework.io.nio.ContentEntity;
import service.middleware.linkage.framework.io.nio.FileEntity;
import service.middleware.linkage.framework.io.nio.FileInformationEntity;

public class FileDataWriter extends WriterDecorator {
	private static final long FILE_TRANSFER_BUFFER_SIZE = 1024 * 1024 * 10;
	private static Logger logger = Logger.getLogger(FileDataWriter.class);
	
	public FileDataWriter(WriterInterface wrappedWriter) {
		super(wrappedWriter);
		// TODO Auto-generated constructor stub
	}

	@Override
	public boolean write(SocketChannel sc, ContentEntity contentEntity)
			throws IOException {
		FileEntity fileEntity = (FileEntity)contentEntity;
		FileInformationEntity fileInformationEntity = NIOFileMessageMixStrategy.findFileInformationEntity(fileEntity.getFileID());
		FileInputStream fis = new FileInputStream(new File(fileInformationEntity.getFilePath()));
		FileChannel fileChannel = fis.getChannel();
		long writtenCount = 0;
		long totalCount = 8;
		ByteBuffer bb = ByteBuffer.allocate((int) totalCount);
		byte[] dst = ConvertUtils.longToBytes(fileEntity.getFileID());
		bb.put(dst, 0, (int) totalCount);
		bb.flip();
		while(writtenCount != totalCount)
		{
			writtenCount = writtenCount + sc.write(bb);
			logger.debug("FileDataWriter write bytes count : " + writtenCount);
		}
		writtenCount = 0;
		totalCount = fileEntity.getLength() - 8;
		while (writtenCount < totalCount) {
			long buffSize = FILE_TRANSFER_BUFFER_SIZE; 
			if (totalCount - writtenCount < buffSize) {
				buffSize = totalCount - writtenCount;
			}
			long transferred = fileChannel.transferTo(writtenCount, buffSize, sc);
			if (transferred > 0) {
				writtenCount += transferred;
				logger.debug("transfered " + writtenCount + " bytes");
			}
		}
		return true;
	}

}
