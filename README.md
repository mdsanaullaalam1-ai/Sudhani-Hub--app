# SudhaniHub ⚡
> **"Sab Kuch, Jaldi Se"** — High-Performance 10-Minute Hyper-Local Grocery & Daily Essentials Delivery Platform

---

## 🌟 Overview

**SudhaniHub** is a modern, end-to-end quick-commerce platform inspired by 10-minute delivery services like Blinkit and Zepto, crafted with an original brand identity, emerald-and-gold visual palette, and architecture for extreme speed and scalability.

This repository includes:
1. **Customer Mobile Application**: Built with Jetpack Compose, Material 3, and Kotlin. Features full navigation across 14 screens, cart management, address selection, coupons, payment checkout, and live order tracking with simulation controls.
2. **Backend REST API**: Node.js + Express + TypeScript with complete endpoints for Auth, Products, Cart, Addresses, Orders, Coupons, and Dark Store Operations.
3. **PostgreSQL Database Schema**: Production-ready relational schema with tables, constraints, foreign keys, and indexes.
4. **Web Admin & Dark Store Hub**: Interactive management dashboard for live telemetry, SKU inventory control, and rider dispatch.
5. **Payment & Notification Abstractions**: Plug-and-play abstractions for Razorpay, UPI deep linking, Google Maps GPS coordinates, and Firebase Cloud Messaging (FCM).

---

## 📱 Customer Mobile App (14 Screens)

The mobile client is built on **Kotlin + Jetpack Compose** with a reactive single source of truth (`SudhaniRepository` with `StateFlow`):

1. **Splash Screen**: SudhaniHub logo with lightning bolt badge, emerald gradient, and tagline *"Sab Kuch, Jaldi Se"*.
2. **Onboarding Screen**: Carousel highlighting 10-minute delivery, fresh farm produce, and multi-mode payments (UPI, COD, Cards).
3. **Login / Signup Screen**: Mobile phone input with +91 country code, user name, optional email, and OTP verification flow (with demo auto-fill `4819`).
4. **Home Screen**:
   - Dynamic address header with instant 10-15 min badge
   - Search bar trigger
   - Promotional banner carousel (e.g., `JALDI100`, Mango festival, Dairy)
   - 10 category pills with quick access
   - Best Deals & Popular Items shelves
   - Add to Cart `+/-` steppers and bottom floating cart banner
5. **Categories Screen**: Left category navigation drawer (10 categories: Fruits & Vegetables, Dairy, Grocery, Snacks, Beverages, Personal Care, Home Care, Baby Care, Stationery, Mobile Accessories) with responsive 2-column product grid.
6. **Search Screen**: Real-time search query matching, popular query chips, category filters, price sorting, and "No products found" empty state.
7. **Product Details Screen**: High-resolution emoji/image preview, 10-min delivery badge, brand, MRP with strikethrough, discount percentage, unit description, rating, stock status, and "Buy Now" quick checkout.
8. **Cart Screen**:
   - Free delivery progress indicator (Unlocked at ₹299+)
   - Quantity adjusters and delete triggers
   - Coupon applicator (`JALDI100`, `WELCOME50`, `FREEDEL`)
   - Detailed bill summary (Subtotal, Delivery Fee, Platform fee, Coupon discount, Grand Total)
   - Empty cart state with "Start Shopping" button
9. **Address Screen**: Saved addresses (Home, Work, Other), GPS location auto-fill simulation, and "Add New Address" modal dialog.
10. **Checkout Screen**: Address verification, 10-minute dark store delivery promise, order items summary, and payment method selection (Cash on Delivery or UPI/Online).
11. **Order Success Screen**: Order ID generator, confetti celebratory badge, estimated delivery countdown, and "Track Order" button.
12. **My Orders Screen**: Active orders with status badges, past order history with timestamps, and one-tap "Reorder" button.
13. **Order Tracking Screen**:
    - 6-stage order journey: `Order Placed` ➔ `Confirmed` ➔ `Preparing` ➔ `Picked Up` ➔ `Out for Delivery` ➔ `Delivered`
    - Live delivery partner details: Ramesh Verma, Electric Scooter (`DL-3S-4412`), Call button, and Delivery OTP.
    - Interactive "Simulate Next Status Step" button to test the entire lifecycle in real time.
14. **Profile & Admin Hub Screen**: User profile editing, saved addresses, wishlist viewer dialog, 24/7 help & support queries, terms of service, and access to the interactive Admin Panel.

---

## 🗄️ Database Architecture (PostgreSQL)

The database schema is located in `backend/src/schema.sql`:

- `users`: Customer, Admin, and Partner accounts (`id`, `phone`, `name`, `email`, `role`, `created_at`)
- `categories`: Hierarchy and icons for the 10 quick-commerce departments
- `products`: SKU metadata, prices, MRP, discounts, unit quantities, stock levels, and flags
- `addresses`: User delivery locations with GPS coordinates and type tags (`Home`, `Work`, `Other`)
- `cart`: Persistent cart sessions keyed by `(user_id, product_id)`
- `orders`: Orders with status enum, payment details, OTP, and assigned rider
- `order_items`: Line-item breakdown per order
- `coupons`: Promotional codes with minimum order limits and flat/percentage discounts
- `delivery_partners`: Rider fleet metadata with live vehicle info, availability, and rating

---

## 🚀 Backend REST API

### Running the Backend
```bash
cd backend
npm install
npm run dev
```

### Endpoints Overview

| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `POST` | `/api/auth/send-otp` | Sends 4-digit verification code to mobile number |
| `POST` | `/api/auth/verify-otp` | Validates OTP and returns JWT token & user session |
| `GET` | `/api/categories` | Returns all 10 grocery categories |
| `GET` | `/api/products` | Lists products with optional `categoryId`, `popular`, and `deal` filters |
| `GET` | `/api/products/search?q=:query` | Searches products across title, brand, and description |
| `GET` | `/api/products/:id` | Returns single product details |
| `GET` | `/api/cart` | Returns user's cart items with delivery & bill calculation |
| `POST` | `/api/cart/add` | Adds product to cart |
| `POST` | `/api/cart/update` | Updates quantity of a product in cart |
| `DELETE` | `/api/cart/remove/:id` | Removes item from cart |
| `GET` | `/api/addresses` | Returns saved delivery addresses |
| `POST` | `/api/addresses` | Creates a new delivery address |
| `GET` | `/api/orders` | Retrieves user order history |
| `POST` | `/api/orders` | Places a new order and empties cart |
| `GET` | `/api/orders/:id` | Fetches live status and assigned delivery partner for an order |
| `PUT` | `/api/orders/:id/status` | Updates order progression stage |
| `POST` | `/api/coupons/apply` | Validates coupon code against order value |
| `GET` | `/api/admin/metrics` | Returns live dark store revenue and delivery KPIs |
| `PUT` | `/api/admin/inventory/:id` | Updates stock quantity or price of an SKU |

---

## 💻 Web Admin Panel

Open `admin/index.html` in any browser or serve with a static server:
```bash
npx serve admin
```

Features included:
- **Live Dispatch Queue**: Real-time order cards showing order status pills (`PREPARING`, `OUT FOR DELIVERY`, `DELIVERED`).
- **One-Click Order Advancement**: Dispatch riders and mark orders delivered in real time.
- **SKU Inventory Management**: Add new products, adjust stock with `+10` / `-5` buttons.
- **Dark Store Analytics**: Revenue, average delivery minutes (10.4 mins), active stores, and fleet status.

---

## 💳 Payment & Notification Abstractions

- **Payment Service** (`backend/src/services/payment.service.ts`):
  - Ready for **Razorpay** checkout generation (amount in paise, signature verification).
  - UPI deep-linking support (`upi://pay?pa=...`) for instant PhonePe and Google Pay checkout.
  - Cash on delivery flow with doorstep verification.
- **Notification Service** (`backend/src/services/notification.service.ts`):
  - Firebase Cloud Messaging (FCM) integration ready for customer order status push notifications.
  - Nearby rider fleet dispatch broadcast.

---

## 🎨 Design System

- **Primary Green**: `#0F8A4B` (Fresh, reliable Indian grocery aesthetic)
- **Dark Green**: `#074625` (High-contrast text and hero surfaces)
- **Sudhani Gold**: `#F5B014` (Lightning delivery badge and accents)
- **Soft Light Green**: `#E8F5E9` (Backgrounds and tags)
- **Accent Orange**: `#FF7A00` (Deals and offers)
- **Discount Red**: `#D32F2F` (Savings and price strikethroughs)
