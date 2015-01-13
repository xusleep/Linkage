package service.middleware.linkage.framework.io.nio.strategy.mixed;

import java.io.File;
import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.Enumeration;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.log4j.Logger;

import service.middleware.linkage.framework.FileInformationStorageList;
import service.middleware.linkage.framework.exception.ServiceException;
import service.middleware.linkage.framework.exception.ServiceOnChanelIOException;
import service.middleware.linkage.framework.handlers.EventDistributionMaster;
import service.middleware.linkage.framework.handlers.ServiceExeptionEvent;
import service.middleware.linkage.framework.io.WorkingChannelOperationResult;
import service.middleware.linkage.framework.io.WorkingChannelStrategy;
import service.middleware.linkage.framework.io.nio.NIOWorkingChannelContext;
import service.middleware.linkage.framework.io.nio.strategy.mixed.events.ServiceOnFileDataReceivedEvent;
import service.middleware.linkage.framework.io.nio.strategy.mixed.events.ServiceOnFileDataWriteEvent;
import service.middleware.linkage.framework.io.nio.strategy.mixed.events.ServiceOnMessageDataReceivedEvent;
import service.middleware.linkage.framework.io.nio.strategy.mixed.events.ServiceOnMessageDataWriteEvent;
import service.middleware.linkage.framework.io.nio.strategy.mixed.packet.FileEntity;
import service.middleware.linkage.framework.io.nio.strategy.mixed.packet.FileInformationEntity;
import service.middleware.linkage.framework.io.nio.strategy.mixed.packet.MessageEntity;
import service.middleware.linkage.framework.io.nio.strategy.mixed.packet.PacketDataType;
import service.middleware.linkage.framework.io.nio.strategy.mixed.packet.PacketEntity;
import service.middleware.linkage.framework.io.nio.strategy.mixed.reader.DataTypeReader;
import service.middleware.linkage.framework.io.nio.strategy.mixed.reader.FileDataReader;
import service.middleware.linkage.framework.io.nio.strategy.mixed.reader.MessageDataReader;
import service.middleware.linkage.framework.io.nio.strategy.mixed.reader.PacketReader;
import service.middleware.linkage.framework.io.nio.strategy.mixed.reader.ReaderInterface;
import service.middleware.linkage.framework.io.nio.strategy.mixed.writer.DataTypeWriter;
import service.middleware.linkage.framework.io.nio.strategy.mixed.writer.FileDataWriter;
import service.middleware.linkage.framework.io.nio.strategy.mixed.writer.MessageDataWriter;
import service.middleware.linkage.framework.io.nio.strategy.mixed.writer.PacketWriter;
import service.middleware.linkage.framework.io.nio.strategy.mixed.writer.WriterInterface;
import service.middleware.linkage.framework.serviceaccess.entity.RequestResultEntity;
import service.middleware.linkage.framework.serviceaccess.entity.ResponseEntity;

/**
 * this strategy is only used for the mixed mode only
 * the message and file will stored in the packet
 * @author zhonxu
 *
 */
public class NIOMixedStrategy extends WorkingChannelStrategy {
	
	// Monitor object for write.
	final public Object writeLock = new Object();
	// Monitor object for read.
	final public Object readLock = new Object();
	// writer for writting the channel
	private WriterInterface writer;
	// reader for reading the channel
	private ReaderInterface reader;
	// event distribution master 
	private final EventDistributionMaster eventDistributionHandler;
	// write file event queue
	private Queue<ServiceOnFileDataWriteEvent> writeFileQueue = new ConcurrentLinkedQueue<ServiceOnFileDataWriteEvent>();
	// write message event queue
	private Queue<ServiceOnMessageDataWriteEvent> writeMessageQueue = new ConcurrentLinkedQueue<ServiceOnMessageDataWriteEvent>();
	private static Logger logger = Logger.getLogger(NIOMixedStrategy.class);
	
	public NIOMixedStrategy(NIOWorkingChannelContext nioWorkingChannelContext,
			EventDistributionMaster eventDistributionHandler) {
		super(nioWorkingChannelContext);
		this.eventDistributionHandler = eventDistributionHandler;
		MessageDataWriter messageWriter = new MessageDataWriter(null);
		FileDataWriter fileWriter = new FileDataWriter(null);
		DataTypeWriter dataWriter = new DataTypeWriter(messageWriter, fileWriter);
		writer = new PacketWriter(dataWriter);
		
		MessageDataReader messageReader = new MessageDataReader(null);
		FileDataReader fileReader = new FileDataReader(null);
		DataTypeReader dataReader = new DataTypeReader(messageReader, fileReader);
		reader = new PacketReader(dataReader);
	}
	
	/**
	 * offer writer message event to the write queue
	 * @param serviceOnMessageDataWriteEvent
	 */
	public void offerMessageWriteQueue(ServiceOnMessageDataWriteEvent serviceOnMessageDataWriteEvent) {
		this.writeMessageQueue.offer(serviceOnMessageDataWriteEvent);
	}
	
	/**
	 * offer write file event to the write queue
	 * @param ServiceOnFileDataWriteEvent
	 */
	public void offerFileWriteQueue(ServiceOnFileDataWriteEvent serviceOnFileDataWriteEvent) {
		synchronized(this.writeLock){
			this.writeFileQueue.offer(serviceOnFileDataWriteEvent);
		}
	}
	
	

	@Override
	public  WorkingChannelOperationResult readChannel() {
		
		PacketEntity packetEntity = new PacketEntity();
		boolean result = false;
		try {
			synchronized(this.readLock)
			{
				result = reader.read((SocketChannel) this.getWorkingChannelContext().getChannel(), packetEntity);
			}
			if(packetEntity.getPacketDataType() == PacketDataType.MESSAGE)
			{
				MessageEntity contentEntity = (MessageEntity) packetEntity.getContentEntity();
				this.getEventDistributionHandler().submitServiceEvent(new ServiceOnMessageDataReceivedEvent(this.getWorkingChannelContext(), contentEntity.getData()));
			}
			else if(packetEntity.getPacketDataType() == PacketDataType.FILE)
			{
				FileEntity fileEntity = (FileEntity) packetEntity.getContentEntity();
				this.getEventDistributionHandler().submitServiceEvent(new ServiceOnFileDataReceivedEvent(this.getWorkingChannelContext(), fileEntity.getFileID()));
			}
		} catch (IOException e) {
			this.getEventDistributionHandler().submitServiceEvent(new ServiceExeptionEvent(this.getWorkingChannelContext(), 
					null, new ServiceOnChanelIOException(e, e.getMessage())));
			result = false;
		}
		return new WorkingChannelOperationResult(result);
	}
	
	@Override
	public WorkingChannelOperationResult writeChannel() {
		ServiceOnMessageDataWriteEvent messageEvent;
		synchronized (this.writeLock) {
			for (;;) {
				if ((messageEvent = writeMessageQueue.poll()) == null) {
					break;
				}
				WorkingChannelOperationResult writeResult = this.writeMessageData(messageEvent.getMessageData());
				if(!writeResult.isSuccess())
					return writeResult;
			}
		}
		ServiceOnFileDataWriteEvent fileEvent;
		synchronized (this.writeLock) {
			for (;;) {
				if ((fileEvent = writeFileQueue.poll()) == null) {
					break;
				}
				WorkingChannelOperationResult writeResult = this.writeFileData(fileEvent.getFileID());
				if(!writeResult.isSuccess())
					return writeResult;
			}
		}
		return new WorkingChannelOperationResult(true);
	}

	
	/**
	 * write message data to the channel 
	 * @param data
	 * @return
	 */
	private WorkingChannelOperationResult writeMessageData(byte[] data){
		MessageEntity contentEntity = new MessageEntity();
		contentEntity.setData(data);
		contentEntity.setLength(data.length);
		PacketEntity packetEntity = new PacketEntity();
		packetEntity.setContentEntity(contentEntity);
		packetEntity.setPacketDataType(PacketDataType.MESSAGE);
		packetEntity.setLength(contentEntity.getLength() + 1 + 8);
		boolean result = false;
		try {
			synchronized(this.writeLock)
			{
				result = writer.write((SocketChannel) this.getWorkingChannelContext().getChannel(), packetEntity);
			}
		} catch (IOException e) {
			this.getEventDistributionHandler().submitServiceEvent(new ServiceExeptionEvent(this.getWorkingChannelContext(), 
					null, new ServiceOnChanelIOException(e, e.getMessage())));
			result = false;
		}
		return new WorkingChannelOperationResult(result);
	}
	
	/**
	 * write the file data into the channel
	 * @param fileID
	 * @return
	 */
	private WorkingChannelOperationResult writeFileData(long fileID){
		boolean result = false;
		try {
			FileInformationEntity fileInformationEntity = FileInformationStorageList.findFileInformationEntity(fileID);
			File file = new File(fileInformationEntity.getFilePath());
			FileEntity contentEntity = new FileEntity();
			contentEntity.setFileID(fileID);
			contentEntity.setLength(file.length() + 8);
			PacketEntity packetEntity = new PacketEntity();
			packetEntity.setContentEntity(contentEntity);
			packetEntity.setPacketDataType(PacketDataType.FILE);
			packetEntity.setLength(file.length() + 1 + 8 + 8);
			synchronized(this.writeLock)
			{
				result = this.writer.write((SocketChannel) this.getWorkingChannelContext().getChannel(), 
						packetEntity);
			}
		} catch (IOException e) {
			this.getEventDistributionHandler().submitServiceEvent(new ServiceExeptionEvent(this.getWorkingChannelContext(), 
					null, new ServiceOnChanelIOException(e, e.getMessage())));
			result = false;
		}
		return new WorkingChannelOperationResult(result);
	}
	
	/**
	 * get event distribution handler
	 * @return
	 */
	public EventDistributionMaster getEventDistributionHandler() {
		return eventDistributionHandler;
	}
}
