import { Routes } from '@angular/router';
import { HomeComponent } from './pages/home/home.component';
import { StoresComponent } from './pages/stores/stores.component';
import { CustomerOrderComponent } from './pages/customer-order/customer-order.component';
import { TrackOrderComponent } from './pages/track-order/track-order.component';
import { AdminShellComponent } from './pages/admin/admin-shell.component';
import { AdminOverviewComponent } from './pages/admin/admin-overview.component';
import { AdminOrdersComponent } from './pages/admin/admin-orders.component';
import { AdminWhatsappComponent } from './pages/admin/admin-whatsapp.component';

export const routes: Routes = [
  { path: '', pathMatch: 'full', component: HomeComponent },
  { path: 'stores', component: StoresComponent },
  { path: 'orders/new', component: CustomerOrderComponent },
  { path: 'orders/track', component: TrackOrderComponent },
  {
    path: 'admin',
    component: AdminShellComponent,
    children: [
      { path: '', pathMatch: 'full', component: AdminOverviewComponent },
      { path: 'orders', component: AdminOrdersComponent },
      { path: 'whatsapp', component: AdminWhatsappComponent },
    ],
  },
  { path: '**', redirectTo: '' },
];
