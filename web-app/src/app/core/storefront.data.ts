export type StoreCategory = 'Fashion' | 'Shoes' | 'Kitchen' | 'Fragrance' | 'Marketplace';
export type DealCategory = 'Fashion' | 'Shoes' | 'Kitchen' | 'Fragrance' | 'Tech';

export interface Store {
  name: string;
  url: string;
  category: StoreCategory;
  meta: string;
  sale?: string;
}

export interface Deal {
  store: string;
  category: DealCategory;
  name: string;
  url: string;
  price: number;
  was?: number;
  /** Shown instead of a computed percentage when there is no "was" price. */
  badge?: string;
}

/** Prices captured from each retailer's website in October 2026; they change often. */
export const PRICES_CHECKED = 'October 2026';

export const STORE_TABS: Array<'All' | StoreCategory> = [
  'All',
  'Fashion',
  'Shoes',
  'Kitchen',
  'Fragrance',
  'Marketplace',
];

/** Search scopes shown in the category bar (maps to product search intent). */
export const SEARCH_CATEGORIES: Array<{ id: string; label: string; hint: string }> = [
  { id: 'all', label: 'All', hint: '' },
  { id: 'fashion', label: 'Fashion', hint: 'dress shirt jacket' },
  { id: 'shoes', label: 'Shoes', hint: 'sneakers boots heels' },
  { id: 'kitchen', label: 'Kitchen', hint: 'air fryer skillet cookware' },
  { id: 'fragrance', label: 'Fragrance', hint: 'perfume cologne' },
  { id: 'tech', label: 'Tech', hint: 'laptop headphones' },
  { id: 'home', label: 'Home', hint: 'bedding towels decor' },
  { id: 'kids', label: 'Kids', hint: 'kids boys girls' },
];

export const STORES: Store[] = [
  { name: 'Takealot', url: 'https://www.takealot.com', category: 'Marketplace', meta: 'Electronics, home, everything', sale: 'Daily deals' },
  { name: 'Makro', url: 'https://www.makro.co.za', category: 'Marketplace', meta: 'Bulk, tech & appliances' },
  { name: 'Zara', url: 'https://www.zara.com/za/', category: 'Fashion', meta: 'Women · Men · Kids', sale: 'Up to 77% off' },
  { name: 'Edgars', url: 'https://www.edgars.co.za', category: 'Fashion', meta: 'Fashion, beauty & fragrance', sale: 'Live search' },
  { name: 'Jet', url: 'https://bash.com/jet', category: 'Fashion', meta: 'Family fashion & home', sale: 'Sale up to 43% off' },
  { name: 'Foschini', url: 'https://www.foschini.co.za', category: 'Fashion', meta: "Women's fashion & beauty", sale: 'Live search' },
  { name: 'Woolworths', url: 'https://www.woolworths.co.za', category: 'Fashion', meta: 'Fashion, beauty, home' },
  { name: 'Mr Price', url: 'https://www.mrp.com/en_za/', category: 'Fashion', meta: 'Fashion at value prices', sale: '30% off jackets' },
  { name: 'Ackermans', url: 'https://www.ackermans.co.za', category: 'Fashion', meta: 'Family value fashion', sale: 'Live search' },
  { name: 'PEP', url: 'https://www.pepstores.com', category: 'Fashion', meta: 'Value fashion & essentials', sale: 'Live search' },
  { name: 'Dunns', url: 'https://www.dunns.co.za', category: 'Fashion', meta: 'Everyday fashion & shoes', sale: 'Live search' },
  { name: 'Cotton On', url: 'https://cottonon.com/ZA/', category: 'Fashion', meta: 'Casual fashion & basics', sale: 'Live search' },
  { name: 'Superbalist', url: 'https://superbalist.com', category: 'Fashion', meta: 'Online fashion & brands', sale: 'Up to 70% off' },
  { name: 'Truworths', url: 'https://www.truworths.co.za', category: 'Fashion', meta: 'Premium everyday fashion' },
  { name: 'Markham', url: 'https://bash.com/markham', category: 'Fashion', meta: "Men's fashion on Bash" },
  { name: 'Exact', url: 'https://bash.com/exact', category: 'Fashion', meta: 'Value family fashion' },
  { name: 'H&M', url: 'https://www2.hm.com/en_za/index.html', category: 'Fashion', meta: 'Fast fashion essentials' },
  { name: 'Steve Madden', url: 'https://stevemadden.co.za', category: 'Shoes', meta: 'Sneakers, heels, boots', sale: 'Up to 50% off' },
  { name: 'Sportscene', url: 'https://www.sportscene.co.za', category: 'Shoes', meta: 'Sneakers & streetwear', sale: 'Offers & sale' },
  { name: 'Totalsports', url: 'https://bash.com/totalsports', category: 'Shoes', meta: 'Sport & running gear' },
  { name: 'Shoe City', url: 'https://shoecity.co.za', category: 'Shoes', meta: 'Everyday footwear' },
  { name: 'Yuppiechef', url: 'https://www.yuppiechef.com', category: 'Kitchen', meta: 'Cookware & kitchen tools', sale: 'Clearance' },
  { name: 'Le Creuset', url: 'https://www.lecreuset.co.za', category: 'Kitchen', meta: 'Cast-iron cookware', sale: 'Up to 25% off' },
  { name: '@home', url: 'https://home.co.za', category: 'Kitchen', meta: 'Homeware & kitchen', sale: 'Up to 70% off' },
  { name: 'Red Square', url: 'https://www.edgars.co.za', category: 'Fragrance', meta: 'Designer fragrance at Edgars', sale: 'Fragrance offers' },
  { name: 'Dis-Chem', url: 'https://www.dischem.co.za', category: 'Fragrance', meta: 'Perfume, beauty & health' },
  { name: 'Clicks', url: 'https://clicks.co.za', category: 'Fragrance', meta: 'Beauty, health & fragrance' },
];

export const DEALS: Deal[] = [
  {
    store: 'Zara', category: 'Fashion', name: 'Textured sweatshirt',
    url: 'https://www.zara.com/za/en/textured-sweatshirt-p04092318.html', price: 359, was: 679,
  },
  {
    store: 'Steve Madden', category: 'Shoes', name: 'Cameos Tan low-top sneaker',
    url: 'https://stevemadden.co.za/products/cameos-tan', price: 595, badge: 'Sale',
  },
  {
    store: 'Takealot', category: 'Tech', name: 'Russell Hobbs Digital 9L Air Fryer (RHAF09S)',
    url: 'https://www.takealot.com/all?qsearch=Russell%20Hobbs%20RHAF09S', price: 1099, was: 1499,
  },
  {
    store: 'Edgars', category: 'Fragrance', name: 'Carolina Herrera Very Good Girl Elixir',
    url: 'https://www.edgars.co.za/collections/carolina-herrera-good-girl', price: 2810, was: 3590,
  },
  {
    store: 'Yuppiechef', category: 'Kitchen', name: 'Le Creuset Foret Signature Cast Iron Skillet, 26cm',
    url: 'https://www.yuppiechef.com/cookware.htm', price: 2924, was: 3899,
  },
  {
    store: 'Jet', category: 'Fashion', name: "Jet Women's Black Knit Textured Skirt",
    url: 'https://bash.com/jet/sale', price: 180, was: 279.99,
  },
  {
    store: 'Yuppiechef', category: 'Kitchen', name: 'WMF Diadem Plus Stainless Steel Cookware Set, 5-Piece',
    url: 'https://www.yuppiechef.com/wmf-diadem-plus.htm', price: 4599, was: 5999,
  },
  {
    store: 'Dis-Chem', category: 'Fragrance', name: 'Hugo Boss Bottled Eau de Toilette 100ml',
    url: 'https://www.dischem.co.za/hugo-boss-bottled-eau-de-toilette-100ml-867', price: 1935, badge: 'Best seller',
  },
  {
    store: 'Superbalist', category: 'Fashion', name: 'boohoo Chiffon strappy volume maxi dress – black',
    url: 'https://superbalist.com/shop/long-dresses', price: 400, was: 999,
  },
  {
    store: 'Sportscene', category: 'Shoes', name: "Vans Women's Knu Skool Oversized Lace White/Blue",
    url: 'https://www.sportscene.co.za/plp/footwear/women/_/N-27z3', price: 1049, badge: 'Sale',
  },
  {
    store: 'Takealot', category: 'Tech', name: 'HP G9 Series 15.6" Laptop – 8GB RAM, 256GB SSD',
    url: 'https://www.takealot.com/all?qsearch=HP%20G9%2015.6%208GB%20256GB', price: 6499, was: 8899,
  },
  {
    store: 'Steve Madden', category: 'Shoes', name: 'Bassett Black/White low-top sneaker',
    url: 'https://stevemadden.co.za/products/bassett-black-white', price: 695, badge: 'Sale',
  },
  {
    store: 'Mr Price', category: 'Fashion', name: 'RT Casual Hooded Puffer Jacket',
    url: 'https://www.mrp.com/en_za/promos/30-off-jackets/mens', price: 250, badge: '30% off',
  },
  {
    store: 'Yuppiechef', category: 'Kitchen', name: 'Stanley The AeroLight Transit Mug, 470ml',
    url: 'https://www.yuppiechef.com/price.htm', price: 799, was: 1099,
  },
  {
    store: 'Red Square', category: 'Fragrance', name: 'Red Square Fragrance – Citrus Noir 50ml',
    url: 'https://www.edgars.co.za/products/red-square-frag-50ml-citrus-noir-42289804-opt4', price: 299.99, badge: 'Under R300',
  },
  {
    store: 'Superbalist', category: 'Fashion', name: 'MANGO Dress perla – black',
    url: 'https://superbalist.com/feature/up-to-50-off-across-departments/948829ed9c/women/dresses/f-258-scoop-neck',
    price: 920, was: 2299,
  },
];
