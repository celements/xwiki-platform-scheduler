/*
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package com.xpn.xwiki.plugin.lucene.searcherProvider;

import static com.google.common.base.Preconditions.*;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.lucene.search.Searcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xpn.xwiki.plugin.lucene.SearchResults;

public class SearcherProvider {

  private static final Logger LOGGER = LoggerFactory.getLogger(SearcherProvider.class);

  /**
   * List of Lucene indexes used for searching. By default there is only one such index
   * for all the wiki. One searches is created for each entry in {@link #indexDirs}.
   */
  private final Searcher[] backedSearchers;

  private final AtomicBoolean markToClose;

  private volatile boolean isClosed;

  private final Set<Long> connectedThreads;

  private final ConcurrentMap<Long, Set<SearchResults>> connectedSearchResultsMap;

  SearcherProvider(Searcher[] searchers) {
    this.backedSearchers = searchers;
    this.markToClose = new AtomicBoolean(false);
    this.connectedThreads = Collections.newSetFromMap(new ConcurrentHashMap<Long, Boolean>());
    this.connectedSearchResultsMap = new ConcurrentHashMap<Long, Set<SearchResults>>();
    LOGGER.debug("create searcherProvider: [" + System.identityHashCode(this) + "].");
  }

  ConcurrentMap<Long, Set<SearchResults>> internal_getConnectedSearchResults() {
    return connectedSearchResultsMap;
  }

  Set<Long> internal_getConnectedThreads() {
    return connectedThreads;
  }

  /**
   * <code>connect</code> is implemented with a fail-fast behavior. It may happen that a thread
   * succeeds to connect on a markedToClose SearcherProvider. The guarantee is, that the
   * SearcherProvider will not close the connected lucene searchers before not all threads
   * finished with theirs SearcherResults and disconnected.
   */
  public void connect() {
    if (!checkConnected()) {
      synchronized (this) {
        checkState(!isMarkedToClose(), "you connected to a SearchProvider marked to close.");
        LOGGER.debug("connect searcherProvider [{}] to [{}].", System.identityHashCode(this),
            getThreadKey());
        connectedThreads.add(getThreadKey());
      }
    }
  }

  public boolean isClosed() {
    return this.isClosed;
  }

  public Searcher[] getSearchers() {
    checkState(!isClosed(), "Getting searchers failed: provider is closed.");
    checkState(checkConnected(), "you must connect to the searcher provider before you can get"
        + " any searchers");
    return backedSearchers;
  }

  private boolean checkConnected() {
    return connectedThreads.contains(getThreadKey());
  }

  public void disconnect() throws IOException {
    if (connectedThreads.remove(getThreadKey())) {
      LOGGER.debug("disconnect searcherProvider [{}] to [{}], markedToClose [{}].",
          System.identityHashCode(this), getThreadKey(), isMarkedToClose());
      closeIfIdle();
    }
  }

  public boolean isMarkedToClose() {
    return markToClose.get();
  }

  public void markToClose() throws IOException {
    if (!markToClose.getAndSet(true)) {
      LOGGER.debug("markToClose searcherProvider [{}].", System.identityHashCode(this));
      closeIfIdle();
    }
  }

  private synchronized void closeIfIdle() throws IOException {
    if (canBeClosed()) {
      closeSearchers();
    }
  }

  boolean canBeClosed() {
    return isMarkedToClose() && isIdle();
  }

  public boolean isIdle() {
    return connectedThreads.isEmpty() && connectedSearchResultsMap.isEmpty();
  }

  /**
   * @throws IOException
   */
  void closeSearchers() throws IOException {
    if (!isClosed()) {
      LOGGER.debug("closeSearchers: for [{}].", System.identityHashCode(this));
      for (Searcher searcher : backedSearchers) {
        if (searcher != null) {
          searcher.close();
        }
      }
      isClosed = true;
    }
  }

  @Override
  protected void finalize() throws Throwable {
    super.finalize();
    closeSearchers();
  }

  public void connectSearchResults(SearchResults searchResults) {
    checkState(checkConnected(), "you may not connect a searchResult to a SearchProvider from"
        + " a not connected thread.");
    getConnectedSearchResultsForCurrentThread().add(searchResults);
  }

  Set<SearchResults> getConnectedSearchResultsForCurrentThread() {
    connectedSearchResultsMap.putIfAbsent(getThreadKey(), new HashSet<SearchResults>());
    return connectedSearchResultsMap.get(getThreadKey());
  }

  public boolean hasSearchResultsForCurrentThread() {
    return connectedSearchResultsMap.containsKey(getThreadKey());
  }

  public void cleanUpAllSearchResultsForThread() throws IOException {
    if (connectedSearchResultsMap.remove(getThreadKey()) != null) {
      closeIfIdle();
    }
  }

  public Long getThreadKey() {
    return Thread.currentThread().getId();
  }

  public void cleanUpSearchResults(SearchResults searchResults) throws IOException {
    if (hasSearchResultsForCurrentThread()) {
      Set<SearchResults> currentThreadSet = getConnectedSearchResultsForCurrentThread();
      if (currentThreadSet.remove(searchResults)) {
        if (currentThreadSet.isEmpty()) {
          connectedSearchResultsMap.remove(getThreadKey());
        }
        closeIfIdle();
      }
    }
  }

}
