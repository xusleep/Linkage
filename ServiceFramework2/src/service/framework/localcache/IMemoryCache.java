package service.framework.localcache;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import java.io.IOException;
import java.io.Serializable;

/**
 * For the framework. Insures methods a MemoryCache needs to access.
 *
 */
public interface IMemoryCache
{
    /**
     * Get the number of elements contained in the memory store
     * <p>
     * @return Element count
     */
    public int getSize();

    /**
     * Removes an item from the cache
     * <p>
     * @param key
     *            Identifies item to be removed
     * @return Description of the Return Value
     * @exception IOException
     *                Description of the Exception
     */
    public boolean remove( Serializable key )
        throws IOException;

    /**
     * Removes all cached items from the cache.
     * <p>
     * @exception IOException
     *                Description of the Exception
     */
    public void removeAll()
        throws IOException;

    /**
     * This instructs the memory cache to remove the <i>numberToFree</i>
     * according to its eviction policy. For example, the LRUMemoryCache will
     * remove the <i>numberToFree</i> least recently used items. These will be
     * spooled to disk if a disk auxiliary is available.
     * <p>
     * @param numberToFree
     * @return the number that were removed. if you ask to free 5, but there are
     *         only 3, you will get 3.
     * @throws IOException
     */
    public int freeElements( int numberToFree )
        throws IOException;

    /**
     * Get an item from the cache
     * <p>
     * @param key
     *            Description of the Parameter
     * @return Description of the Return Value
     * @exception IOException
     *                Description of the Exception
     */
    public ICacheElement get( Serializable key )
        throws IOException;

    /**
     * Puts an item to the cache.
     * <p>
     * @param ce
     *            Description of the Parameter
     * @exception IOException
     *                Description of the Exception
     */
    public void update( ICacheElement ce )
        throws IOException;
}
