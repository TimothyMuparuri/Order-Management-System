import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, catchError, map, of } from 'rxjs';
import { environment } from '../../environments/environment';
import { DEAL_IDS_IN_ORDER, fallbackImageFor } from './deal-image-fallbacks';
import { DEALS, Deal, STORES, Store } from './storefront.data';

export interface CatalogDeal extends Deal {
  id?: string;
  imageUrl?: string | null;
  scrapeStatus?: string;
  lastScrapedAt?: string;
}

export interface CatalogStore extends Store {
  id?: string;
  imageUrl?: string | null;
  scrapeStatus?: string;
}

export interface LiveSearchResult {
  id: string;
  store: string;
  name: string;
  url: string;
  imageUrl?: string | null;
  price?: number | null;
  was?: number | null;
  category?: string;
  badge?: string | null;
  source?: 'catalog' | 'live' | string;
}

export interface LiveSearchResponse {
  query: string;
  results: LiveSearchResult[];
  storesQueried: string[];
  storesOk: string[];
  storesFailed: string[];
  note: string;
}

@Injectable({ providedIn: 'root' })
export class CatalogService {
  private readonly http = inject(HttpClient);
  private readonly base = environment.apiBaseUrl;

  loadDeals(): Observable<CatalogDeal[]> {
    return this.http.get<{ deals: CatalogDeal[] }>(`${this.base}/api/catalog/deals`).pipe(
      map((res) => (res.deals ?? []).map((d) => this.withImage(d))),
      catchError(() =>
        of(
          DEALS.map((d, i) =>
            this.withImage({
              ...d,
              id: DEAL_IDS_IN_ORDER[i] || `local-${i}`,
              imageUrl: null,
            })
          )
        )
      )
    );
  }

  loadStores(): Observable<CatalogStore[]> {
    return this.http.get<{ stores: CatalogStore[] }>(`${this.base}/api/catalog/stores`).pipe(
      map((res) =>
        (res.stores ?? []).map((s) => ({
          ...s,
          imageUrl: s.imageUrl ? this.absolute(s.imageUrl) : null,
        }))
      ),
      catchError(() => of(STORES.map((s) => ({ ...s, imageUrl: null }))))
    );
  }

  triggerScrape(): Observable<unknown> {
    return this.http.post(`${this.base}/api/catalog/scrape`, {});
  }

  /** Live store previews + curated catalog fallback (stays on this site). */
  liveSearch(query: string): Observable<LiveSearchResponse> {
    const q = query.trim();
    if (!q) {
      return of({
        query: '',
        results: [],
        storesQueried: [],
        storesOk: [],
        storesFailed: [],
        note: '',
      });
    }
    return this.http
      .get<LiveSearchResponse>(`${this.base}/api/catalog/search`, { params: { q } })
      .pipe(
        map((res) => ({
          ...res,
          results: (res.results ?? []).map((r) => ({
            ...r,
            imageUrl: r.imageUrl
              ? r.imageUrl.startsWith('http')
                ? r.imageUrl
                : this.absolute(r.imageUrl)
              : fallbackImageFor({ id: r.id?.replace(/^catalog-/, ''), category: r.category, name: r.name }),
          })),
        })),
        catchError(() =>
          of({
            query: q,
            results: [],
            storesQueried: [],
            storesOk: [],
            storesFailed: [],
            note: 'Live search unavailable — showing on-site deals only.',
          })
        )
      );
  }

  private withImage(deal: CatalogDeal): CatalogDeal {
    // Only trust scraped files marked OK; otherwise use standby photos so the UI stays filled.
    const scraped =
      deal.imageUrl && (!deal.scrapeStatus || deal.scrapeStatus === 'OK')
        ? this.absolute(deal.imageUrl)
        : null;
    return {
      ...deal,
      imageUrl: scraped || fallbackImageFor(deal),
    };
  }

  private absolute(path: string): string {
    if (path.startsWith('http')) {
      return path;
    }
    return `${this.base}${path.startsWith('/') ? '' : '/'}${path}`;
  }
}
