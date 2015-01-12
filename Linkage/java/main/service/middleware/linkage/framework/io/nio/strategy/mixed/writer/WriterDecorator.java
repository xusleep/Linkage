package service.middleware.linkage.framework.io.nio.strategy.mixed.writer;

public abstract class WriterDecorator implements WriterInterface {
	private WriterInterface wrappedWriter;

	public WriterDecorator(WriterInterface wrappedWriter) {
		super();
		this.wrappedWriter = wrappedWriter;
	}

	public WriterInterface getWrappedWriter() {
		return wrappedWriter;
	}

	public void setWrappedWriter(WriterInterface wrappedWriter) {
		this.wrappedWriter = wrappedWriter;
	}
}
