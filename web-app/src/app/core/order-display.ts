import { OrderDetail, OrderLineView } from './api.service';
import { CatalogDeal } from './catalog.service';
import { DEALS } from './storefront.data';

/** Fill missing store / product link from catalog so admin can always open the retailer page. */
export function enrichOrderForDisplay(order: OrderDetail, catalogDeals: CatalogDeal[] = []): OrderDetail {
  const deals: Array<{ id?: string; name: string; store: string; url: string; imageUrl?: string | null }> = [
    ...catalogDeals,
    ...DEALS.map((d) => ({ ...d, imageUrl: null as string | null })),
  ];

  return {
    ...order,
    lines: (order.lines ?? []).map((line) => enrichLine(line, deals)),
  };
}

function enrichLine(
  line: OrderLineView,
  deals: Array<{ id?: string; name: string; store: string; url: string; imageUrl?: string | null }>
): OrderLineView {
  if (line.storeName && line.productUrl) {
    return line;
  }

  const dealId = line.imageUrl?.match(/\/deal\/([^/?#]+)/i)?.[1];
  const byId = dealId ? deals.find((d) => d.id === dealId) : undefined;
  const name = (line.productName || '').trim().toLowerCase();
  const byName = name
    ? deals.find((d) => d.name.trim().toLowerCase() === name)
    : undefined;
  const byPartial = name
    ? deals.find(
        (d) =>
          name.includes(d.name.trim().toLowerCase()) ||
          d.name.trim().toLowerCase().includes(name)
      )
    : undefined;
  const match = byId || byName || byPartial;

  if (!match) {
    return line;
  }

  return {
    ...line,
    storeName: line.storeName || match.store,
    productUrl: line.productUrl || match.url,
    imageUrl: line.imageUrl || match.imageUrl || undefined,
  };
}
