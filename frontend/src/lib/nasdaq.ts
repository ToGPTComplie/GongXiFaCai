import nasdaqData from "../data/nasdaq.json";

export interface NasdaqSecurity {
  symbol: string;
  name: string;
  logoUrl: string;
}

const securities = (nasdaqData as NasdaqSecurity[]).map((entry) => ({
  symbol: entry.symbol.trim().toUpperCase(),
  name: entry.name.trim(),
  logoUrl: entry.logoUrl,
}));

const securitiesBySymbol = new Map(securities.map((entry) => [entry.symbol, entry]));

function getMatchScore(entry: NasdaqSecurity, normalizedQuery: string) {
  const symbol = entry.symbol.toLowerCase();
  const name = entry.name.toLowerCase();

  if (symbol === normalizedQuery) {
    return 0;
  }

  if (symbol.startsWith(normalizedQuery)) {
    return 1;
  }

  if (name.startsWith(normalizedQuery)) {
    return 2;
  }

  if (symbol.includes(normalizedQuery)) {
    return 3;
  }

  if (name.includes(normalizedQuery)) {
    return 4;
  }

  return Number.POSITIVE_INFINITY;
}

export function findNasdaqSecurityBySymbol(symbol: string) {
  return securitiesBySymbol.get(symbol.trim().toUpperCase()) ?? null;
}

export function inferAssetTypeFromSecurityName(name: string): "STOCK" | "BOND" {
  return name.toLowerCase().includes("ishares") ? "BOND" : "STOCK";
}

export function searchNasdaqSecurities(query: string, limit = 8) {
  const normalizedQuery = query.trim().toLowerCase();

  if (!normalizedQuery) {
    return [];
  }

  return securities
    .map((entry) => ({
      entry,
      score: getMatchScore(entry, normalizedQuery),
    }))
    .filter((candidate) => Number.isFinite(candidate.score))
    .sort((left, right) => {
      if (left.score !== right.score) {
        return left.score - right.score;
      }

      return left.entry.symbol.localeCompare(right.entry.symbol);
    })
    .slice(0, limit)
    .map((candidate) => candidate.entry);
}
