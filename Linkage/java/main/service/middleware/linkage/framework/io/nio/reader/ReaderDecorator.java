package service.middleware.linkage.framework.io.nio.reader;


public abstract class ReaderDecorator implements ReaderInterface {
	private ReaderInterface wrappedReader;
	
	public ReaderDecorator(ReaderInterface wrappedReader){
		this.wrappedReader = wrappedReader;
	}

	public ReaderInterface getWrappedReader() {
		return wrappedReader;
	}

	public void setWrappedReader(ReaderInterface wrappedReader) {
		this.wrappedReader = wrappedReader;
	}
}
