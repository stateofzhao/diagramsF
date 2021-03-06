/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.volley;

import com.android.volley.toolbox.DiskBasedCache;
import java.util.Collections;
import java.util.Map;

/**
 * Volley对于缓存的处理比较特殊，它没有直接借助于网络实现部分的缓存（例如，使用okhttp库自带的缓存功能），
 * 而是直接实现了自己单独的缓存，并且这个单独实现的缓存还是会自己来读取结果的http报头信息，如果过去还是会重新请求网络的。
 * <P>
 * 我们可以看一下Volley自带的一个实现类是{@link DiskBasedCache},这个是作为磁盘缓存而存在的（并不是内存缓存），
 * 所以这里就有问题了，后续优化的话，可以自己实现一个缓存类，但是这个缓存类只作为内存缓存，
 * 而磁盘缓存我们可以直接在{@link Network#performRequest(Request)}中实现（一般网络库都会自带磁盘缓存的，所以一般不需要自己来实现）
 * <P>
 *
 * An interface for a cache keyed by a String with a byte array as data.
 */
public interface Cache {
  /**
   * Retrieves an entry from the cache.
   *
   * @param key Cache key
   * @return An {@link Entry} or null in the event of a cache miss
   */
  public Entry get(String key);

  /**
   * Adds or replaces an entry to the cache.
   *
   * @param key Cache key
   * @param entry Data to store and metadata for cache coherency, TTL, etc.
   */
  public void put(String key, Entry entry);

  /**
   * Performs any potentially long-running actions needed to initialize the cache;
   * will be called from a worker thread.
   */
  public void initialize();

  /**
   * Invalidates an entry in the cache.
   *
   * @param key Cache key
   * @param fullExpire True to fully expire the entry, false to soft expire
   */
  public void invalidate(String key, boolean fullExpire);

  /**
   * Removes an entry from the cache.
   *
   * @param key Cache key
   */
  public void remove(String key);

  /**
   * Empties the cache.
   */
  public void clear();

  /**
   * 缓存的数据和元数据
   *
   *
   * Data and metadata for an entry returned by the cache.
   */
  public static class Entry {
    /** The data returned from cache. */
    public byte[] data;

    /** ETag for cache coherency. */
    public String etag;

    /** Date of this response as reported by the server. */
    public long serverDate;

    /** TTL for this record. */
    public long ttl;

    /** Soft TTL for this record. */
    public long softTtl;

    /** Immutable response headers as received from server; must be non-null. */
    public Map<String, String> responseHeaders = Collections.emptyMap();

    /** True if the entry is expired. */
    public boolean isExpired() {
      return this.ttl < System.currentTimeMillis();
    }

    /** True if a refresh is needed from the original data source. */
    public boolean refreshNeeded() {
      return this.softTtl < System.currentTimeMillis();
    }
  }
}
