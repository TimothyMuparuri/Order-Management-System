import { Component, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import {
  CatalogService,
  CatalogStore,
  LiveSearchResult,
} from '../../core/catalog.service';
import {
  SEARCH_CATEGORIES,
  STORE_TABS,
  STORES,
  Store,
} from '../../core/storefront.data';
import { OrderCartService } from '../../core/order-cart.service';

type StoreTab = (typeof STORE_TABS)[number];

@Component({
  selector: 'app-stores',
  standalone: true,
  imports: [FormsModule, RouterLink],
  templateUrl: './stores.component.html',
  styleUrl: './stores.component.css',
})
export class StoresComponent {
  private readonly catalog = inject(CatalogService);
  private readonly cart = inject(OrderCartService);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);

  readonly storeTabs = STORE_TABS;
  readonly searchCategories = SEARCH_CATEGORIES;

  readonly stores = signal<CatalogStore[]>(STORES.map((s) => ({ ...s, imageUrl: null })));
  readonly storeTab = signal<StoreTab>('All');
  readonly storeQuery = signal('');
  storeFilter = '';

  readonly searchCategory = signal('all');
  productQuery = '';
  readonly liveResults = signal<LiveSearchResult[]>([]);
  readonly liveNote = signal('');
  readonly liveLoading = signal(false);

  readonly visibleStores = computed(() => {
    const tab = this.storeTab();
    const q = this.storeQuery().trim().toLowerCase();
    return this.stores().filter((s) => {
      if (tab !== 'All' && s.category !== tab) return false;
      if (!q) return true;
      const hay = `${s.name} ${s.meta ?? ''} ${s.category}`.toLowerCase();
      return hay.includes(q);
    });
  });

  readonly counts = computed(() => {
    const all = this.stores();
    const map: Record<string, number> = { All: all.length };
    for (const tab of STORE_TABS) {
      if (tab === 'All') continue;
      map[tab] = all.filter((s) => s.category === tab).length;
    }
    return map;
  });

  constructor() {
    this.catalog.loadStores().subscribe((stores) => {
      if (stores.length) {
        const byName = new Map(stores.map((s) => [s.name.toLowerCase(), s]));
        this.stores.set(
          STORES.map((seed) => {
            const live = byName.get(seed.name.toLowerCase());
            return live ? { ...seed, ...live, imageUrl: live.imageUrl ?? null } : { ...seed, imageUrl: null };
          })
        );
      }
    });

    this.route.queryParamMap.pipe(takeUntilDestroyed()).subscribe((params) => {
      const cat = (params.get('cat') ?? 'all').toLowerCase();
      const known = SEARCH_CATEGORIES.some((c) => c.id === cat) ? cat : 'all';
      this.searchCategory.set(known);

      const storeCat = params.get('storeCat') as StoreTab | null;
      if (storeCat && STORE_TABS.includes(storeCat)) {
        this.storeTab.set(storeCat);
      } else if (known !== 'all') {
        const mapped = this.mapSearchCatToStoreTab(known);
        if (mapped) this.storeTab.set(mapped);
      }

      const q = params.get('q')?.trim() ?? '';
      if (q) {
        this.productQuery = q;
        this.runProductSearch(q);
      }
    });
  }

  setStoreTab(tab: StoreTab): void {
    this.storeTab.set(tab);
    this.router.navigate([], {
      relativeTo: this.route,
      queryParams: { storeCat: tab === 'All' ? null : tab },
      queryParamsHandling: 'merge',
      replaceUrl: true,
    });
  }

  filterStores(): void {
    this.storeQuery.set(this.storeFilter);
  }

  setSearchCategory(id: string): void {
    this.searchCategory.set(id);
    const mapped = this.mapSearchCatToStoreTab(id);
    if (mapped) this.storeTab.set(mapped);
    else if (id === 'all') this.storeTab.set('All');

    this.router.navigate([], {
      relativeTo: this.route,
      queryParams: {
        cat: id === 'all' ? null : id,
        storeCat: mapped && mapped !== 'All' ? mapped : null,
      },
      queryParamsHandling: 'merge',
      replaceUrl: true,
    });

    if (this.productQuery.trim()) {
      this.runProductSearch(this.productQuery.trim());
    }
  }

  searchProducts(event?: Event): void {
    event?.preventDefault();
    const q = this.productQuery.trim();
    if (!q) return;
    this.router.navigate([], {
      relativeTo: this.route,
      queryParams: {
        q,
        cat: this.searchCategory() === 'all' ? null : this.searchCategory(),
      },
      queryParamsHandling: 'merge',
    });
    this.runProductSearch(q);
  }

  searchStore(store: Store): void {
    this.productQuery = store.name;
    this.searchCategory.set('all');
    this.router.navigate([], {
      relativeTo: this.route,
      queryParams: { q: store.name, cat: null },
      queryParamsHandling: 'merge',
    });
    this.runProductSearch(store.name);
    setTimeout(() => document.getElementById('store-results')?.scrollIntoView({ behavior: 'smooth' }), 50);
  }

  clearProductSearch(): void {
    this.productQuery = '';
    this.liveResults.set([]);
    this.liveNote.set('');
    this.router.navigate([], {
      relativeTo: this.route,
      queryParams: { q: null },
      queryParamsHandling: 'merge',
      replaceUrl: true,
    });
  }

  addLiveResult(item: LiveSearchResult): void {
    this.cart.addProduct({
      productName: item.name,
      store: item.store,
      productUrl: item.url,
      imageUrl: item.imageUrl || undefined,
      unitPrice: item.price != null ? Number(item.price) : undefined,
      category: item.category,
    });
    this.router.navigate(['/orders/new']);
  }

  categoryLabel(id = this.searchCategory()): string {
    return this.searchCategories.find((c) => c.id === id)?.label ?? 'All';
  }

  searchPlaceholder(): string {
    if (this.searchCategory() === 'all') {
      return 'Search products across stores…';
    }
    return `Search in ${this.categoryLabel()}…`;
  }

  initials(store: Store): string {
    const letters = store.name
      .replace(/[^A-Za-z ]/g, '')
      .split(' ')
      .filter(Boolean)
      .map((w) => w[0]);
    return (letters.join('').slice(0, 2) || store.name.slice(0, 2)).toUpperCase();
  }

  rand(value: number): string {
    const cents = value % 1 !== 0;
    return (
      'R' +
      value.toLocaleString('en-US', {
        minimumFractionDigits: cents ? 2 : 0,
        maximumFractionDigits: 2,
      })
    );
  }

  private runProductSearch(q: string): void {
    const cat = this.searchCategories.find((c) => c.id === this.searchCategory());
    const scoped = cat && cat.id !== 'all' && cat.hint ? `${q} ${cat.hint.split(' ')[0]}` : q;
    this.liveLoading.set(true);
    this.liveNote.set(`Searching ${cat?.label ?? 'all'} across partner stores…`);
    this.catalog.liveSearch(scoped).subscribe({
      next: (res) => {
        let results = res.results ?? [];
        if (cat && cat.id !== 'all') {
          const filtered = results.filter((r) => this.matchesSearchCategory(r, cat.id));
          if (filtered.length) results = filtered;
        }
        this.liveResults.set(results);
        this.liveNote.set(res.note || '');
        this.liveLoading.set(false);
      },
      error: () => {
        this.liveResults.set([]);
        this.liveNote.set('Live search unavailable — browse stores below instead.');
        this.liveLoading.set(false);
      },
    });
  }

  private matchesSearchCategory(item: LiveSearchResult, catId: string): boolean {
    const hay = `${item.category ?? ''} ${item.store ?? ''} ${item.name ?? ''}`.toLowerCase();
    switch (catId) {
      case 'fashion':
        return /fashion|clothing|apparel|dress|shirt|jacket|edgars|jet|foschini|zara|cotton|ackermans|pep|dunns/.test(hay);
      case 'shoes':
        return /shoe|sneaker|boot|heel|footwear|steve madden|sportscene|vans|adidas|nike/.test(hay);
      case 'kitchen':
        return /kitchen|cook|skillet|fryer|yuppiechef|le creuset|homeware/.test(hay);
      case 'fragrance':
        return /fragrance|perfume|cologne|beauty|edgars|dis-chem|red square/.test(hay);
      case 'tech':
        return /tech|laptop|phone|headphone|takealot|makro|electronic/.test(hay);
      case 'home':
        return /home|bedding|towel|decor|@home/.test(hay);
      case 'kids':
        return /kid|boy|girl|baby|junior|child/.test(hay);
      default:
        return true;
    }
  }

  private mapSearchCatToStoreTab(id: string): StoreTab | null {
    switch (id) {
      case 'fashion':
      case 'kids':
        return 'Fashion';
      case 'shoes':
        return 'Shoes';
      case 'kitchen':
      case 'home':
        return 'Kitchen';
      case 'fragrance':
        return 'Fragrance';
      case 'tech':
        return 'Marketplace';
      default:
        return null;
    }
  }
}
