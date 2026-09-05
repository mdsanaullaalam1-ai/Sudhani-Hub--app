import express, { Request, Response, NextFunction } from 'express';
import cors from 'cors';

const app = express();
const PORT = process.env.PORT || 5000;

app.use(cors());
app.use(express.json());

// In-Memory Quick-Commerce Seed Database for immediate zero-config plug & play
interface Product {
  id: string;
  categoryId: string;
  name: string;
  brand: string;
  description: string;
  price: number;
  mrp: number;
  discountPercent: number;
  unit: string;
  stock: number;
  emoji: string;
  isPopular: boolean;
  isDeal: boolean;
}

const categories = [
  { id: 'fruits_veg', name: 'Fruits & Vegetables', iconName: '🥦' },
  { id: 'dairy_breakfast', name: 'Dairy & Breakfast', iconName: '🥛' },
  { id: 'grocery', name: 'Grocery & Kitchen', iconName: '🌾' },
  { id: 'snacks', name: 'Snacks & Munchies', iconName: '🍟' },
  { id: 'beverages', name: 'Cold Drinks & Juices', iconName: '🥤' },
  { id: 'personal_care', name: 'Personal Care', iconName: '🧴' },
  { id: 'home_care', name: 'Home Care & Cleaning', iconName: '🧼' },
  { id: 'baby_care', name: 'Baby Care Essentials', iconName: '🍼' },
  { id: 'stationery', name: 'Office & Stationery', iconName: '✏️' },
  { id: 'accessories', name: 'Mobile Accessories', iconName: '🔌' }
];

let products: Product[] = [
  {
    id: 'fv_1',
    categoryId: 'fruits_veg',
    name: 'Fresh Hybrid Tomato',
    brand: 'Farm Fresh',
    description: 'Fresh farm-picked ripe red tomatoes.',
    price: 34,
    mrp: 45,
    discountPercent: 24,
    unit: '1 kg',
    stock: 80,
    emoji: '🍅',
    isPopular: true,
    isDeal: true
  },
  {
    id: 'fv_2',
    categoryId: 'fruits_veg',
    name: 'Ratnagiri Alphonso Mango',
    brand: 'Orchard Gold',
    description: 'Naturally ripened sweet mangoes.',
    price: 380,
    mrp: 500,
    discountPercent: 24,
    unit: '1 kg (approx 4-5 pcs)',
    stock: 35,
    emoji: '🥭',
    isPopular: true,
    isDeal: true
  },
  {
    id: 'db_1',
    categoryId: 'dairy_breakfast',
    name: 'Amul Taaza Homogenised Milk',
    brand: 'Amul',
    description: 'Pasteurised toned milk with wholesome nutrition.',
    price: 27,
    mrp: 28,
    discountPercent: 3,
    unit: '500 ml pouch',
    stock: 120,
    emoji: '🥛',
    isPopular: true,
    isDeal: false
  },
  {
    id: 'gr_1',
    categoryId: 'grocery',
    name: 'Aashirvaad Shudh Chakki Atta',
    brand: 'Aashirvaad',
    description: '100% pure whole wheat flour.',
    price: 215,
    mrp: 240,
    discountPercent: 10,
    unit: '5 kg bag',
    stock: 45,
    emoji: '🌾',
    isPopular: true,
    isDeal: true
  },
  {
    id: 'sn_1',
    categoryId: 'snacks',
    name: "Lay's India's Magic Masala",
    brand: "Lay's",
    description: 'Spicy crunchy potato chips with authentic Indian masala.',
    price: 20,
    mrp: 20,
    discountPercent: 0,
    unit: '50 g pouch',
    stock: 90,
    emoji: '🍟',
    isPopular: true,
    isDeal: false
  }
];

let cart: { [productId: string]: number } = { 'fv_1': 2, 'db_1': 1 };
let addresses = [
  {
    id: 'addr_home',
    label: 'Home',
    house: 'Flat 402, Green Meadows',
    street: '14th Cross, 100ft Road',
    area: 'Indiranagar',
    city: 'Bengaluru',
    state: 'Karnataka',
    pincode: '560038',
    isDefault: true
  }
];

let orders = [
  {
    id: 'SH-49219',
    status: 'CONFIRMED',
    total: 210,
    items: [{ productId: 'fv_1', quantity: 2 }, { productId: 'db_1', quantity: 1 }],
    deliveryPartner: 'Ramesh Verma',
    partnerPhone: '+91 98112 34567',
    vehicle: 'Electric Scooter (DL-3S-4412)',
    deliveryOtp: '4819',
    createdAt: new Date().toISOString()
  }
];

const coupons = [
  { code: 'JALDI100', discount: 100, minOrder: 399, description: 'Flat ₹100 OFF on orders above ₹399' },
  { code: 'WELCOME50', discount: 50, minOrder: 199, description: 'Flat ₹50 OFF for new users' }
];

// --------------------------------------------------------------------------
// 1. AUTH REST API
// --------------------------------------------------------------------------
app.post('/api/auth/send-otp', (req: Request, res: Response) => {
  const { phone } = req.body;
  if (!phone || phone.length < 10) {
    return res.status(400).json({ success: false, message: 'Valid 10-digit mobile number is required.' });
  }
  // Simulated SMS gateway trigger
  res.json({
    success: true,
    message: `OTP sent successfully to +91 ${phone}`,
    demoOtp: '4819'
  });
});

app.post('/api/auth/verify-otp', (req: Request, res: Response) => {
  const { phone, otp, name, email } = req.body;
  if (otp !== '4819' && otp !== '1234') {
    return res.status(400).json({ success: false, message: 'Invalid or expired OTP code.' });
  }
  res.json({
    success: true,
    message: 'Authenticated successfully',
    token: 'jwt_mock_token_sudhanihub_' + Date.now(),
    user: {
      id: 'usr_' + Date.now(),
      phone,
      name: name || 'Rahul Sharma',
      email: email || 'rahul@example.com',
      role: 'customer'
    }
  });
});

// --------------------------------------------------------------------------
// 2. CATEGORIES & PRODUCTS REST API
// --------------------------------------------------------------------------
app.get('/api/categories', (req: Request, res: Response) => {
  res.json({ success: true, count: categories.length, categories });
});

app.get('/api/products', (req: Request, res: Response) => {
  const { categoryId, popular, deal } = req.query;
  let result = [...products];

  if (categoryId) {
    result = result.filter(p => p.categoryId === categoryId);
  }
  if (popular === 'true') {
    result = result.filter(p => p.isPopular);
  }
  if (deal === 'true') {
    result = result.filter(p => p.isDeal);
  }

  res.json({ success: true, count: result.length, products: result });
});

app.get('/api/products/search', (req: Request, res: Response) => {
  const query = (req.query.q as string || '').toLowerCase();
  const matched = products.filter(p =>
    p.name.toLowerCase().includes(query) ||
    p.brand.toLowerCase().includes(query) ||
    p.description.toLowerCase().includes(query)
  );
  res.json({ success: true, count: matched.length, products: matched });
});

app.get('/api/products/:id', (req: Request, res: Response) => {
  const product = products.find(p => p.id === req.params.id);
  if (!product) {
    return res.status(404).json({ success: false, message: 'Product not found' });
  }
  res.json({ success: true, product });
});

// --------------------------------------------------------------------------
// 3. CART REST API
// --------------------------------------------------------------------------
app.get('/api/cart', (req: Request, res: Response) => {
  const items = Object.entries(cart).map(([productId, quantity]) => {
    const product = products.find(p => p.id === productId);
    return { product, quantity };
  }).filter(item => item.product != null);

  const subtotal = items.reduce((acc, cur) => acc + ((cur.product?.price || 0) * cur.quantity), 0);
  const deliveryFee = subtotal > 299 || subtotal === 0 ? 0 : 25;
  const platformFee = subtotal > 0 ? 2 : 0;
  const grandTotal = subtotal + deliveryFee + platformFee;

  res.json({
    success: true,
    items,
    bill: {
      subtotal,
      deliveryFee,
      platformFee,
      grandTotal
    }
  });
});

app.post('/api/cart/add', (req: Request, res: Response) => {
  const { productId, quantity = 1 } = req.body;
  cart[productId] = (cart[productId] || 0) + quantity;
  res.json({ success: true, message: 'Item added to cart', cart });
});

app.post('/api/cart/update', (req: Request, res: Response) => {
  const { productId, quantity } = req.body;
  if (quantity <= 0) {
    delete cart[productId];
  } else {
    cart[productId] = quantity;
  }
  res.json({ success: true, cart });
});

app.delete('/api/cart/remove/:id', (req: Request, res: Response) => {
  delete cart[req.params.id];
  res.json({ success: true, message: 'Item removed from cart', cart });
});

// --------------------------------------------------------------------------
// 4. ADDRESSES REST API
// --------------------------------------------------------------------------
app.get('/api/addresses', (req: Request, res: Response) => {
  res.json({ success: true, addresses });
});

app.post('/api/addresses', (req: Request, res: Response) => {
  const { label, house, street, area, city, state, pincode } = req.body;
  const newAddr = {
    id: 'addr_' + Date.now(),
    label: label || 'Home',
    house,
    street,
    area,
    city: city || 'Bengaluru',
    state: state || 'Karnataka',
    pincode,
    isDefault: false
  };
  addresses.push(newAddr);
  res.status(201).json({ success: true, address: newAddr });
});

// --------------------------------------------------------------------------
// 5. ORDERS REST API
// --------------------------------------------------------------------------
app.get('/api/orders', (req: Request, res: Response) => {
  res.json({ success: true, orders });
});

app.get('/api/orders/:id', (req: Request, res: Response) => {
  const order = orders.find(o => o.id === req.params.id);
  if (!order) {
    return res.status(404).json({ success: false, message: 'Order not found' });
  }
  res.json({ success: true, order });
});

app.post('/api/orders', (req: Request, res: Response) => {
  const { paymentMethod = 'COD', couponCode } = req.body;
  const newOrder = {
    id: `SH-${Math.floor(10000 + Math.random() * 90000)}`,
    status: 'ORDER_PLACED',
    total: 245,
    items: Object.entries(cart).map(([productId, quantity]) => ({ productId, quantity })),
    deliveryPartner: 'Ramesh Verma',
    partnerPhone: '+91 98112 34567',
    vehicle: 'Electric Scooter (DL-3S-4412)',
    deliveryOtp: '4819',
    createdAt: new Date().toISOString()
  };

  orders.unshift(newOrder);
  cart = {}; // Empty cart on successful checkout
  res.status(201).json({ success: true, message: 'Order placed successfully', order: newOrder });
});

app.put('/api/orders/:id/status', (req: Request, res: Response) => {
  const { status } = req.body;
  const order = orders.find(o => o.id === req.params.id);
  if (!order) {
    return res.status(404).json({ success: false, message: 'Order not found' });
  }
  order.status = status;
  res.json({ success: true, order });
});

// --------------------------------------------------------------------------
// 6. COUPONS REST API
// --------------------------------------------------------------------------
app.post('/api/coupons/apply', (req: Request, res: Response) => {
  const { code, orderAmount } = req.body;
  const coupon = coupons.find(c => c.code.toUpperCase() === code?.toUpperCase());
  if (!coupon) {
    return res.status(400).json({ success: false, message: 'Invalid coupon code.' });
  }
  if (orderAmount < coupon.minOrder) {
    return res.status(400).json({
      success: false,
      message: `Minimum order amount of ₹${coupon.minOrder} required for ${coupon.code}`
    });
  }
  res.json({ success: true, discount: coupon.discount, message: 'Coupon applied successfully!' });
});

// --------------------------------------------------------------------------
// 7. ADMIN & OPERATIONS REST API
// --------------------------------------------------------------------------
app.get('/api/admin/metrics', (req: Request, res: Response) => {
  res.json({
    success: true,
    metrics: {
      totalOrders: orders.length,
      totalSales: orders.reduce((sum, o) => sum + o.total, 0),
      activeDarkStores: 12,
      activeRiders: 48,
      avgDeliveryMinutes: 10.4,
      inventoryCount: products.length
    }
  });
});

app.put('/api/admin/inventory/:id', (req: Request, res: Response) => {
  const { stock, price } = req.body;
  const product = products.find(p => p.id === req.params.id);
  if (!product) return res.status(404).json({ success: false, message: 'Product not found' });
  if (stock !== undefined) product.stock = stock;
  if (price !== undefined) product.price = price;
  res.json({ success: true, product });
});

// Global Error Handler
app.use((err: Error, req: Request, res: Response, next: NextFunction) => {
  console.error(err.stack);
  res.status(500).json({ success: false, message: 'Internal Server Error' });
});

// Start Server
if (process.env.NODE_ENV !== 'test') {
  app.listen(PORT, () => {
    console.log(`⚡ SudhaniHub API Server running on port ${PORT}`);
    console.log(`⚡ Health Check: http://localhost:${PORT}/api/categories`);
  });
}

export default app;
