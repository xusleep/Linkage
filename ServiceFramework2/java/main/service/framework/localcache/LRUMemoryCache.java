package service.framework.localcache;

import java.io.IOException;
import java.io.Serializable;
import service.framework.utils.struct.DoubleLinkedList;

public class LRUMemoryCache extends AbstractMemoryCache {
	/** thread-safe double linked list for lru */
	private DoubleLinkedList list;
	private int maxSize;
	private static final IMemoryCache instance = new LRUMemoryCache(2000);

	private LRUMemoryCache(int maxSize) {
		super();
		list = new DoubleLinkedList();
		this.maxSize = maxSize;
	}
	
	public static IMemoryCache getInstance(){
		return instance;
	}

	@Override
	public int freeElements(int numberToFree) throws IOException {
		// TODO Auto-generated method stub
		int count = this.map.size();
		this.map.clear();
		return count;
	}

	@Override
	public boolean remove(Serializable key) throws IOException {
		ICacheElement ce = (ICacheElement) this.map.remove(key);
		return ce != null;
	}

	@Override
	public ICacheElement get(Serializable key) throws IOException {
		ICacheElement ce = null;
		MemoryElementDescriptor me = (MemoryElementDescriptor) map.get(key);

		if (me != null) {
			ce = me.ce;

			list.makeFirst(me);
		}

		return ce;
	}

	@Override
	public ICacheElement getQuiet(Serializable key) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void update(ICacheElement ce) throws IOException {
		MemoryElementDescriptor old = null;
		synchronized (this) {
			// TODO address double synchronization of addFirst, use write lock
			addFirst(ce);
			// this must be synchronized
			old = (MemoryElementDescriptor) map.put(
					((MemoryElementDescriptor) list.getFirst()).ce.getKey(),
					list.getFirst());
			// If the node was the same as an existing node, remove it.
			if (old != null
					&& ((MemoryElementDescriptor) list.getFirst()).ce.getKey()
							.equals(old.ce.getKey())) {
				list.remove(old);
			}
			if (this.map.size() == maxSize) {
				spoolLastElement();
			}
		}
	}

	protected ICacheElement spoolLastElement() throws Error {
		ICacheElement toSpool = null;
		synchronized (this) {
			if (list.getLast() != null) {
				toSpool = ((MemoryElementDescriptor) list.getLast()).ce;
				this.map.remove(toSpool.getKey());
				list.removeLast();
			}
		}
		return toSpool;
	}

	/**
	 * Adds a new node to the start of the link list.
	 * <p>
	 * 
	 * @param ce
	 *            The feature to be added to the First
	 */
	private synchronized void addFirst(ICacheElement ce) {
		MemoryElementDescriptor me = new MemoryElementDescriptor(ce);
		list.addFirst(me);
	}
}
