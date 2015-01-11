package service.middleware.linkage.framework.io.common;

import java.io.File;
import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;

import service.middleware.linkage.framework.event.ServerOnFileDataReceivedEvent;
import service.middleware.linkage.framework.event.ServiceOnMessageDataReceivedEvent;
import service.middleware.linkage.framework.handlers.EventDistributionMaster;
import service.middleware.linkage.framework.io.nio.FileEntity;
import service.middleware.linkage.framework.io.nio.FileInformationEntity;
import service.middleware.linkage.framework.io.nio.MessageEntity;
import service.middleware.linkage.framework.io.nio.PacketDataType;
import service.middleware.linkage.framework.io.nio.PacketEntity;
import service.middleware.linkage.framework.io.nio.reader.DataTypeReader;
import service.middleware.linkage.framework.io.nio.reader.FileDataReader;
import service.middleware.linkage.framework.io.nio.reader.MessageDataReader;
import service.middleware.linkage.framework.io.nio.reader.PacketReader;
import service.middleware.linkage.framework.io.nio.reader.ReaderInterface;
import service.middleware.linkage.framework.io.nio.writer.DataTypeWriter;
import service.middleware.linkage.framework.io.nio.writer.FileDataWriter;
import service.middleware.linkage.framework.io.nio.writer.MessageDataWriter;
import service.middleware.linkage.framework.io.nio.writer.PacketWriter;
import service.middleware.linkage.framework.io.nio.writer.WriterInterface;

/**
 * this strategy is only used for the file mode only
 * @author zhonxu
 *
 */
public class NIOFileMessageMixStrategy extends WorkingChannelStrategy {
	
	private WriterInterface writer;
	private ReaderInterface reader;
	private static List<FileInformationEntity> fileList = new ArrayList<FileInformationEntity>();
	
	public NIOFileMessageMixStrategy(NIOWorkingChannelContext nioWorkingChannelContext,
			EventDistributionMaster eventDistributionHandler) {
		super(nioWorkingChannelContext, eventDistributionHandler);
		MessageDataWriter messageWriter = new MessageDataWriter(null);
		FileDataWriter fileWriter = new FileDataWriter(null);
		DataTypeWriter dataWriter = new DataTypeWriter(messageWriter, fileWriter);
		writer = new PacketWriter(dataWriter);
		
		MessageDataReader messageReader = new MessageDataReader(null);
		FileDataReader fileReader = new FileDataReader(null);
		DataTypeReader dataReader = new DataTypeReader(messageReader, fileReader);
		reader = new PacketReader(dataReader);
	}
	
	public static synchronized void removeFileInformationEntity(long fileID){
		int index = -1;
		for(index = 0; index < fileList.size(); index++){
			if(fileList.get(index).getFileID() == fileID){
				break;
			}
		}
		fileList.remove(index);
	}
	
	public static synchronized void addFileInformationEntity(FileInformationEntity fileInformationEntity){
		fileList.add(fileInformationEntity);
	}
	
	public static synchronized FileInformationEntity findFileInformationEntity(long fileID){
		for(FileInformationEntity fileInformationEntity : fileList){
			if(fileInformationEntity.getFileID() == fileID){
				return fileInformationEntity;
			}
		}
		return null;
	}

	@Override
	public synchronized WorkingChannelOperationResult readChannel() {
		PacketEntity packetEntity = new PacketEntity();
		boolean result = false;
		try {
			result = reader.read((SocketChannel) this.getWorkingChannelContext().getChannel(), packetEntity);
			if(packetEntity.getPacketDataType() == PacketDataType.MESSAGE)
			{
				MessageEntity contentEntity = (MessageEntity) packetEntity.getContentEntity();
				this.getEventDistributionHandler().submitServiceEvent(new ServiceOnMessageDataReceivedEvent(this.getWorkingChannelContext(), contentEntity.getData()));
			}
			else if(packetEntity.getPacketDataType() == PacketDataType.FILE)
			{
				FileEntity fileEntity = (FileEntity) packetEntity.getContentEntity();
				this.getEventDistributionHandler().submitServiceEvent(new ServerOnFileDataReceivedEvent(this.getWorkingChannelContext(), fileEntity.getFileID()));
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			result = false;
		}
		return new WorkingChannelOperationResult(result);
	}
	
	public synchronized WorkingChannelOperationResult writeMessageData(byte[] data){
		MessageEntity contentEntity = new MessageEntity();
		contentEntity.setData(data);
		contentEntity.setLength(data.length);
		PacketEntity packetEntity = new PacketEntity();
		packetEntity.setContentEntity(contentEntity);
		packetEntity.setPacketDataType(PacketDataType.MESSAGE);
		packetEntity.setLength(contentEntity.getLength() + 1 + 8);
		boolean result = false;
		try {
			result = writer.write((SocketChannel) this.getWorkingChannelContext().getChannel(), packetEntity);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			result = false;
		}
		return new WorkingChannelOperationResult(result);
	}
	
	public synchronized WorkingChannelOperationResult writeFileData(long fileID){
		boolean result = false;
		try {
			FileInformationEntity fileInformationEntity = NIOFileMessageMixStrategy.findFileInformationEntity(fileID);
			File file = new File(fileInformationEntity.getFilePath());
			FileEntity contentEntity = new FileEntity();
			contentEntity.setFileID(fileID);
			contentEntity.setLength(file.length() + 8);
			PacketEntity packetEntity = new PacketEntity();
			packetEntity.setContentEntity(contentEntity);
			packetEntity.setPacketDataType(PacketDataType.FILE);
			packetEntity.setLength(file.length() + 1 + 8 + 8);
			result = this.writer.write((SocketChannel) this.getWorkingChannelContext().getChannel(), 
					packetEntity);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			result = false;
		}
		return new WorkingChannelOperationResult(result);
	}

	@Override
	public WorkingChannelOperationResult writeChannel() {
		return null;
	}

	@Override
	public void clear() {
	}
}