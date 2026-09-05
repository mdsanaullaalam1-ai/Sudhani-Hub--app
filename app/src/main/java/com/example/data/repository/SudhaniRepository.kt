package com.example.data.repository

import android.content.Context
import com.example.data.firebase.FirebaseAuthRepository
import com.example.data.firebase.FirestoreRepository
import com.example.data.model.*
import com.example.data.service.PriceCalculationService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

object SudhaniRepository {

    val firebaseAuth = FirebaseAuthRepository()
    val firestoreRepo = FirestoreRepository()

    private val repositoryScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var orderRepository: OrderRepository? = null
    private var couponRepository: CouponRepository? = null
    private var paymentRepository: PaymentRepository? = null
    private var customerRepository: CustomerRepository? = null
    private var partnerRepository: DeliveryPartnerRepository? = null
    private var productRepository: ProductRepository? = null
    private var savedPaymentRepository: SavedPaymentMethodRepository? = null
    private var returnRepository: ReturnRepository? = null

    val categories = listOf(
        Category("fruits_veg", "Fruits & Vegetables", "🥦", "Fresh Daily"),
        Category("dairy_breakfast", "Dairy & Breakfast", "🥛", "Morning Farm"),
        Category("grocery", "Grocery & Staples", "🌾", "Best Deals"),
        Category("snacks", "Snacks & Munchies", "🍿", "Quick Bites"),
        Category("beverages", "Beverages", "🧃", "Chilled"),
        Category("personal_care", "Personal Care", "🧴", "Daily Hygiene"),
        Category("home_care", "Home Care", "🧼", "Cleaning"),
        Category("baby_care", "Baby Care", "👶", "Gentle"),
        Category("stationery", "Stationery", "✏️", "Office & School"),
        Category("mobile_acc", "Mobile Accessories", "🔌", "Cables & Power")
    )

    private val initialProducts = listOf(
        // Fruits & Veg
        Product(
            id = "fv_1",
            categoryId = "fruits_veg",
            name = "Fresh Hybrid Tomatoes",
            brand = "Farm Fresh",
            description = "Locally sourced ripe and juicy hybrid red tomatoes. Handpicked for curries and salads.",
            unit = "1 kg",
            price = 38.0,
            mrp = 55.0,
            discountPercent = 30,
            isPopular = true,
            isDeal = true,
            emoji = "🍅"
        ),
        Product(
            id = "fv_2",
            categoryId = "fruits_veg",
            name = "Fresh Alphonso Mangoes",
            brand = "Ratnagiri Farms",
            description = "Naturally ripened sweet and aromatic Ratnagiri Alphonso mangoes.",
            unit = "6 pcs",
            price = 449.0,
            mrp = 600.0,
            discountPercent = 25,
            isPopular = true,
            isDeal = true,
            emoji = "🥭"
        ),
        Product(
            id = "fv_3",
            categoryId = "fruits_veg",
            name = "Organic Spinach (Palak)",
            brand = "Green Earth",
            description = "Crisp and tender hydroponic farm fresh spinach leaves rich in iron.",
            unit = "250 g",
            price = 24.0,
            mrp = 35.0,
            discountPercent = 31,
            emoji = "🥬"
        ),
        Product(
            id = "fv_4",
            categoryId = "fruits_veg",
            name = "Fresh Red Onions",
            brand = "Farm Fresh",
            description = "Grade-A medium red onions with crisp texture and rich pungent flavor.",
            unit = "1 kg",
            price = 32.0,
            mrp = 45.0,
            discountPercent = 28,
            isPopular = true,
            isRecentlyPurchased = true,
            emoji = "🧅"
        ),

        // Dairy & Breakfast
        Product(
            id = "db_1",
            categoryId = "dairy_breakfast",
            name = "Amul Taaza Homogenised Toned Milk",
            brand = "Amul",
            description = "Wholesome toned milk pasteurized and packed under hygienic conditions.",
            unit = "1 L",
            price = 56.0,
            mrp = 58.0,
            discountPercent = 4,
            isPopular = true,
            isRecentlyPurchased = true,
            emoji = "🥛"
        ),
        Product(
            id = "db_2",
            categoryId = "dairy_breakfast",
            name = "Amul Salted Butter",
            brand = "Amul",
            description = "The classic taste of India. Pure cow & buffalo milk creamy table butter.",
            unit = "500 g",
            price = 275.0,
            mrp = 285.0,
            discountPercent = 5,
            isPopular = true,
            emoji = "🧈"
        ),
        Product(
            id = "db_3",
            categoryId = "dairy_breakfast",
            name = "Kellogg's Real Almond Corn Flakes",
            brand = "Kellogg's",
            description = "Crispy golden corn flakes enriched with sliced almonds and natural honey.",
            unit = "450 g",
            price = 220.0,
            mrp = 260.0,
            discountPercent = 15,
            isDeal = true,
            emoji = "🥣"
        ),
        Product(
            id = "db_4",
            categoryId = "dairy_breakfast",
            name = "Farm Fresh White Eggs (Pack of 12)",
            brand = "Eggoz",
            description = "UV sanitized, chemical-free protein rich farm brown/white eggs.",
            unit = "12 pcs",
            price = 108.0,
            mrp = 130.0,
            discountPercent = 16,
            isPopular = true,
            emoji = "🥚"
        ),

        // Grocery & Staples
        Product(
            id = "gr_1",
            categoryId = "grocery",
            name = "Aashirvaad Shudh Chakki Atta",
            brand = "Aashirvaad",
            description = "100% whole wheat flour made from MP Sharbati grains. Makes rotis soft.",
            unit = "5 kg",
            price = 239.0,
            mrp = 275.0,
            discountPercent = 13,
            isPopular = true,
            isRecentlyPurchased = true,
            emoji = "🌾"
        ),
        Product(
            id = "gr_2",
            categoryId = "grocery",
            name = "Fortune Sunlite Refined Sunflower Oil",
            brand = "Fortune",
            description = "Light, non-sticky cooking oil enriched with Vitamins A and D.",
            unit = "1 L Pouch",
            price = 135.0,
            mrp = 165.0,
            discountPercent = 18,
            isDeal = true,
            emoji = "🌻"
        ),
        Product(
            id = "gr_3",
            categoryId = "grocery",
            name = "Tata Salt Vacuum Evaporated Iodised Salt",
            brand = "Tata",
            description = "Desh Ka Namak. Pure vacuum evaporated iodised cooking salt.",
            unit = "1 kg",
            price = 28.0,
            mrp = 30.0,
            discountPercent = 6,
            emoji = "🧂"
        ),
        Product(
            id = "gr_4",
            categoryId = "grocery",
            name = "India Gate Basmati Rice Feast Rozzana",
            brand = "India Gate",
            description = "Aged slender long-grain Basmati rice with authentic aromatic flavor.",
            unit = "5 kg",
            price = 425.0,
            mrp = 550.0,
            discountPercent = 22,
            isDeal = true,
            emoji = "🍚"
        ),

        // Snacks
        Product(
            id = "sn_1",
            categoryId = "snacks",
            name = "Lay's India's Magic Masala Chips",
            brand = "Lay's",
            description = "Crunchy ridged potato chips seasoned with Indian street masala spice blend.",
            unit = "73 g",
            price = 30.0,
            mrp = 30.0,
            discountPercent = 0,
            isPopular = true,
            emoji = "🥔"
        ),
        Product(
            id = "sn_2",
            categoryId = "snacks",
            name = "Haldiram's Bhujia Sev",
            brand = "Haldiram's",
            description = "Traditional crispy gram flour noodles spiced with red chilli & black pepper.",
            unit = "400 g",
            price = 115.0,
            mrp = 130.0,
            discountPercent = 11,
            isPopular = true,
            emoji = "🥨"
        ),
        Product(
            id = "sn_3",
            categoryId = "snacks",
            name = "Maggi 2-Minute Masala Noodles (Pack of 4)",
            brand = "Nestle",
            description = "India's favorite instant noodle with authentic blend of 10 aromatic spices.",
            unit = "280 g",
            price = 56.0,
            mrp = 60.0,
            discountPercent = 7,
            isPopular = true,
            isRecentlyPurchased = true,
            emoji = "🍜"
        ),

        // Beverages
        Product(
            id = "bev_1",
            categoryId = "beverages",
            name = "Nescafe Classic Instant Coffee Jar",
            brand = "Nescafe",
            description = "Rich aroma and bold signature roast coffee granules for morning vitality.",
            unit = "100 g",
            price = 299.0,
            mrp = 340.0,
            discountPercent = 12,
            isPopular = true,
            emoji = "☕"
        ),
        Product(
            id = "bev_2",
            categoryId = "beverages",
            name = "Coca-Cola Zero Sugar Chilled Can",
            brand = "Coca-Cola",
            description = "Iconic refreshing cola taste with crisp effervescence and zero sugar.",
            unit = "300 ml",
            price = 40.0,
            mrp = 40.0,
            discountPercent = 0,
            emoji = "🥤"
        ),
        Product(
            id = "bev_3",
            categoryId = "beverages",
            name = "Real Fruit Power Mixed Fruit Juice",
            brand = "Dabur",
            description = "Pure fruit juice packed with natural goodness of 9 chosen fruits.",
            unit = "1 L",
            price = 110.0,
            mrp = 135.0,
            discountPercent = 18,
            isDeal = true,
            emoji = "🍹"
        ),

        // Personal Care
        Product(
            id = "pc_1",
            categoryId = "personal_care",
            name = "Dettol Original Germ Protection Bathing Soap",
            brand = "Dettol",
            description = "Antibacterial protection soap with classic pine fragrance. Pack of 4.",
            unit = "4 x 125 g",
            price = 185.0,
            mrp = 220.0,
            discountPercent = 16,
            isPopular = true,
            emoji = "🧼"
        ),
        Product(
            id = "pc_2",
            categoryId = "personal_care",
            name = "Colgate Total Advanced Health Toothpaste",
            brand = "Colgate",
            description = "Antibacterial shield offering 12-hour protection against plaque and cavities.",
            unit = "150 g",
            price = 145.0,
            mrp = 175.0,
            discountPercent = 17,
            emoji = "🪥"
        ),

        // Home Care
        Product(
            id = "hc_1",
            categoryId = "home_care",
            name = "Surf Excel Easy Wash Detergent Powder",
            brand = "Surf Excel",
            description = "Superior stain removal power that dissolves fast in water like magic.",
            unit = "1 kg",
            price = 142.0,
            mrp = 160.0,
            discountPercent = 11,
            isPopular = true,
            emoji = "🧺"
        ),
        Product(
            id = "hc_2",
            categoryId = "home_care",
            name = "Vim Dishwash Gel Lemon Flavour",
            brand = "Vim",
            description = "One spoon cleans full sink of greasy utensils. Fresh lemon fragrance.",
            unit = "750 ml",
            price = 170.0,
            mrp = 205.0,
            discountPercent = 17,
            isDeal = true,
            emoji = "🧴"
        ),

        // Baby Care
        Product(
            id = "bc_1",
            categoryId = "baby_care",
            name = "Pampers All Round Protection Diaper Pants (M)",
            brand = "Pampers",
            description = "Ultra-absorb core with lotion containing Aloe Vera to prevent diaper rash.",
            unit = "36 Pants",
            price = 589.0,
            mrp = 749.0,
            discountPercent = 21,
            isPopular = true,
            emoji = "👶"
        ),
        Product(
            id = "bc_2",
            categoryId = "baby_care",
            name = "Himalaya Baby Wipes with Aloe Vera",
            brand = "Himalaya",
            description = "Alcohol and paraben-free gentle cleansing wipes for delicate baby skin.",
            unit = "72 Wipes",
            price = 145.0,
            mrp = 190.0,
            discountPercent = 24,
            emoji = "🧻"
        ),

        // Stationery
        Product(
            id = "st_1",
            categoryId = "stationery",
            name = "Classmate Pulse Spiral Notebook (Single Line)",
            brand = "ITC Classmate",
            description = "Smooth 70 GSM paper with durable poly cover and snag-free spiral binding.",
            unit = "300 Pages",
            price = 140.0,
            mrp = 160.0,
            discountPercent = 12,
            emoji = "📓"
        ),
        Product(
            id = "st_2",
            categoryId = "stationery",
            name = "Reynolds 045 Fine Carbure Ball Pens (Pack of 10)",
            brand = "Reynolds",
            description = "Smooth non-smudge blue ink ball pens designed for comfortable writing.",
            unit = "10 Pens",
            price = 90.0,
            mrp = 100.0,
            discountPercent = 10,
            emoji = "🖊️"
        ),

        // Mobile Accessories
        Product(
            id = "ma_1",
            categoryId = "mobile_acc",
            name = "boAt Dual Port 20W Fast USB-C Charger",
            brand = "boAt",
            description = "Smart IC power delivery fast charger compatible with iOS and Android devices.",
            unit = "1 pc",
            price = 499.0,
            mrp = 899.0,
            discountPercent = 44,
            isDeal = true,
            isPopular = true,
            emoji = "🔌"
        ),
        Product(
            id = "ma_2",
            categoryId = "mobile_acc",
            name = "Portronics 65W Braided Fast Charging Type-C Cable",
            brand = "Portronics",
            description = "Tangle-free 1.2m nylon braided cable with 480 Mbps high speed data transfer.",
            unit = "1.2 m",
            price = 199.0,
            mrp = 399.0,
            discountPercent = 50,
            isDeal = true,
            emoji = "⚡"
        )
    )

    val defaultCoupons = listOf(
        Coupon(
            code = "WELCOME50",
            discountType = DiscountType.FLAT,
            discountValue = 50.0,
            minimumOrder = 299.0,
            maximumDiscount = null,
            isActive = true,
            expiryDate = "31 Dec 2026",
            oneTimePerCustomer = true,
            usageLimit = 100,
            timesUsed = 0,
            description = "₹50 instant discount on orders above ₹299 (One-time use)"
        ),
        Coupon(
            code = "SAVE10",
            discountType = DiscountType.PERCENTAGE,
            discountValue = 10.0,
            minimumOrder = 499.0,
            maximumDiscount = 100.0,
            isActive = true,
            expiryDate = "31 Dec 2026",
            oneTimePerCustomer = false,
            usageLimit = 500,
            timesUsed = 0,
            description = "10% OFF up to ₹100 on orders above ₹499"
        ),
        Coupon(
            code = "SAVE75",
            discountType = DiscountType.FLAT,
            discountValue = 75.0,
            minimumOrder = 699.0,
            maximumDiscount = null,
            isActive = true,
            expiryDate = "31 Dec 2026",
            oneTimePerCustomer = false,
            usageLimit = 200,
            timesUsed = 0,
            description = "Flat ₹75 OFF on orders above ₹699"
        )
    )

    private val _availableCoupons = MutableStateFlow(defaultCoupons)
    val availableCoupons: StateFlow<List<Coupon>> = _availableCoupons.asStateFlow()
    val coupons: List<Coupon> get() = _availableCoupons.value

    private val defaultAddresses = listOf(
        Address(
            id = "addr_1",
            label = "Home",
            house = "Flat 402, Royal Palms Apartments",
            street = "12th Main Road, Sector 4",
            area = "HSR Layout",
            city = "Bengaluru",
            state = "Karnataka",
            pincode = "560102",
            isDefault = true,
            distanceKm = 2.8
        ),
        Address(
            id = "addr_2",
            label = "Work",
            house = "Tower B, 5th Floor, Tech Hub",
            street = "Outer Ring Road",
            area = "Bellandur",
            city = "Bengaluru",
            state = "Karnataka",
            pincode = "560103",
            isDefault = false,
            distanceKm = 6.8
        )
    )

    private val defaultDeliveryAreas = listOf(
        DeliveryArea(
            id = "122001",
            pincode = "122001",
            areaName = "Gurugram",
            city = "Gurugram",
            state = "Haryana",
            deliveryFee = 0.0,
            minimumOrderAmount = 199.0,
            estimatedDeliveryTime = "10–20 mins",
            isActive = true,
            minDistanceKm = 0.5,
            maxDistanceKm = 15.0,
            defaultDistanceKm = 2.5
        ),
        DeliveryArea(
            id = "560102",
            pincode = "560102",
            areaName = "HSR Layout",
            city = "Bengaluru",
            state = "Karnataka",
            deliveryFee = 0.0,
            minimumOrderAmount = 199.0,
            estimatedDeliveryTime = "10–15 mins",
            isActive = true,
            minDistanceKm = 0.5,
            maxDistanceKm = 10.0,
            defaultDistanceKm = 2.8
        ),
        DeliveryArea(
            id = "560038",
            pincode = "560038",
            areaName = "Indiranagar",
            city = "Bengaluru",
            state = "Karnataka",
            deliveryFee = 0.0,
            minimumOrderAmount = 199.0,
            estimatedDeliveryTime = "10–15 mins",
            isActive = true,
            minDistanceKm = 1.0,
            maxDistanceKm = 15.0,
            defaultDistanceKm = 5.2
        ),
        DeliveryArea(
            id = "560103",
            pincode = "560103",
            areaName = "Bellandur",
            city = "Bengaluru",
            state = "Karnataka",
            deliveryFee = 25.0,
            minimumOrderAmount = 199.0,
            estimatedDeliveryTime = "15–25 mins",
            isActive = true,
            minDistanceKm = 2.0,
            maxDistanceKm = 18.0,
            defaultDistanceKm = 6.8
        ),
        DeliveryArea(
            id = "560066",
            pincode = "560066",
            areaName = "Whitefield",
            city = "Bengaluru",
            state = "Karnataka",
            deliveryFee = 60.0,
            minimumOrderAmount = 249.0,
            estimatedDeliveryTime = "25–40 mins",
            isActive = true,
            minDistanceKm = 5.0,
            maxDistanceKm = 25.0,
            defaultDistanceKm = 14.5
        ),
        DeliveryArea(
            id = "122051",
            pincode = "122051",
            areaName = "Manesar South",
            city = "Gurugram",
            state = "Haryana",
            deliveryFee = 90.0,
            minimumOrderAmount = 399.0,
            estimatedDeliveryTime = "35–55 mins",
            isActive = true,
            minDistanceKm = 15.0,
            maxDistanceKm = 30.0,
            defaultDistanceKm = 22.0
        )
    )

    // Reactive State
    val products = MutableStateFlow(initialProducts)
    
    private val _cart = MutableStateFlow<Map<String, CartItem>>(emptyMap())
    val cart: StateFlow<Map<String, CartItem>> = _cart.asStateFlow()

    private val _wishlist = MutableStateFlow<Set<String>>(setOf("fv_2", "ma_1"))
    val wishlist: StateFlow<Set<String>> = _wishlist.asStateFlow()

    private val _addresses = MutableStateFlow(defaultAddresses)
    val addresses: StateFlow<List<Address>> = _addresses.asStateFlow()

    private val _deliveryAreas = MutableStateFlow<List<DeliveryArea>>(defaultDeliveryAreas)
    val deliveryAreas: StateFlow<List<DeliveryArea>> = _deliveryAreas.asStateFlow()

    private val _deliveryPricingConfig = MutableStateFlow(DeliveryPricingConfig())
    val deliveryPricingConfig: StateFlow<DeliveryPricingConfig> = _deliveryPricingConfig.asStateFlow()

    fun updateDeliveryPricingConfig(config: DeliveryPricingConfig) {
        _deliveryPricingConfig.value = config
    }

    fun resetDeliveryPricingConfigToDefaults() {
        _deliveryPricingConfig.value = DeliveryPricingConfig()
    }

    fun resolveDeliveryDistance(address: Address?, area: DeliveryArea?): Double {
        if (address?.distanceKm != null && address.distanceKm > 0.0) {
            return address.distanceKm
        }
        if (area != null && area.defaultDistanceKm > 0.0) {
            return area.defaultDistanceKm
        }
        if (address != null && (address.latitude != 0.0 || address.longitude != 0.0)) {
            val isBlr = address.city.contains("Bengaluru", ignoreCase = true) || address.pincode.startsWith("560")
            val hubLat = if (isBlr) 12.9352 else 28.4595
            val hubLng = if (isBlr) 77.6245 else 77.0266
            val dist = calculateHaversineDistance(hubLat, hubLng, address.latitude, address.longitude)
            if (dist in 0.5..60.0) {
                return (kotlin.math.round(dist * 10) / 10.0)
            }
        }
        return area?.defaultDistanceKm ?: 3.5
    }

    private fun calculateHaversineDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = kotlin.math.sin(dLat / 2) * kotlin.math.sin(dLat / 2) +
                kotlin.math.cos(Math.toRadians(lat1)) * kotlin.math.cos(Math.toRadians(lat2)) *
                kotlin.math.sin(dLon / 2) * kotlin.math.sin(dLon / 2)
        val c = 2 * kotlin.math.atan2(kotlin.math.sqrt(a), kotlin.math.sqrt(1 - a))
        return r * c
    }

    fun overrideOrderDeliveryFee(orderId: String, newDeliveryFee: Double) {
        val safeFee = newDeliveryFee.coerceAtLeast(0.0)
        _orders.update { list ->
            list.map { ord ->
                if (ord.orderId == orderId) {
                    val newTotal = (ord.subtotal + safeFee - ord.discount).coerceAtLeast(0.0)
                    ord.copy(
                        deliveryFee = safeFee,
                        isFreeDelivery = (safeFee == 0.0),
                        totalAmount = newTotal,
                        updatedAt = System.currentTimeMillis()
                    )
                } else ord
            }
        }
        repositoryScope.launch {
            firestoreRepo.updateOrderDeliveryFee(orderId, safeFee)
        }
    }

    private val _selectedAddress = MutableStateFlow(defaultAddresses[0])
    val selectedAddress: StateFlow<Address> = _selectedAddress.asStateFlow()

    private val _appliedCoupon = MutableStateFlow<Coupon?>(null)
    val appliedCoupon: StateFlow<Coupon?> = _appliedCoupon.asStateFlow()

    private val _returnPolicyConfig = MutableStateFlow(ReturnPolicyConfig())
    val returnPolicyConfig: StateFlow<ReturnPolicyConfig> = _returnPolicyConfig.asStateFlow()

    private val _customerNotifications = MutableStateFlow<List<CustomerNotification>>(
        listOf(
            CustomerNotification(
                id = "NOTIF-RET-01",
                customerId = "USR_101",
                title = "Return Request Under Review",
                message = "Your return request for Fresh Alphonso Mangoes (Order #SH-91450) is being inspected by our store team.",
                type = "RETURN_STATUS",
                referenceId = "SH-91450",
                timestamp = System.currentTimeMillis() - 3600000L * 4
            )
        )
    )
    val customerNotifications: StateFlow<List<CustomerNotification>> = _customerNotifications.asStateFlow()

    private val _orders = MutableStateFlow<List<Order>>(
        listOf(
            Order(
                orderId = "SH-78219",
                customerId = "USR_101",
                customerName = "Rahul Sharma",
                customerPhone = "+91 98765 12345",
                deliveryAddress = "Flat 402, Royal Palms Apartments, 12th Main Road, Sector 4, HSR Layout, Bengaluru - 560102",
                items = listOf(
                    OrderItem("db_1", "Amul Taaza Homogenised Toned Milk", "🥛", 2, 56.0, 112.0),
                    OrderItem("gr_1", "Aashirvaad Shudh Chakki Atta", "🌾", 1, 239.0, 239.0)
                ),
                subtotal = 351.0,
                deliveryFee = 0.0,
                discount = 50.0,
                couponCode = "WELCOME50",
                totalAmount = 301.0,
                paymentMethod = "Cash on Delivery",
                paymentStatus = "Paid",
                orderStatus = OrderStatus.DELIVERED,
                createdAt = System.currentTimeMillis() - 86400000L,
                deliveredAt = System.currentTimeMillis() - 86400000L + (18 * 60 * 1000L)
            ),
            Order(
                orderId = "SH-91450",
                customerId = "USR_101",
                customerName = "Rahul Sharma",
                customerPhone = "+91 98765 12345",
                deliveryAddress = "Flat 402, Royal Palms Apartments, 12th Main Road, Sector 4, HSR Layout, Bengaluru - 560102",
                items = listOf(
                    OrderItem("fv_2", "Fresh Alphonso Mangoes", "🥭", 1, 449.0, 449.0),
                    OrderItem("ma_1", "boAt Dual Port 20W Fast USB-C Charger", "🔌", 1, 499.0, 499.0)
                ),
                subtotal = 948.0,
                deliveryFee = 0.0,
                discount = 0.0,
                totalAmount = 948.0,
                paymentMethod = "Google Pay UPI",
                paymentStatus = "Paid",
                orderStatus = OrderStatus.DELIVERED,
                createdAt = System.currentTimeMillis() - (86400000L * 2),
                deliveredAt = System.currentTimeMillis() - (86400000L * 2) + (14 * 60 * 1000L)
            ),
            Order(
                orderId = "SH-64012",
                customerId = "USR_101",
                customerName = "Rahul Sharma",
                customerPhone = "+91 98765 12345",
                deliveryAddress = "Flat 402, Royal Palms Apartments, 12th Main Road, Sector 4, HSR Layout, Bengaluru - 560102",
                items = listOf(
                    OrderItem("fv_1", "Fresh Hybrid Tomatoes", "🍅", 1, 38.0, 38.0),
                    OrderItem("db_2", "Amul Salted Butter", "🧈", 1, 275.0, 275.0)
                ),
                subtotal = 313.0,
                deliveryFee = 0.0,
                discount = 0.0,
                totalAmount = 313.0,
                paymentMethod = "PhonePe UPI",
                paymentStatus = "Paid",
                orderStatus = OrderStatus.DELIVERED,
                createdAt = System.currentTimeMillis() - (86400000L * 5),
                deliveredAt = System.currentTimeMillis() - (86400000L * 5) + (12 * 60 * 1000L)
            )
        )
    )
    val orders: StateFlow<List<Order>> = _orders.asStateFlow()

    private val _returnRequests = MutableStateFlow<List<ReturnRequest>>(
        listOf(
            ReturnRequest(
                returnId = "RET-91450-01",
                orderId = "SH-91450",
                customerId = "USR_101",
                customerName = "Rahul Sharma",
                customerPhone = "+91 98765 12345",
                deliveryAddress = "Flat 402, Royal Palms Apartments, 12th Main Road, Sector 4, HSR Layout, Bengaluru - 560102",
                productId = "fv_2",
                productName = "Fresh Alphonso Mangoes",
                productImage = "🥭",
                productUnit = "6 pcs",
                returnQuantity = 1,
                unitPrice = 449.0,
                itemAmount = 449.0,
                refundAmount = 449.0,
                isDeliveryFeeRefunded = false,
                deliveryFeeRefundAmount = 0.0,
                refundMethod = "Original UPI (Google Pay)",
                refundStatus = "Pending",
                orderCreatedAt = System.currentTimeMillis() - (86400000L * 2),
                deliveredAt = System.currentTimeMillis() - (86400000L * 2) + (14 * 60 * 1000L),
                requestedAt = System.currentTimeMillis() - (3600000L * 5),
                returnDeadline = System.currentTimeMillis() - (86400000L * 2) + (3 * 86400000L),
                reason = "Product is damaged",
                customerDescription = "2 mangoes were squeezed and leaking pulp inside the carton box during transit.",
                photos = listOf("photo_damaged_mangoes_01.jpg", "photo_box_transit_02.jpg"),
                status = ReturnStatus.UNDER_REVIEW
            )
        )
    )
    val returnRequests: StateFlow<List<ReturnRequest>> = _returnRequests.asStateFlow()

    private val _payments = MutableStateFlow<List<Payment>>(
        listOf(
            Payment(
                paymentId = "PAY-78219",
                orderId = "SH-78219",
                customerId = "USR_101",
                customerName = "Rahul Sharma",
                amount = 301.0,
                paymentMethod = "Cash on Delivery",
                paymentStatus = PaymentStatus.SUCCESS,
                transactionRef = "TXN_78219001",
                createdAt = System.currentTimeMillis() - 86400000L
            )
        )
    )
    val payments: StateFlow<List<Payment>> = _payments.asStateFlow()

    private val _customers = MutableStateFlow<List<Customer>>(
        listOf(
            Customer(
                customerId = "USR_101",
                name = "Rahul Sharma",
                phone = "+91 98765 12345",
                email = "rahul.sharma@example.com",
                totalOrders = 1,
                totalSpending = 301.0,
                registeredAt = System.currentTimeMillis() - (86400000L * 15),
                isActive = true,
                role = UserRole.CUSTOMER
            ),
            Customer(
                customerId = "USR_ADMIN",
                name = "Sudhani Admin",
                phone = "+91 99999 00000",
                email = "admin@sudhanihub.com",
                totalOrders = 0,
                totalSpending = 0.0,
                registeredAt = System.currentTimeMillis() - (86400000L * 60),
                isActive = true,
                role = UserRole.ADMIN
            )
        )
    )
    val customers: StateFlow<List<Customer>> = _customers.asStateFlow()

    private val _deliveryPartners = MutableStateFlow<List<DeliveryPartner>>(
        listOf(
            DeliveryPartner(
                partnerId = "DP_101",
                name = "Ramesh Verma",
                phone = "+91 98765 43210",
                vehicle = "Electric Scooter (DL-3S-4412)",
                isAvailable = true,
                currentOrderId = null,
                activeDeliveriesCount = 0,
                rating = 4.92
            ),
            DeliveryPartner(
                partnerId = "DP_102",
                name = "Suresh Kumar",
                phone = "+91 98765 43211",
                vehicle = "Hero Electric Optima (KA-05-9821)",
                isAvailable = true,
                currentOrderId = null,
                activeDeliveriesCount = 0,
                rating = 4.88
            ),
            DeliveryPartner(
                partnerId = "DP_103",
                name = "Ankit Sharma",
                phone = "+91 98765 43212",
                vehicle = "Ather 450X (KA-03-7741)",
                isAvailable = true,
                currentOrderId = null,
                activeDeliveriesCount = 0,
                rating = 4.95
            )
        )
    )
    val deliveryPartners: StateFlow<List<DeliveryPartner>> = _deliveryPartners.asStateFlow()

    private val _currentActiveOrder = MutableStateFlow<Order?>(null)
    val currentActiveOrder: StateFlow<Order?> = _currentActiveOrder.asStateFlow()

    private var appContext: Context? = null
    private val _user = MutableStateFlow(UserProfile())
    val user: StateFlow<UserProfile> = _user.asStateFlow()

    fun init(
        context: Context,
        orderRepo: OrderRepository,
        couponRepo: CouponRepository,
        paymentRepo: PaymentRepository,
        customerRepo: CustomerRepository,
        partnerRepo: DeliveryPartnerRepository,
        productRepo: ProductRepository,
        savedPaymentRepo: SavedPaymentMethodRepository? = null,
        returnRepo: ReturnRepository? = null
    ) {
        this.appContext = context.applicationContext
        this.orderRepository = orderRepo
        this.couponRepository = couponRepo
        this.paymentRepository = paymentRepo
        this.customerRepository = customerRepo
        this.partnerRepository = partnerRepo
        this.productRepository = productRepo
        this.savedPaymentRepository = savedPaymentRepo
        this.returnRepository = returnRepo

        // Restore user profile and profile picture from persistent storage
        try {
            val restoredUser = com.example.util.ProfileImageManager.loadPersistedUserProfile(context, _user.value)
            _user.value = restoredUser
        } catch (e: Exception) {
            e.printStackTrace()
        }

        savedPaymentRepo?.let { repo ->
            repositoryScope.launch {
                repo.getCustomerSavedMethods("USR_101").collect { roomSavedMethods ->
                    if (roomSavedMethods.isNotEmpty()) {
                        _savedPaymentMethods.value = roomSavedMethods
                    }
                }
            }
        }

        repositoryScope.launch {
            orderRepo.getAllOrders().collect { roomOrders ->
                if (roomOrders.isNotEmpty()) {
                    _orders.value = roomOrders
                }
            }
        }

        repositoryScope.launch {
            couponRepo.getAllCoupons().collect { roomCoupons ->
                if (roomCoupons.isNotEmpty()) {
                    _availableCoupons.value = roomCoupons
                }
            }
        }

        repositoryScope.launch {
            paymentRepo.getAllPayments().collect { roomPayments ->
                if (roomPayments.isNotEmpty()) {
                    _payments.value = roomPayments
                }
            }
        }

        repositoryScope.launch {
            customerRepo.getAllCustomers().collect { roomCustomers ->
                if (roomCustomers.isNotEmpty()) {
                    _customers.value = roomCustomers
                }
            }
        }

        repositoryScope.launch {
            partnerRepo.getAllPartners().collect { roomPartners ->
                if (roomPartners.isNotEmpty()) {
                    _deliveryPartners.value = roomPartners
                }
            }
        }

        returnRepo?.let { repo ->
            repositoryScope.launch {
                repo.getAllReturnRequests().collect { roomRequests ->
                    if (roomRequests.isNotEmpty()) {
                        _returnRequests.value = roomRequests
                    }
                }
            }
        }

        // Real-time Firestore synchronization
        firestoreRepo.listenToOrders { firestoreOrders ->
            if (firestoreOrders.isNotEmpty()) {
                _orders.value = firestoreOrders
                val active = firestoreOrders.firstOrNull { it.orderStatus != OrderStatus.DELIVERED && it.orderStatus != OrderStatus.CANCELLED }
                if (active != null) {
                    _currentActiveOrder.value = active
                }
            }
        }

        firestoreRepo.listenToProducts { firestoreProducts ->
            if (firestoreProducts.isNotEmpty()) {
                products.value = firestoreProducts
            }
        }

        firestoreRepo.listenToDeliveryAreas { firestoreDeliveryAreas ->
            if (firestoreDeliveryAreas.isNotEmpty()) {
                _deliveryAreas.value = firestoreDeliveryAreas
            }
        }

        repositoryScope.launch {
            firestoreRepo.seedInitialDeliveryAreasIfEmpty()
        }
    }

    // Cart Operations
    fun addToCart(product: Product): Boolean {
        if (product.stock <= 0) return false
        val currentQty = _cart.value[product.id]?.quantity ?: 0
        if (currentQty >= product.stock) return false
        _cart.update { current ->
            val newQty = currentQty + 1
            current + (product.id to CartItem(product, newQty))
        }
        return true
    }

    fun decrementQuantity(productId: String) {
        _cart.update { current ->
            val existing = current[productId] ?: return@update current
            if (existing.quantity <= 1) {
                current - productId
            } else {
                current + (productId to existing.copy(quantity = existing.quantity - 1))
            }
        }
    }

    fun removeFromCart(productId: String) {
        _cart.update { current -> current - productId }
    }

    fun clearCart() {
        _cart.value = emptyMap()
        _appliedCoupon.value = null
    }

    fun toggleWishlist(productId: String) {
        _wishlist.update { current ->
            if (current.contains(productId)) current - productId else current + productId
        }
    }

    fun applyCoupon(code: String): Boolean {
        val coupon = _availableCoupons.value.find { it.code.equals(code.trim(), ignoreCase = true) }
        return if (coupon != null && coupon.isActive) {
            _appliedCoupon.value = coupon
            true
        } else {
            false
        }
    }

    fun removeCoupon() {
        _appliedCoupon.value = null
    }

    fun addAddress(address: Address) {
        _addresses.update { it + address }
        if (address.isDefault || _addresses.value.size == 1) {
            _selectedAddress.value = address
        }
    }

    fun selectAddress(address: Address) {
        _selectedAddress.value = address
    }

    data class ServiceabilityResult(
        val isServiceable: Boolean,
        val deliveryArea: DeliveryArea? = null,
        val message: String
    )

    fun getDeliveryAreaForPincode(pincode: String): DeliveryArea? {
        val cleanPin = pincode.trim()
        if (cleanPin.isBlank()) return null
        return _deliveryAreas.value.firstOrNull { it.pincode.trim() == cleanPin }
    }

    fun checkServiceability(pincode: String): ServiceabilityResult {
        val cleanPin = pincode.trim()
        if (cleanPin.isBlank()) {
            return ServiceabilityResult(
                isServiceable = false,
                deliveryArea = null,
                message = "Please enter a valid PIN code"
            )
        }
        val area = getDeliveryAreaForPincode(cleanPin)
        return if (area != null && area.isActive) {
            ServiceabilityResult(
                isServiceable = true,
                deliveryArea = area,
                message = "✓ Delivery available"
            )
        } else {
            ServiceabilityResult(
                isServiceable = false,
                deliveryArea = area,
                message = "Sorry, we don't deliver to this area yet."
            )
        }
    }

    fun addDeliveryArea(area: DeliveryArea) {
        val docId = if (area.id.isNotBlank()) area.id else if (area.pincode.isNotBlank()) area.pincode.trim() else "area_${System.currentTimeMillis()}"
        val normalized = area.copy(id = docId, pincode = area.pincode.trim())
        _deliveryAreas.update { current ->
            val filtered = current.filterNot { it.id == normalized.id || (it.pincode.trim() == normalized.pincode && normalized.pincode.isNotBlank()) }
            listOf(normalized) + filtered
        }
        repositoryScope.launch {
            firestoreRepo.saveDeliveryArea(normalized)
        }
    }

    fun updateDeliveryArea(area: DeliveryArea) {
        _deliveryAreas.update { current ->
            current.map { if (it.id == area.id) area else it }
        }
        repositoryScope.launch {
            firestoreRepo.saveDeliveryArea(area)
        }
    }

    fun deleteDeliveryArea(areaId: String) {
        _deliveryAreas.update { current ->
            current.filterNot { it.id == areaId }
        }
        repositoryScope.launch {
            firestoreRepo.deleteDeliveryArea(areaId)
        }
    }

    fun toggleDeliveryAreaStatus(areaId: String) {
        var targetStatus: Boolean? = null
        _deliveryAreas.update { current ->
            current.map {
                if (it.id == areaId) {
                    val toggled = !it.isActive
                    targetStatus = toggled
                    it.copy(isActive = toggled, updatedAt = System.currentTimeMillis())
                } else it
            }
        }
        targetStatus?.let { status ->
            repositoryScope.launch {
                firestoreRepo.updateDeliveryAreaStatus(areaId, status)
            }
        }
    }

    private fun parseEtaMinutes(eta: String?): Int {
        if (eta.isNullOrBlank()) return 11
        val numbers = Regex("\\d+").findAll(eta).map { it.value.toIntOrNull() ?: 11 }.toList()
        return if (numbers.isNotEmpty()) numbers.last() else 11
    }

    fun placeOrder(
        paymentMethod: String,
        address: Address? = null,
        customPaymentStatus: PaymentStatus? = null,
        customTransactionRef: String? = null
    ): Order {
        val cartItems = _cart.value.values.toList()
        val subtotal = cartItems.sumOf { it.product.price * it.quantity }
        val targetAddress = address ?: _selectedAddress.value
        val area = getDeliveryAreaForPincode(targetAddress.pincode)

        val totalWeightGrams = cartItems.sumOf { it.product.calculateWeightInGrams() * it.quantity }
        val totalWeightKg = (totalWeightGrams / 1000.0).coerceAtLeast(0.1)
        val distanceKm = resolveDeliveryDistance(targetAddress, area)

        val priceBreakdown = PriceCalculationService.calculatePrice(
            subtotal = subtotal,
            coupon = _appliedCoupon.value,
            deliveryArea = area,
            distanceKm = distanceKm,
            totalWeightKg = totalWeightKg,
            pricingConfig = _deliveryPricingConfig.value
        )
        
        val orderItems = cartItems.map {
            OrderItem(
                productId = it.product.id,
                productName = it.product.name,
                productImage = it.product.emoji,
                quantity = it.quantity,
                price = it.product.price,
                total = it.product.price * it.quantity
            )
        }

        val generatedId = "SH-${(10000..99999).random()}"
        val user = _user.value
        val deliveryAddr = targetAddress.fullAddress

        // Select an available delivery partner or fallback
        val partner = _deliveryPartners.value.firstOrNull { it.isAvailable }
            ?: _deliveryPartners.value.firstOrNull()
            ?: DeliveryPartner("DP_101", "Ramesh Verma", "+91 98765 43210", "Electric Scooter (DL-3S-4412)")

        val isPendingPayment = paymentMethod.contains("Cash", ignoreCase = true) || paymentMethod == "COD"
        val paymentStatusEnum = customPaymentStatus ?: (if (isPendingPayment) PaymentStatus.PENDING else PaymentStatus.PAID)
        val paymentStatusStr = paymentStatusEnum.displayName

        val newOrder = Order(
            orderId = generatedId,
            customerId = user.id,
            customerName = user.name,
            customerPhone = user.phone,
            deliveryAddress = deliveryAddr,
            items = orderItems,
            subtotal = priceBreakdown.subtotal,
            deliveryFee = priceBreakdown.deliveryFee,
            discount = priceBreakdown.discount,
            couponCode = priceBreakdown.couponCode,
            totalAmount = priceBreakdown.finalTotal,
            paymentMethod = paymentMethod,
            paymentStatus = paymentStatusStr,
            orderStatus = OrderStatus.NEW,
            createdAt = System.currentTimeMillis(),
            deliveryPartnerId = partner.partnerId,
            deliveryPartnerName = partner.name,
            deliveryPartnerPhone = partner.phone,
            deliveryPartnerVehicle = partner.vehicle,
            estimatedDeliveryMinutes = parseEtaMinutes(area?.estimatedDeliveryTime),
            deliveryOtp = "${(1000..9999).random()}",
            deliveryArea = area?.areaName ?: targetAddress.area,
            pincode = targetAddress.pincode,
            deliveryDistanceKm = distanceKm,
            totalWeightKg = totalWeightKg,
            isFreeDelivery = priceBreakdown.isFreeDelivery
        )

        val payment = Payment(
            paymentId = "PAY-${generatedId.removePrefix("SH-")}",
            orderId = generatedId,
            customerId = user.id,
            customerName = user.name,
            amount = priceBreakdown.finalTotal,
            paymentMethod = paymentMethod,
            paymentStatus = paymentStatusEnum,
            transactionRef = customTransactionRef ?: "TXN_${System.currentTimeMillis().toString().takeLast(8)}",
            createdAt = System.currentTimeMillis()
        )

        // Save into StateFlow immediately
        _orders.update { listOf(newOrder) + it }
        _currentActiveOrder.value = newOrder
        _payments.update { listOf(payment) + it }

        // Update customer total orders and total spending
        _customers.update { list ->
            val existing = list.find { it.customerId == user.id }
            if (existing != null) {
                list.map {
                    if (it.customerId == user.id) {
                        it.copy(
                            totalOrders = it.totalOrders + 1,
                            totalSpending = it.totalSpending + priceBreakdown.finalTotal
                        )
                    } else it
                }
            } else {
                listOf(
                    Customer(
                        customerId = user.id,
                        name = user.name,
                        phone = user.phone,
                        email = user.email,
                        totalOrders = 1,
                        totalSpending = priceBreakdown.finalTotal,
                        registeredAt = System.currentTimeMillis(),
                        isActive = true,
                        role = user.role
                    )
                ) + list
            }
        }

        // Deduct products stock
        cartItems.forEach { item ->
            updateProductStock(item.product.id, (item.product.stock - item.quantity).coerceAtLeast(0))
        }

        // Mark partner as busy with this order
        _deliveryPartners.update { list ->
            list.map {
                if (it.partnerId == partner.partnerId) {
                    it.copy(isAvailable = false, currentOrderId = generatedId, activeDeliveriesCount = it.activeDeliveriesCount + 1)
                } else it
            }
        }

        // Persist into Room Database & Firestore asynchronously
        repositoryScope.launch {
            try {
                orderRepository?.createOrder(newOrder)
                paymentRepository?.recordPayment(payment)
                customerRepository?.incrementCustomerStats(user.id, priceBreakdown.finalTotal)
                partnerRepository?.updateAssignment(partner.partnerId, false, generatedId, partner.activeDeliveriesCount + 1)
                firestoreRepo.saveOrder(newOrder, payment)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        clearCart()
        return newOrder
    }

    fun updateOrderStatus(orderId: String, newStatus: OrderStatus) {
        var updatedOrder: Order? = null
        _orders.update { list ->
            list.map { ord ->
                if (ord.orderId == orderId) {
                    val isNowDelivered = newStatus == OrderStatus.DELIVERED
                    val newDeliveredAt = if (isNowDelivered) (ord.deliveredAt ?: System.currentTimeMillis()) else ord.deliveredAt
                    val newPaymentStatus = if (isNowDelivered && ord.paymentStatus.equals("Pending", ignoreCase = true)) "Paid" else ord.paymentStatus
                    val updated = ord.copy(
                        orderStatus = newStatus,
                        paymentStatus = newPaymentStatus,
                        deliveredAt = newDeliveredAt,
                        updatedAt = System.currentTimeMillis()
                    )
                    if (_currentActiveOrder.value?.orderId == orderId) {
                        _currentActiveOrder.value = updated
                    }
                    updatedOrder = updated
                    updated
                } else ord
            }
        }

        // Synchronize payment status if delivered or cancelled
        if (newStatus == OrderStatus.DELIVERED) {
            _payments.update { list ->
                list.map { pay ->
                    if (pay.orderId == orderId && pay.paymentStatus == PaymentStatus.PENDING) {
                        pay.copy(paymentStatus = PaymentStatus.SUCCESS)
                    } else pay
                }
            }
            // Free the partner
            updatedOrder?.deliveryPartnerId?.let { partnerId ->
                _deliveryPartners.update { list ->
                    list.map { if (it.partnerId == partnerId) it.copy(isAvailable = true, currentOrderId = null) else it }
                }
            }
        } else if (newStatus == OrderStatus.CANCELLED) {
            _payments.update { list ->
                list.map { pay ->
                    if (pay.orderId == orderId && pay.paymentStatus == PaymentStatus.SUCCESS) {
                        pay.copy(paymentStatus = PaymentStatus.REFUNDED)
                    } else pay
                }
            }
        }

        repositoryScope.launch {
            try {
                orderRepository?.updateOrderStatus(orderId, newStatus)
                firestoreRepo.updateOrderStatus(orderId, newStatus)
                if (newStatus == OrderStatus.DELIVERED) {
                    val matchingPayment = _payments.value.find { it.orderId == orderId }
                    if (matchingPayment != null) {
                        paymentRepository?.updatePaymentStatus(matchingPayment.paymentId, PaymentStatus.SUCCESS)
                    }
                    updatedOrder?.deliveryPartnerId?.let { pId ->
                        val p = _deliveryPartners.value.find { it.partnerId == pId }
                        partnerRepository?.updateAssignment(pId, true, null, (p?.activeDeliveriesCount ?: 1) - 1)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun updatePaymentStatus(paymentId: String, newStatus: PaymentStatus) {
        _payments.update { list ->
            list.map { if (it.paymentId == paymentId) it.copy(paymentStatus = newStatus) else it }
        }
        // Also update matching order payment status
        val payment = _payments.value.find { it.paymentId == paymentId }
        if (payment != null) {
            _orders.update { list ->
                list.map { if (it.orderId == payment.orderId) it.copy(paymentStatus = newStatus.displayName) else it }
            }
        }

        repositoryScope.launch {
            try {
                paymentRepository?.updatePaymentStatus(paymentId, newStatus)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun toggleCustomerStatus(customerId: String, isActive: Boolean) {
        _customers.update { list ->
            list.map { if (it.customerId == customerId) it.copy(isActive = isActive) else it }
        }
        repositoryScope.launch {
            try {
                customerRepository?.toggleCustomerStatus(customerId, isActive)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun assignDeliveryPartner(orderId: String, partnerId: String) {
        val partner = _deliveryPartners.value.find { it.partnerId == partnerId } ?: return
        _orders.update { list ->
            list.map { ord ->
                if (ord.orderId == orderId) {
                    ord.copy(
                        deliveryPartnerId = partner.partnerId,
                        deliveryPartnerName = partner.name,
                        deliveryPartnerPhone = partner.phone,
                        deliveryPartnerVehicle = partner.vehicle
                    )
                } else ord
            }
        }
        if (_currentActiveOrder.value?.orderId == orderId) {
            _currentActiveOrder.value = _orders.value.find { it.orderId == orderId }
        }
        _deliveryPartners.update { list ->
            list.map {
                if (it.partnerId == partnerId) {
                    it.copy(isAvailable = false, currentOrderId = orderId, activeDeliveriesCount = it.activeDeliveriesCount + 1)
                } else it
            }
        }
        repositoryScope.launch {
            try {
                orderRepository?.updateDeliveryPartner(
                    orderId = orderId,
                    partnerId = partner.partnerId,
                    partnerName = partner.name,
                    partnerPhone = partner.phone,
                    partnerVehicle = partner.vehicle
                )
                partnerRepository?.updateAssignment(partnerId, false, orderId, partner.activeDeliveriesCount + 1)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun addDeliveryPartner(partner: DeliveryPartner) {
        _deliveryPartners.update { listOf(partner) + it }
        repositoryScope.launch {
            try {
                partnerRepository?.savePartner(partner)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun addProduct(product: Product) {
        // Default new products to INACTIVE, isVisible = false as required by specification
        val securedProduct = product.copy(
            isActive = false,
            isVisible = false,
            listingStatus = ListingStatus.INACTIVE,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        products.update { listOf(securedProduct) + it }
        repositoryScope.launch {
            try {
                productRepository?.saveProduct(securedProduct)
                firestoreRepo.saveProduct(securedProduct)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun updateProduct(product: Product) {
        val safeStock = product.stock.coerceAtLeast(0)
        val status = if (safeStock == 0) {
            ListingStatus.OUT_OF_STOCK
        } else if (product.isActive && product.isVisible) {
            ListingStatus.ACTIVE
        } else if (!product.isVisible) {
            ListingStatus.HIDDEN
        } else {
            ListingStatus.INACTIVE
        }
        val updated = product.copy(stock = safeStock, listingStatus = status, updatedAt = System.currentTimeMillis())
        products.update { list ->
            list.map { if (it.id == product.id) updated else it }
        }
        repositoryScope.launch {
            try {
                productRepository?.updateProduct(updated)
                firestoreRepo.saveProduct(updated)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun publishProduct(productId: String) {
        products.update { list ->
            list.map {
                if (it.id == productId) {
                    val newStatus = if (it.stock > 0) ListingStatus.ACTIVE else ListingStatus.OUT_OF_STOCK
                    it.copy(isActive = true, listingStatus = newStatus, updatedAt = System.currentTimeMillis())
                } else it
            }
        }
        products.value.find { it.id == productId }?.let { prod ->
            repositoryScope.launch {
                try {
                    productRepository?.updateProduct(prod)
                    firestoreRepo.saveProduct(prod)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    fun unpublishProduct(productId: String) {
        products.update { list ->
            list.map {
                if (it.id == productId) {
                    it.copy(isActive = false, listingStatus = ListingStatus.INACTIVE, updatedAt = System.currentTimeMillis())
                } else it
            }
        }
        products.value.find { it.id == productId }?.let { prod ->
            repositoryScope.launch {
                try {
                    productRepository?.updateProduct(prod)
                    firestoreRepo.saveProduct(prod)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    fun showProduct(productId: String) {
        products.update { list ->
            list.map {
                if (it.id == productId) {
                    val newStatus = if (!it.isActive) ListingStatus.INACTIVE else if (it.stock <= 0) ListingStatus.OUT_OF_STOCK else ListingStatus.ACTIVE
                    it.copy(isVisible = true, listingStatus = newStatus, updatedAt = System.currentTimeMillis())
                } else it
            }
        }
        products.value.find { it.id == productId }?.let { prod ->
            repositoryScope.launch {
                try {
                    productRepository?.updateProduct(prod)
                    firestoreRepo.saveProduct(prod)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    fun hideProduct(productId: String) {
        products.update { list ->
            list.map {
                if (it.id == productId) {
                    it.copy(isVisible = false, listingStatus = ListingStatus.HIDDEN, updatedAt = System.currentTimeMillis())
                } else it
            }
        }
        products.value.find { it.id == productId }?.let { prod ->
            repositoryScope.launch {
                try {
                    productRepository?.updateProduct(prod)
                    firestoreRepo.saveProduct(prod)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    fun deleteProduct(productId: String) {
        products.update { list ->
            list.filterNot { it.id == productId }
        }
        repositoryScope.launch {
            try {
                productRepository?.deleteProduct(productId)
                firestoreRepo.deleteProduct(productId)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun updateProductDetails(
        productId: String,
        name: String,
        categoryId: String,
        price: Double,
        mrp: Double,
        discountPercent: Int,
        stock: Int,
        emoji: String
    ) {
        products.update { list ->
            list.map {
                if (it.id == productId) {
                    it.copy(
                        name = name,
                        categoryId = categoryId,
                        price = price,
                        mrp = mrp,
                        discountPercent = discountPercent,
                        stock = stock,
                        emoji = emoji
                    )
                } else it
            }
        }
        val updated = products.value.find { it.id == productId }
        if (updated != null) {
            repositoryScope.launch {
                try {
                    productRepository?.updateProduct(updated)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    fun loginOrRegisterCustomer(name: String, phone: String, email: String, role: UserRole = UserRole.CUSTOMER) {
        val existing = _customers.value.find { it.phone == phone }
        val customerId = existing?.customerId ?: "USR_${(100..999).random()}"
        val updatedRole = if (phone == "+91 99999 00000" || email.contains("admin", ignoreCase = true)) UserRole.ADMIN else role

        val customer = Customer(
            customerId = customerId,
            name = name,
            phone = phone,
            email = email,
            totalOrders = existing?.totalOrders ?: 0,
            totalSpending = existing?.totalSpending ?: 0.0,
            registeredAt = existing?.registeredAt ?: System.currentTimeMillis(),
            isActive = true,
            role = updatedRole
        )

        _customers.update { list ->
            val index = list.indexOfFirst { it.customerId == customerId }
            if (index >= 0) {
                list.toMutableList().apply { set(index, customer) }
            } else {
                listOf(customer) + list
            }
        }

        _user.value = UserProfile(
            id = customerId,
            name = name,
            phone = phone,
            email = email,
            isVerified = true,
            role = updatedRole
        )

        repositoryScope.launch {
            try {
                customerRepository?.saveCustomer(customer)
                firebaseAuth.registerOrUpdateCustomer(
                    name = name,
                    phone = phone,
                    email = email,
                    role = updatedRole
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun switchUserRole(role: UserRole) {
        _user.update { it.copy(role = role) }
    }

    fun advanceOrderStatus(orderId: String) {
        val currentOrder = _orders.value.find { it.orderId == orderId } ?: return
        val nextStatus = when (currentOrder.orderStatus) {
            OrderStatus.NEW -> OrderStatus.CONFIRMED
            OrderStatus.CONFIRMED -> OrderStatus.PREPARING
            OrderStatus.PREPARING -> OrderStatus.READY_FOR_PICKUP
            OrderStatus.READY_FOR_PICKUP -> OrderStatus.OUT_FOR_DELIVERY
            OrderStatus.OUT_FOR_DELIVERY -> OrderStatus.DELIVERED
            OrderStatus.DELIVERED -> OrderStatus.DELIVERED
            OrderStatus.CANCELLED -> OrderStatus.CANCELLED
        }
        updateOrderStatus(orderId, nextStatus)
    }

    fun addOrUpdateCoupon(coupon: Coupon) {
        _availableCoupons.update { list ->
            val index = list.indexOfFirst { it.code.equals(coupon.code, ignoreCase = true) }
            if (index >= 0) {
                list.toMutableList().apply { set(index, coupon) }
            } else {
                listOf(coupon) + list
            }
        }
        repositoryScope.launch {
            try {
                couponRepository?.addCoupon(coupon)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun toggleCouponActive(code: String, isActive: Boolean) {
        _availableCoupons.update { list ->
            list.map { if (it.code.equals(code, ignoreCase = true)) it.copy(isActive = isActive) else it }
        }
        repositoryScope.launch {
            try {
                couponRepository?.toggleCouponStatus(code, isActive)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun updateUserProfile(
        name: String,
        phone: String,
        email: String,
        profilePicturePath: String? = _user.value.profilePicturePath
    ) {
        val updated = _user.value.copy(
            name = name,
            phone = phone,
            email = email,
            profilePicturePath = profilePicturePath
        )
        _user.value = updated
        appContext?.let { ctx ->
            com.example.util.ProfileImageManager.persistUserProfile(ctx, updated)
        }
    }

    fun updateProfilePicture(path: String?) {
        val updated = _user.value.copy(profilePicturePath = path)
        _user.value = updated
        appContext?.let { ctx ->
            com.example.util.ProfileImageManager.persistUserProfile(ctx, updated)
        }
    }

    fun removeProfilePicture() {
        appContext?.let { ctx ->
            com.example.util.ProfileImageManager.removeProfilePicture(ctx)
        }
        val updated = _user.value.copy(profilePicturePath = null)
        _user.value = updated
        appContext?.let { ctx ->
            com.example.util.ProfileImageManager.persistUserProfile(ctx, updated)
        }
    }

    // User Wallet
    private val _walletBalance = MutableStateFlow(450.0)
    val walletBalance: StateFlow<Double> = _walletBalance.asStateFlow()

    private val _walletTransactions = MutableStateFlow(
        listOf(
            WalletTransaction(
                id = "WTX_101",
                title = "Welcome Cashback",
                amount = 50.0,
                isCredit = true,
                date = "Today, 11:30 AM",
                description = "Cashback credited on order #SH-78219"
            ),
            WalletTransaction(
                id = "WTX_102",
                title = "Wallet Top-up",
                amount = 500.0,
                isCredit = true,
                date = "Yesterday, 04:15 PM",
                description = "Added via UPI (Google Pay)"
            ),
            WalletTransaction(
                id = "WTX_103",
                title = "Grocery Order #SH-74120",
                amount = 100.0,
                isCredit = false,
                date = "28 Aug 2026",
                description = "Paid from Sudhani Wallet"
            )
        )
    )
    val walletTransactions: StateFlow<List<WalletTransaction>> = _walletTransactions.asStateFlow()

    fun addMoneyToWallet(amount: Double) {
        if (amount <= 0) return
        _walletBalance.update { it + amount }
        val newTxn = WalletTransaction(
            id = "WTX_${System.currentTimeMillis().toString().takeLast(6)}",
            title = "Wallet Top-up",
            amount = amount,
            isCredit = true,
            date = "Just now",
            description = "Instant top-up via UPI"
        )
        _walletTransactions.update { listOf(newTxn) + it }
    }

    // Shopping Lists
    private val _shoppingLists = MutableStateFlow(
        listOf(
            ShoppingList(
                id = "SL_1",
                name = "Weekly Essentials",
                description = "Milk, curd, bread, bananas, farm eggs",
                itemNames = listOf("Amul Taaza Milk", "Fresh Brown Eggs (6 pcs)", "Harvest Gold Bread", "Robusta Bananas"),
                estimatedTotal = 245.0,
                iconEmoji = "🥛"
            ),
            ShoppingList(
                id = "SL_2",
                name = "Monthly Grocery Staples",
                description = "Atta, basmati rice, dal, sunflower oil",
                itemNames = listOf("Aashirvaad Chakki Atta 5kg", "India Gate Basmati Rice", "Tata Sampann Toor Dal", "Fortune Sunflower Oil"),
                estimatedTotal = 1280.0,
                iconEmoji = "🌾"
            ),
            ShoppingList(
                id = "SL_3",
                name = "Pooja & Festive Prep",
                description = "Camphor, agarbatti, fresh flowers, ghee",
                itemNames = listOf("Mangaldeep Agarbatti", "Patanjali Cow Ghee 500ml", "Pure Bhimseni Camphor"),
                estimatedTotal = 390.0,
                iconEmoji = "🪔"
            )
        )
    )
    val shoppingLists: StateFlow<List<ShoppingList>> = _shoppingLists.asStateFlow()

    fun addShoppingList(name: String, description: String, items: List<String>, estimatedTotal: Double, iconEmoji: String = "🛒") {
        val newList = ShoppingList(
            id = "SL_${System.currentTimeMillis().toString().takeLast(6)}",
            name = name,
            description = description,
            itemNames = items,
            estimatedTotal = estimatedTotal,
            iconEmoji = iconEmoji
        )
        _shoppingLists.update { listOf(newList) + it }
    }

    fun deleteShoppingList(id: String) {
        _shoppingLists.update { it.filter { list -> list.id != id } }
    }

    // Saved Payments
    private val _savedPaymentMethods = MutableStateFlow(
        listOf(
            SavedPaymentMethod(
                id = "SPM_GPAY_01",
                customerId = "USR_101",
                title = "Google Pay UPI",
                subtitle = "sanaulla@oksbi",
                type = "UPI",
                bankOrProvider = "Google Pay",
                maskedDetails = "sanaulla@oksbi",
                upiVpa = "sanaulla@oksbi",
                isDefault = true,
                createdAt = System.currentTimeMillis() - 86400000 * 2
            ),
            SavedPaymentMethod(
                id = "SPM_PHONEPE_02",
                customerId = "USR_101",
                title = "PhonePe UPI",
                subtitle = "9508700923@ybl",
                type = "UPI",
                bankOrProvider = "PhonePe",
                maskedDetails = "9508700923@ybl",
                upiVpa = "9508700923@ybl",
                isDefault = false,
                createdAt = System.currentTimeMillis() - 86400000
            ),
            SavedPaymentMethod(
                id = "SPM_CARD_01",
                customerId = "USR_101",
                title = "HDFC Bank Platinum Debit Card",
                subtitle = "•••• •••• •••• 4921",
                type = "DEBIT_CARD",
                bankOrProvider = "HDFC Bank",
                maskedDetails = "•••• •••• •••• 4921",
                lastFourDigits = "4921",
                cardNetwork = "VISA",
                cardExpiry = "08/28",
                gatewayTokenRef = "tok_rzp_hdfc_4921",
                isDefault = false,
                createdAt = System.currentTimeMillis() - 3600000 * 12
            ),
            SavedPaymentMethod(
                id = "SPM_CARD_02",
                customerId = "USR_101",
                title = "ICICI Amazon Pay Credit Card",
                subtitle = "•••• •••• •••• 8014",
                type = "CREDIT_CARD",
                bankOrProvider = "ICICI Bank",
                maskedDetails = "•••• •••• •••• 8014",
                lastFourDigits = "8014",
                cardNetwork = "MASTERCARD",
                cardExpiry = "11/29",
                gatewayTokenRef = "tok_rzp_icici_8014",
                isDefault = false,
                createdAt = System.currentTimeMillis() - 3600000 * 6
            )
        )
    )
    val savedPaymentMethods: StateFlow<List<SavedPaymentMethod>> = _savedPaymentMethods.asStateFlow()

    fun addSavedPaymentMethod(method: SavedPaymentMethod) {
        _savedPaymentMethods.update { list ->
            val updated = if (method.isDefault) {
                listOf(method) + list.map { it.copy(isDefault = false) }
            } else {
                listOf(method) + list
            }
            updated
        }
        repositoryScope.launch {
            savedPaymentRepository?.addSavedMethod(method)
            if (method.isDefault) {
                savedPaymentRepository?.setDefaultPaymentMethod(method.id, method.customerId)
            }
        }
    }

    fun deleteSavedPaymentMethod(id: String) {
        _savedPaymentMethods.update { it.filter { m -> m.id != id } }
        repositoryScope.launch {
            savedPaymentRepository?.deleteSavedMethod(id)
        }
    }

    fun setDefaultPaymentMethod(id: String) {
        _savedPaymentMethods.update { list ->
            list.map { it.copy(isDefault = it.id == id) }
        }
        repositoryScope.launch {
            savedPaymentRepository?.setDefaultPaymentMethod(id, "USR_101")
        }
    }

    // User Ratings & Reviews
    private val _userReviews = MutableStateFlow(
        listOf(
            ProductReview(
                id = "REV_1",
                productName = "Amul Taaza Homogenised Toned Milk",
                productEmoji = "🥛",
                rating = 5,
                date = "Yesterday",
                comment = "Always delivered cold within 10 minutes. Pack is fresh and intact!"
            ),
            ProductReview(
                id = "REV_2",
                productName = "Shimla Royal Crisp Apples",
                productEmoji = "🍎",
                rating = 5,
                date = "29 Aug 2026",
                comment = "Very sweet, crunchy and freshly handpicked. Great packaging."
            ),
            ProductReview(
                id = "REV_3",
                productName = "Aashirvaad Shudh Chakki Atta",
                productEmoji = "🌾",
                rating = 4,
                date = "24 Aug 2026",
                comment = "Good quality flour. Soft rotis every time."
            )
        )
    )
    val userReviews: StateFlow<List<ProductReview>> = _userReviews.asStateFlow()

    fun addUserReview(review: ProductReview) {
        _userReviews.update { listOf(review) + it }
    }

    // Notifications
    private val _notifications = MutableStateFlow(
        listOf(
            AppNotification(
                id = "NOTIF_1",
                title = "Order Delivered! ⚡",
                message = "Your grocery order #SH-78219 has been delivered by Ramesh in 11 minutes. Rate your experience!",
                timeAgo = "10 mins ago",
                type = "ORDER",
                isRead = false
            ),
            AppNotification(
                id = "NOTIF_2",
                title = "₹50 Cashback Added 🎉",
                message = "Congratulations! ₹50 instant cashback has been credited to your Sudhani Wallet.",
                timeAgo = "1 hour ago",
                type = "WALLET",
                isRead = false
            ),
            AppNotification(
                id = "NOTIF_3",
                title = "Super Saver Weekend 🥑",
                message = "Get up to 40% off on fresh veggies and fruits plus free delivery on orders above ₹199.",
                timeAgo = "4 hours ago",
                type = "OFFER",
                isRead = true
            ),
            AppNotification(
                id = "NOTIF_4",
                title = "New Dark Store in Bengaluru 🚀",
                message = "We have opened a new high-speed delivery hub near you! Enjoy sub-10 minute deliveries.",
                timeAgo = "1 day ago",
                type = "SYSTEM",
                isRead = true
            )
        )
    )
    val notifications: StateFlow<List<AppNotification>> = _notifications.asStateFlow()

    fun markNotificationAsRead(id: String) {
        _notifications.update { list ->
            list.map { if (it.id == id) it.copy(isRead = true) else it }
        }
    }

    fun markAllNotificationsAsRead() {
        _notifications.update { list -> list.map { it.copy(isRead = true) } }
    }

    fun clearAllNotifications() {
        _notifications.value = emptyList()
    }

    // Admin product updates
    fun updateProductStock(productId: String, newStock: Int) {
        val safeStock = newStock.coerceAtLeast(0)
        products.update { list ->
            list.map {
                if (it.id == productId) {
                    val newStatus = if (safeStock == 0) {
                        ListingStatus.OUT_OF_STOCK
                    } else {
                        if (it.isActive && it.isVisible) ListingStatus.ACTIVE else it.listingStatus
                    }
                    it.copy(stock = safeStock, listingStatus = newStatus, updatedAt = System.currentTimeMillis())
                } else it
            }
        }
        repositoryScope.launch {
            try {
                productRepository?.updateStock(productId, safeStock)
                firestoreRepo.updateProductStock(productId, safeStock)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // ==========================================
    // Product Return Management System
    // ==========================================

    fun updateReturnPolicyConfig(config: ReturnPolicyConfig) {
        _returnPolicyConfig.value = config
    }

    fun toggleProductReturnable(productId: String, isReturnable: Boolean) {
        products.update { list ->
            list.map {
                if (it.id == productId) it.copy(isReturnable = isReturnable) else it
            }
        }
    }

    fun canRequestReturnForProduct(order: Order, productId: String): Pair<Boolean, String> {
        val policy = _returnPolicyConfig.value
        if (!policy.isReturnEnabled) {
            return Pair(false, "Returns are currently disabled by store policy.")
        }
        if (order.orderStatus == OrderStatus.CANCELLED) {
            return Pair(false, "Cancelled orders cannot be returned.")
        }
        if (order.orderStatus != OrderStatus.DELIVERED) {
            return Pair(false, "Return requests can only be submitted after the order is delivered.")
        }
        val isWindowOpen = order.isReturnWindowOpen(policy.defaultReturnWindowDays)
        if (!isWindowOpen) {
            return Pair(false, "Return period expired (${policy.defaultReturnWindowDays}-day window passed).")
        }
        val product = products.value.find { it.id == productId }
        if (product != null && !product.isReturnable) {
            return Pair(false, "This item is marked Non-Returnable as per store policy.")
        }
        val existingReturn = _returnRequests.value.find { it.orderId == order.orderId && it.productId == productId }
        if (existingReturn != null) {
            return Pair(false, "Return already requested for this item (${existingReturn.status.displayName}).")
        }
        return Pair(true, "Eligible for return")
    }

    fun submitReturnRequest(
        orderId: String,
        productId: String,
        returnQuantity: Int,
        reason: String,
        customerDescription: String,
        photos: List<String>
    ): Pair<Boolean, String> {
        val order = _orders.value.find { it.orderId == orderId }
            ?: return Pair(false, "Order #$orderId not found.")

        val item = order.items.find { it.productId == productId }
            ?: return Pair(false, "Product not found in this order.")

        val (canReturn, eligibilityMessage) = canRequestReturnForProduct(order, productId)
        if (!canReturn) {
            return Pair(false, eligibilityMessage)
        }

        val policy = _returnPolicyConfig.value
        val safeQuantity = returnQuantity.coerceIn(1, item.quantity)
        val itemAmount = item.price * safeQuantity
        val deadline = order.calculateReturnDeadline(policy.defaultReturnWindowDays)
            ?: (System.currentTimeMillis() + policy.defaultReturnWindowDays * 86400000L)

        val newReturnId = "RET-${order.orderId.removePrefix("SH-")}-${(10..99).random()}"
        val returnRequest = ReturnRequest(
            returnId = newReturnId,
            orderId = order.orderId,
            customerId = order.customerId,
            customerName = order.customerName,
            customerPhone = order.customerPhone,
            deliveryAddress = order.deliveryAddress,
            productId = item.productId,
            productName = item.productName,
            productImage = item.productImage,
            productUnit = "1 unit",
            returnQuantity = safeQuantity,
            unitPrice = item.price,
            itemAmount = itemAmount,
            refundAmount = itemAmount,
            isDeliveryFeeRefunded = policy.refundDeliveryFeeByDefault,
            deliveryFeeRefundAmount = if (policy.refundDeliveryFeeByDefault) order.deliveryFee else 0.0,
            refundMethod = order.paymentMethod,
            refundStatus = "Pending",
            orderCreatedAt = order.createdAt,
            deliveredAt = order.deliveredAt ?: (order.createdAt + 20 * 60 * 1000L),
            requestedAt = System.currentTimeMillis(),
            returnDeadline = deadline,
            reason = reason,
            customerDescription = customerDescription,
            photos = photos,
            status = ReturnStatus.REQUESTED
        )

        _returnRequests.update { listOf(returnRequest) + it }

        // Send customer notification
        val notification = CustomerNotification(
            customerId = order.customerId,
            title = "Return Request Submitted 📦",
            message = "Your return request for ${item.productName} ($newReturnId) has been received. Our team will review it shortly.",
            type = "RETURN_STATUS",
            referenceId = order.orderId
        )
        _customerNotifications.update { listOf(notification) + it }

        repositoryScope.launch {
            try {
                returnRepository?.saveReturnRequest(returnRequest)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        return Pair(true, "Return request submitted successfully. Your request is under review.")
    }

    fun approveReturn(returnId: String, adminNotes: String? = null) {
        var updatedReq: ReturnRequest? = null
        _returnRequests.update { list ->
            list.map { req ->
                if (req.returnId == returnId) {
                    val updated = req.copy(
                        status = ReturnStatus.APPROVED,
                        adminNotes = adminNotes ?: req.adminNotes,
                        updatedAt = System.currentTimeMillis()
                    )
                    updatedReq = updated
                    updated
                } else req
            }
        }
        updatedReq?.let { req ->
            val notif = CustomerNotification(
                customerId = req.customerId,
                title = "Return Approved ✅",
                message = "Your return request for ${req.productName} has been approved. A pickup partner will be assigned soon.",
                type = "RETURN_STATUS",
                referenceId = req.orderId
            )
            _customerNotifications.update { listOf(notif) + it }
            repositoryScope.launch {
                returnRepository?.updateStatus(returnId, ReturnStatus.APPROVED)
            }
        }
    }

    fun rejectReturn(returnId: String, rejectionReason: String) {
        val safeReason = rejectionReason.ifBlank { "Does not meet return eligibility criteria." }
        var updatedReq: ReturnRequest? = null
        _returnRequests.update { list ->
            list.map { req ->
                if (req.returnId == returnId) {
                    val updated = req.copy(
                        status = ReturnStatus.REJECTED,
                        rejectionReason = safeReason,
                        updatedAt = System.currentTimeMillis()
                    )
                    updatedReq = updated
                    updated
                } else req
            }
        }
        updatedReq?.let { req ->
            val notif = CustomerNotification(
                customerId = req.customerId,
                title = "Return Request Declined ❌",
                message = "Your return for ${req.productName} was declined. Reason: $safeReason",
                type = "RETURN_STATUS",
                referenceId = req.orderId
            )
            _customerNotifications.update { listOf(notif) + it }
            repositoryScope.launch {
                returnRepository?.updateStatus(returnId, ReturnStatus.REJECTED, safeReason)
            }
        }
    }

    fun askForMoreInformation(returnId: String, note: String) {
        _returnRequests.update { list ->
            list.map { req ->
                if (req.returnId == returnId) {
                    req.copy(
                        status = ReturnStatus.UNDER_REVIEW,
                        adminNotes = note,
                        updatedAt = System.currentTimeMillis()
                    )
                } else req
            }
        }
    }

    fun schedulePickup(
        returnId: String,
        pickupDate: Long,
        timeSlot: String,
        partnerName: String,
        partnerPhone: String
    ) {
        var updatedReq: ReturnRequest? = null
        _returnRequests.update { list ->
            list.map { req ->
                if (req.returnId == returnId) {
                    val updated = req.copy(
                        status = ReturnStatus.PICKUP_SCHEDULED,
                        pickupDate = pickupDate,
                        pickupTimeSlot = timeSlot,
                        pickupPartnerName = partnerName,
                        pickupPartnerPhone = partnerPhone,
                        updatedAt = System.currentTimeMillis()
                    )
                    updatedReq = updated
                    updated
                } else req
            }
        }
        updatedReq?.let { req ->
            val notif = CustomerNotification(
                customerId = req.customerId,
                title = "Reverse Pickup Scheduled 🛵",
                message = "Pickup for ${req.productName} is scheduled for $timeSlot with $partnerName ($partnerPhone).",
                type = "RETURN_STATUS",
                referenceId = req.orderId
            )
            _customerNotifications.update { listOf(notif) + it }
            repositoryScope.launch {
                returnRepository?.updateReturnRequest(req)
            }
        }
    }

    fun markPickupCompleted(returnId: String) {
        var updatedReq: ReturnRequest? = null
        _returnRequests.update { list ->
            list.map { req ->
                if (req.returnId == returnId) {
                    val updated = req.copy(
                        status = ReturnStatus.PICKED_UP,
                        updatedAt = System.currentTimeMillis()
                    )
                    updatedReq = updated
                    updated
                } else req
            }
        }
        updatedReq?.let { req ->
            val notif = CustomerNotification(
                customerId = req.customerId,
                title = "Item Picked Up 📦",
                message = "${req.productName} has been picked up from your address and is in transit to the hub.",
                type = "RETURN_STATUS",
                referenceId = req.orderId
            )
            _customerNotifications.update { listOf(notif) + it }
            repositoryScope.launch {
                returnRepository?.updateStatus(returnId, ReturnStatus.PICKED_UP)
            }
        }
    }

    fun markProductReceived(returnId: String, notes: String? = null) {
        var updatedReq: ReturnRequest? = null
        _returnRequests.update { list ->
            list.map { req ->
                if (req.returnId == returnId) {
                    val updated = req.copy(
                        status = ReturnStatus.PRODUCT_RECEIVED,
                        adminNotes = notes ?: req.adminNotes,
                        updatedAt = System.currentTimeMillis()
                    )
                    updatedReq = updated
                    updated
                } else req
            }
        }
        updatedReq?.let { req ->
            val notif = CustomerNotification(
                customerId = req.customerId,
                title = "Product Received at Hub 🏬",
                message = "${req.productName} was received and inspected at the fulfillment center. Refund is being processed.",
                type = "RETURN_STATUS",
                referenceId = req.orderId
            )
            _customerNotifications.update { listOf(notif) + it }
            repositoryScope.launch {
                returnRepository?.updateStatus(returnId, ReturnStatus.PRODUCT_RECEIVED)
            }
        }
    }

    fun approveRefund(
        returnId: String,
        refundAmount: Double,
        refundDeliveryFee: Boolean,
        refundMethod: String
    ) {
        var updatedReq: ReturnRequest? = null
        _returnRequests.update { list ->
            list.map { req ->
                if (req.returnId == returnId) {
                    val order = _orders.value.find { it.orderId == req.orderId }
                    val deliveryRefundAmt = if (refundDeliveryFee) (order?.deliveryFee ?: 0.0) else 0.0
                    val updated = req.copy(
                        status = ReturnStatus.REFUND_PROCESSING,
                        refundAmount = refundAmount,
                        isDeliveryFeeRefunded = refundDeliveryFee,
                        deliveryFeeRefundAmount = deliveryRefundAmt,
                        refundMethod = refundMethod,
                        refundStatus = "Processing",
                        updatedAt = System.currentTimeMillis()
                    )
                    updatedReq = updated
                    updated
                } else req
            }
        }
        updatedReq?.let { req ->
            val totalRef = req.totalRefund
            val notif = CustomerNotification(
                customerId = req.customerId,
                title = "Refund of ₹${totalRef.toInt()} Initiated 💳",
                message = "Refund for ${req.productName} has been approved and sent via ${req.refundMethod}.",
                type = "REFUND",
                referenceId = req.orderId
            )
            _customerNotifications.update { listOf(notif) + it }
            repositoryScope.launch {
                returnRepository?.updateReturnRequest(req)
            }
        }
    }

    fun markRefundCompleted(returnId: String, transactionRef: String) {
        var updatedReq: ReturnRequest? = null
        _returnRequests.update { list ->
            list.map { req ->
                if (req.returnId == returnId) {
                    val updated = req.copy(
                        status = ReturnStatus.REFUNDED,
                        refundStatus = "Completed",
                        refundDate = System.currentTimeMillis(),
                        refundTransactionRef = transactionRef,
                        updatedAt = System.currentTimeMillis()
                    )
                    updatedReq = updated
                    updated
                } else req
            }
        }
        updatedReq?.let { req ->
            // If refund is to wallet, credit the wallet balance
            if (req.refundMethod.contains("Wallet", ignoreCase = true)) {
                _walletBalance.update { it + req.totalRefund }
                val newTxn = WalletTransaction(
                    id = "WTX_REF_${System.currentTimeMillis().toString().takeLast(5)}",
                    title = "Refund for ${req.productName}",
                    amount = req.totalRefund,
                    isCredit = true,
                    date = "Just now",
                    description = "Refund against Return #${req.returnId}"
                )
                _walletTransactions.update { listOf(newTxn) + it }
            }

            val notif = CustomerNotification(
                customerId = req.customerId,
                title = "Refund Completed 🎉",
                message = "₹${req.totalRefund.toInt()} has been successfully credited via ${req.refundMethod}. Txn Ref: $transactionRef.",
                type = "REFUND",
                referenceId = req.orderId
            )
            _customerNotifications.update { listOf(notif) + it }
            repositoryScope.launch {
                returnRepository?.updateReturnRequest(req)
            }
        }
    }

    fun dismissCustomerNotification(id: String) {
        _customerNotifications.update { list -> list.filter { it.id != id } }
    }
}
