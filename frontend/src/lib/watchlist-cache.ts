import type { WatchlistItem } from "./types";

const watchlistCache = new Map<string, WatchlistItem[]>();
const watchlistListeners = new Map<string, Set<() => void>>();

function emitWatchlistCacheChange(userId: string) {
  const listeners = watchlistListeners.get(userId);

  if (!listeners) {
    return;
  }

  listeners.forEach((listener) => listener());
}

export function readWatchlistCache(userId: string) {
  return watchlistCache.get(userId) ?? null;
}

export function writeWatchlistCache(userId: string, items: WatchlistItem[]) {
  watchlistCache.set(userId, items);
  emitWatchlistCacheChange(userId);
}

export function invalidateWatchlistCache(userId: string) {
  watchlistCache.delete(userId);
  emitWatchlistCacheChange(userId);
}

export function subscribeWatchlistCache(userId: string, listener: () => void) {
  const listeners = watchlistListeners.get(userId) ?? new Set<() => void>();
  listeners.add(listener);
  watchlistListeners.set(userId, listeners);

  return () => {
    const currentListeners = watchlistListeners.get(userId);

    if (!currentListeners) {
      return;
    }

    currentListeners.delete(listener);

    if (currentListeners.size === 0) {
      watchlistListeners.delete(userId);
    }
  };
}
