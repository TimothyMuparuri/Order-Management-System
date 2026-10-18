import { Component, computed, inject } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { FormsModule } from '@angular/forms';
import { NavigationEnd, Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { filter, map, startWith } from 'rxjs';
import { OrderCartService } from './core/order-cart.service';
import { PRICES_CHECKED, SEARCH_CATEGORIES, STORES } from './core/storefront.data';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, RouterLink, RouterLinkActive, FormsModule],
  templateUrl: './app.component.html',
  styleUrl: './app.component.css',
})
export class AppComponent {
  title = 'sapersonalshopper';
  query = '';
  readonly footerStores = STORES.slice(0, 6);
  readonly searchCategories = SEARCH_CATEGORIES;
  readonly pricesChecked = PRICES_CHECKED;
  readonly cart = inject(OrderCartService);
  private readonly router = inject(Router);

  private readonly url = toSignal(
    this.router.events.pipe(
      filter((e): e is NavigationEnd => e instanceof NavigationEnd),
      map(() => this.router.url),
      startWith(this.router.url)
    ),
    { initialValue: this.router.url }
  );

  readonly isAdmin = computed(() => this.url().startsWith('/admin'));

  search(event: Event): void {
    event.preventDefault();
    const q = this.query.trim();
    if (!q) return;
    this.router.navigate(['/'], { queryParams: { q }, fragment: 'live-results' });
  }

  openCategory(id: string): void {
    if (id === 'all') {
      this.router.navigate(['/stores']);
      return;
    }
    this.router.navigate(['/stores'], { queryParams: { cat: id } });
  }
}
