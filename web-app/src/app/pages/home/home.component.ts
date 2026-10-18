import { Component, DestroyRef, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { ApiService } from '../../core/api.service';
import { OrderCartService } from '../../core/order-cart.service';
import {
  CatalogDeal,
  CatalogService,
  CatalogStore,
  LiveSearchResult,
} from '../../core/catalog.service';
import { DEAL_IDS_IN_ORDER, fallbackImageFor } from '../../core/deal-image-fallbacks';
import {
  DEALS,
  DealCategory,
  PRICES_CHECKED,
  STORE_TABS,
  STORES,
  Store,
  StoreCategory,
} from '../../core/storefront.data';

type DealFilter = 'All' | DealCategory;
type StoreTab = 'All' | StoreCategory;

interface CategoryTile {
  title: string;
  stores: string;
  tab: StoreTab;
}

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [FormsModule, RouterLink],
  templateUrl: './home.component.html',
  styleUrl: './home.component.css',
})
export class HomeComponent {
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);
  private readonly api = inject(ApiService);
  private readonly catalog = inject(CatalogService);
  private readonly cart = inject(OrderCartService);

  readonly pricesChecked = PRICES_CHECKED;
  readonly dealFilters: DealFilter[] = ['All', 'Fashion', 'Shoes', 'Kitchen', 'Fragrance', 'Tech'];
  readonly storeTabs = STORE_TABS;
  readonly hints = ['Air fryer', 'Steve Madden sneakers', 'Zara sweatshirt', 'Cast-iron skillet'];
  readonly categories: CategoryTile[] = [
    { title: "Women's fashion", stores: 'Zara · Woolworths · Foschini', tab: 'Fashion' },
    { title: "Men's fashion", stores: 'Jet · Mr Price · Truworths', tab: 'Fashion' },
    { title: 'Shoes & sneakers', stores: 'Steve Madden · Sportscene', tab: 'Shoes' },
    { title: 'Kitchen & cooking', stores: 'Yuppiechef · Le Creuset', tab: 'Kitchen' },
    { title: 'Fragrance', stores: 'Red Square · Dis-Chem', tab: 'Fragrance' },
    { title: 'Tech & home', stores: 'Takealot · Makro', tab: 'Marketplace' },
  ];

  query = '';
  trackId = '';
  email = '';

  readonly deals = signal<CatalogDeal[]>(
    DEALS.map((d, i) => {
      const id = DEAL_IDS_IN_ORDER[i];
      return { ...d, id, imageUrl: fallbackImageFor({ ...d, id }) };
    })
  );
  readonly stores = signal<CatalogStore[]>(STORES.map((s) => ({ ...s, imageUrl: null })));
  readonly catalogLoading = signal(true);
  readonly scrapeNote = signal('');

  readonly dealFilter = signal<DealFilter>('All');
  readonly searchQuery = signal('');
  readonly liveResults = signal<LiveSearchResult[]>([]);
  readonly liveNote = signal('');
  readonly liveLoading = signal(false);
  readonly storeTab = signal<StoreTab>('All');
  readonly saved = signal<ReadonlySet<string>>(new Set());
  readonly countdown = signal({ h: '00', m: '00', s: '00' });
  readonly trackResult = signal<{ ok: boolean; text: string } | null>(null);
  readonly subscribed = signal(false);

  readonly visibleDeals = computed(() => {
    const f = this.dealFilter();
    const q = this.searchQuery().trim().toLowerCase();
    return this.deals()
      .filter((d) => f === 'All' || d.category === f)
      .filter((d) => {
        if (!q) return true;
        const hay = `${d.name} ${d.store} ${d.category}`.toLowerCase();
        if (hay.includes(q)) return true;
        return q.split(/\s+/).filter(Boolean).every((token) => this.tokenMatch(hay, token));
      })
      .slice(0, q ? 16 : 8);
  });

  readonly visibleStores = computed(() => {
    const t = this.storeTab();
    return this.stores().filter((s) => t === 'All' || s.category === t).slice(0, 8);
  });

  readonly heroDeals = computed(() => {
    const all = this.deals();
    return {
      fashion: all.find((d) => d.store === 'Zara') ?? all[0],
      shoes: all.find((d) => d.store === 'Steve Madden') ?? all[1],
      fragrance: all.find((d) => d.category === 'Fragrance' && d.was) ?? all.find((d) => d.category === 'Fragrance'),
    };
  });

  constructor() {
    this.tick();
    const timer = setInterval(() => this.tick(), 1000);
    const destroyRef = inject(DestroyRef);
    destroyRef.onDestroy(() => clearInterval(timer));
    const sub = this.route.queryParamMap.subscribe((params) => {
      const q = params.get('q')?.trim() ?? '';
      this.query = q;
      this.searchQuery.set(q);
      if (q) {
        this.dealFilter.set('All');
        this.runLiveSearch(q);
        setTimeout(() => document.getElementById('live-results')?.scrollIntoView({ behavior: 'smooth' }), 80);
      } else {
        this.liveResults.set([]);
        this.liveNote.set('');
      }
    });
    destroyRef.onDestroy(() => sub.unsubscribe());
    this.refreshCatalog();
  }

  refreshCatalog(): void {
    this.catalogLoading.set(true);
    this.catalog.loadDeals().subscribe((deals) => {
      this.deals.set(deals);
      this.catalogLoading.set(false);
      const withPhoto = deals.filter((d) => !!d.imageUrl).length;
      this.scrapeNote.set(`Showing ${withPhoto} product photos (scraped or standby images).`);
      // Best-effort background scrape for future visits — do not block UI on retailer blocks.
      this.catalog.triggerScrape().subscribe({
        next: () => {
          setTimeout(() => {
            this.catalog.loadDeals().subscribe((fresh) => this.deals.set(fresh));
            this.catalog.loadStores().subscribe((stores) => this.mergeStores(stores));
          }, 8000);
        },
        error: () => {
          /* admin-bff optional for standby images */
        },
      });
    });
    this.catalog.loadStores().subscribe((stores) => this.mergeStores(stores));
  }

  private mergeStores(fromApi: CatalogStore[]): void {
    if (!fromApi.length) return;
    const byName = new Map(fromApi.map((s) => [s.name.toLowerCase(), s]));
    this.stores.set(
      STORES.map((seed) => {
        const live = byName.get(seed.name.toLowerCase());
        return live ? { ...seed, ...live, imageUrl: live.imageUrl ?? null } : { ...seed, imageUrl: null };
      })
    );
  }

  find(term = this.query): void {
    const q = term.trim();
    if (!q) {
      this.clearSearch();
      return;
    }
    this.query = q;
    this.dealFilter.set('All');
    const same = (this.route.snapshot.queryParamMap.get('q') ?? '') === q;
    // queryParamMap subscription runs live search + scroll (re-run if same query)
    this.router.navigate(['/'], { queryParams: { q }, fragment: 'live-results' }).then(() => {
      if (same) {
        this.searchQuery.set(q);
        this.runLiveSearch(q);
        setTimeout(() => document.getElementById('live-results')?.scrollIntoView({ behavior: 'smooth' }), 50);
      }
    });
  }

  clearSearch(): void {
    this.query = '';
    this.searchQuery.set('');
    this.liveResults.set([]);
    this.liveNote.set('');
    this.router.navigate(['/'], { fragment: 'sale' });
  }

  private runLiveSearch(q: string): void {
    this.liveLoading.set(true);
    this.liveNote.set('Searching Takealot, Edgars, Steve Madden, Jet, Foschini, Cotton On and more…');
    this.catalog.liveSearch(q).subscribe({
      next: (res) => {
        this.liveResults.set(res.results ?? []);
        this.liveNote.set(res.note || '');
        this.liveLoading.set(false);
      },
      error: () => {
        this.liveResults.set([]);
        this.liveNote.set('Live search unavailable — browse deals below instead.');
        this.liveLoading.set(false);
      },
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

  /** Swap a broken scraped URL for the standby product photo. */
  onDealImageError(deal: CatalogDeal): void {
    const fb = fallbackImageFor(deal);
    if (!fb || deal.imageUrl === fb) return;
    this.deals.update((list) =>
      list.map((d) => (d.id === deal.id || (d.name === deal.name && d.store === deal.store) ? { ...d, imageUrl: fb } : d))
    );
  }

  uploadPhoto(): void {
    this.router.navigate(['/orders/new'], { queryParams: { photo: 1 } });
  }

  /** Add product (photo + price) to the cart; pick size on the cart page if needed. */
  addToOrder(deal: CatalogDeal): void {
    this.cart.addProduct({
      productName: deal.name,
      store: deal.store,
      productUrl: deal.url,
      imageUrl: deal.imageUrl || undefined,
      unitPrice: deal.price,
      category: deal.category,
    });
    this.router.navigate(['/orders/new']);
  }

  toggleSaved(deal: CatalogDeal): void {
    this.saved.update((set) => {
      const next = new Set(set);
      next.has(deal.url + deal.name) ? next.delete(deal.url + deal.name) : next.add(deal.url + deal.name);
      return next;
    });
  }

  isSaved(deal: CatalogDeal): boolean {
    return this.saved().has(deal.url + deal.name);
  }

  showCategory(tab: StoreTab): void {
    if (tab === 'All') {
      this.router.navigate(['/stores']);
      return;
    }
    this.router.navigate(['/stores'], { queryParams: { storeCat: tab } });
  }

  showDeals(filter: DealFilter): void {
    this.dealFilter.set(filter);
    document.getElementById('sale')?.scrollIntoView({ behavior: 'smooth' });
  }

  track(): void {
    const id = this.trackId.trim();
    if (!id) return;
    this.router.navigate(['/orders/track'], { queryParams: { id } });
  }

  subscribe(): void {
    if (!/^\S+@\S+\.\S+$/.test(this.email.trim())) return;
    this.subscribed.set(true);
  }

  discount(deal: CatalogDeal): string {
    if (deal.was) return `−${Math.round((1 - deal.price / deal.was) * 100)}%`;
    return deal.badge ?? '';
  }

  rand(value: number): string {
    const cents = value % 1 !== 0;
    return 'R' + value.toLocaleString('en-US', { minimumFractionDigits: cents ? 2 : 0, maximumFractionDigits: 2 });
  }

  initials(store: Store): string {
    const letters = store.name.replace(/[^A-Za-z ]/g, '').split(' ').filter(Boolean).map((w) => w[0]);
    return (letters.join('').slice(0, 2) || store.name.slice(0, 2)).toUpperCase();
  }

  private tokenMatch(hay: string, token: string): boolean {
    if (hay.includes(token)) return true;
    // soft plural / singular ("sneakers" ↔ "sneaker")
    if (token.endsWith('s') && token.length > 3 && hay.includes(token.slice(0, -1))) return true;
    if (hay.includes(token + 's')) return true;
    return false;
  }

  private tick(): void {
    const now = new Date();
    const end = new Date(now);
    end.setHours(24, 0, 0, 0);
    const total = Math.max(0, Math.floor((end.getTime() - now.getTime()) / 1000));
    const pad = (n: number) => String(n).padStart(2, '0');
    this.countdown.set({
      h: pad(Math.floor(total / 3600)),
      m: pad(Math.floor((total % 3600) / 60)),
      s: pad(total % 60),
    });
  }
}
