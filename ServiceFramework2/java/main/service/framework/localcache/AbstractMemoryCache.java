package service.framework.localcache;

import java.io.IOException;
import java.io.Serializable;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public abstract class AbstractMemoryCache implements IMemoryCache {
	private final static Log log = LogFactory.getLog(AbstractMemoryCache.class);

	private static final int DEFAULT_CHUNK_SIZE = 2;

	/** The region name. This defines a namespace of sorts. */
	protected String cacheName;

	/**
	 * Map where items are stored by key
	 */
	protected Map map;

	/** status */
	protected int status;

	/** How many to spool at a time. TODO make configurable */
	protected int chunkSize = DEFAULT_CHUNK_SIZE;

	/**
	 * Constructor for the LRUMemoryCache object
	 */
	public AbstractMemoryCache() {
		map = new ConcurrentHashMap(16);
	}

	/**
	 * Removes an item from the cache
	 * 
	 * @param key
	 *            Identifies item to be removed
	 * @return Description of the Return Value
	 * @exception IOException
	 *                Description of the Exception
	 */
	public abstract boolean remove(Serializable key) throws IOException;

	/**
	 * Get an item from the cache
	 * 
	 * @param key
	 *            Description of the Parameter
	 * @return Description of the Return Value
	 * @exception IOException
	 *                Description of the Exception
	 */
	public abstract ICacheElement get(Serializable key) throws IOException;

	/**
	 * Get an item from the cache without affecting its order or last access
	 * time
	 * 
	 * @param key
	 *            Description of the Parameter
	 * @return The quiet value
	 * @exception IOException
	 *                Description of the Exception
	 */
	public abstract ICacheElement getQuiet(Serializable key) throws IOException;

	/**
	 * Puts an item to the cache.
	 * 
	 * @param ce
	 *            Description of the Parameter
	 * @exception IOException
	 *                Description of the Exception
	 */
	public abstract void update(ICacheElement ce) throws IOException;

	/**
	 * Removes all cached items from the cache.
	 * 
	 * @exception IOException
	 */
	public void removeAll() throws IOException {
		map = new ConcurrentHashMap(16);
	}

	/**
	 * Returns the current cache size.
	 * 
	 * @return The size value
	 */
	public int getSize() {
		return this.map.size();
	}

	/**
	 * Returns the cache status.
	 * 
	 * @return The status value
	 */
	public int getStatus() {
		return this.status;
	}

	/**
	 * Returns the cache name.
	 * 
	 * @return The cacheName value
	 */
	public String getCacheName() {
		return this.cacheName;
	}

	/**
	 * Gets the iterator attribute of the LRUMemoryCache object
	 * 
	 * @return The iterator value
	 */
	public Iterator getIterator() {
		return map.entrySet().iterator();
	}
}
