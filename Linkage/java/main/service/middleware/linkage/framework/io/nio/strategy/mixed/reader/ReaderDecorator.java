package service.middleware.linkage.framework.io.nio.strategy.mixed.reader;

/**
 * abstract decorator for the reader
 * @author zhonxu
 *
 */
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
