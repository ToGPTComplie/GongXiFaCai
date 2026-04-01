import type { WatchlistItem } from "./types";

const watchlistCache = new Map<string, WatchlistItem[]>();

export function readWatchlistCache(userId: string) {
  return watchlistCache.get(userId) ?? null;
}

export function writeWatchlistCache(userId: string, items: WatchlistItem[]) {
  watchlistCache.set(userId, items);
}

export function invalidateWatchlistCache(userId: string) {
  watchlistCache.delete(userId);
}
