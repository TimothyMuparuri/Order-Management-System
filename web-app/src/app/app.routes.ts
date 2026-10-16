import { Routes } from '@angular/router';
import { CustomerOrderComponent } from './pages/customer-order/customer-order.component';
import { AdminComponent } from './pages/admin/admin.component';

export const routes: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'orders/new' },
  { path: 'orders/new', component: CustomerOrderComponent },
  { path: 'admin', component: AdminComponent },
  { path: '**', redirectTo: 'orders/new' },
];
