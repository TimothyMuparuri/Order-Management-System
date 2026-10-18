import { DealCategory } from './storefront.data';

/** Categories that usually need a customer size choice. */
const SIZED_CATEGORIES = new Set<string>(['Fashion', 'Shoes']);

export function needsSize(category?: string | null): boolean {
  return !!category && SIZED_CATEGORIES.has(category);
}

export function sizeOptionsFor(category?: string | null): string[] {
  if (category === 'Shoes') {
    return ['3', '4', '5', '6', '7', '8', '9', '10', '11', '12'];
  }
  if (category === 'Fashion') {
    return ['XS', 'S', 'M', 'L', 'XL', 'XXL'];
  }
  return [];
}

export function defaultCategoryHint(store?: string, name?: string): DealCategory | undefined {
  const s = `${store || ''} ${name || ''}`.toLowerCase();
  if (/(sneaker|shoe|heel|boot|sandal|vans|madden|sportscene)/.test(s)) return 'Shoes';
  if (/(dress|skirt|jacket|hoodie|top|shirt|jean|trouser|fashion|zara|jet|foschini)/.test(s)) {
    return 'Fashion';
  }
  if (/(perfume|fragrance|eau de)/.test(s)) return 'Fragrance';
  if (/(air fryer|laptop|cookware|skillet|kitchen)/.test(s)) return 'Kitchen';
  return undefined;
}
