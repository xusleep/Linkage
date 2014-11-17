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

import java.io.Serializable;

/**
 * Generic element wrapper. Often stuffed inside another.
 */
public class CacheElement
    implements ICacheElement, Serializable
{
    private static final long serialVersionUID = -6062305728297627263L;

    /** This is the cache key by which the value can be referenced. */
    public final Serializable key;

    /** This is the cached value, reference by the key. */
    public final Serializable val;

    /**
     * Constructor for the CacheElement object
     * <p>
     * @param cacheName
     * @param key
     * @param val
     */
    public CacheElement( Serializable key, Serializable val )
    {
        this.key = key;
        this.val = val;
    }


    /**
     * Gets the key attribute of the CacheElement object
     * <p>
     * @return The key value
     */
    public Serializable getKey()
    {
        return this.key;
    }

    /**
     * Gets the val attribute of the CacheElement object
     * <p>
     * @return The val value
     */
    public Serializable getVal()
    {
        return this.val;
    }

  
    /**
     * @return a hash of the key only
     */
    public int hashCode()
    {
        return key.hashCode();
    }


	@Override
	public String getCacheName() {
		// TODO Auto-generated method stub
		return null;
	}
}
