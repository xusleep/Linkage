package service.middleware.linkage.framework.io.nio;

public class PacketEntity extends ContentEntity {
	private PacketDataType packetDataType; 
	private ContentEntity contentEntity;
	
	public PacketDataType getPacketDataType() {
		return packetDataType;
	}
	public void setPacketDataType(PacketDataType packetDataType) {
		this.packetDataType = packetDataType;
	}
	public ContentEntity getContentEntity() {
		return contentEntity;
	}
	public void setContentEntity(ContentEntity contentEntity) {
		this.contentEntity = contentEntity;
	}
	
	
}
