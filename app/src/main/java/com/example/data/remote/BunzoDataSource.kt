package com.example.data.remote

import com.example.data.model.AppSetting
import com.example.data.model.BannerOffer
import com.example.data.model.Branch
import com.example.data.model.Category
import com.example.data.model.Product
import com.example.data.model.ProductExtra
import com.example.data.model.ProductSize

object BunzoDataSource {
    val categories = listOf(
        Category(
            id = "cat_burgers",
            nameAr = "برغر الذواقة",
            nameEn = "Gourmet Burgers",
            imageUrl = "https://images.unsplash.com/photo-1568901346375-23c9450c58cd?w=600&auto=format&fit=crop&q=80",
            productCount = 6,
            iconName = "lunch_dining",
            sortOrder = 1
        ),
        Category(
            id = "cat_crispy",
            nameAr = "بروستد ومقرمشات",
            nameEn = "Broasted & Crispy",
            imageUrl = "https://images.unsplash.com/photo-1626082927389-6cd097cdc6ec?w=600&auto=format&fit=crop&q=80",
            productCount = 5,
            iconName = "restaurant",
            sortOrder = 2
        ),
        Category(
            id = "cat_shawarma",
            nameAr = "شاورما وساندوتش",
            nameEn = "Shawarma & Wraps",
            imageUrl = "https://images.unsplash.com/photo-1529006557810-274b9b2fc783?w=600&auto=format&fit=crop&q=80",
            productCount = 4,
            iconName = "kebab_dining",
            sortOrder = 3
        ),
        Category(
            id = "cat_appetizers",
            nameAr = "مقبلات وصوصات",
            nameEn = "Appetizers & Sides",
            imageUrl = "https://images.unsplash.com/photo-1541592106381-b31e9677c0e5?w=700&auto=format&fit=crop&q=80",
            productCount = 8,
            iconName = "tapas",
            sortOrder = 4
        ),
        Category(
            id = "cat_drinks",
            nameAr = "مشروبات وموهيتو",
            nameEn = "Drinks & Shakes",
            imageUrl = "https://images.unsplash.com/photo-1551024709-8f23befc6f87?w=600&auto=format&fit=crop&q=80",
            productCount = 5,
            iconName = "local_bar",
            sortOrder = 5
        ),
        Category(
            id = "cat_desserts",
            nameAr = "حلويات وسويتس",
            nameEn = "Desserts & Sweets",
            imageUrl = "https://images.unsplash.com/photo-1551024601-bec78aea704b?w=600&auto=format&fit=crop&q=80",
            productCount = 4,
            iconName = "icecream",
            sortOrder = 6
        )
    )

    private val commonBurgerExtras = listOf(
        ProductExtra("ext_cheese", "جبنة شيدر إضافية", "Extra Cheddar Cheese", 4000.0),
        ProductExtra("ext_beef_bacon", "بيكون بقري مدخن", "Smoked Beef Bacon", 7000.0),
        ProductExtra("ext_caramel_onion", "بصل مكرمل متبل", "Caramelized Onions", 3000.0),
        ProductExtra("ext_jalapeno", "هالبينو حار", "Spicy Jalapeno", 2500.0),
        ProductExtra("ext_mushroom", "فطر طازج سوتيه", "Fresh Sautéed Mushroom", 5000.0)
    )

    private val burgerSizes = listOf(
        ProductSize("size_single", "سنجل (150غ لحم)", "Single (150g)", 0.0, isDefault = true),
        ProductSize("size_double", "دبل (300غ لحم)", "Double (300g)", 14000.0),
        ProductSize("size_triple", "تربل (450غ لحم)", "Triple (450g)", 26000.0)
    )

    val products = listOf(
        Product(
            id = "prod_bunzo_classic",
            nameAr = "برغر بونزوا كلاسيك",
            nameEn = "Bunzo Signature Burger",
            descriptionAr = "شريحة لحم بلدي طازج 100%، جبنة شيدر ذائبة، خس مقرمش، طماطم، صوص بونزوا السري في خبز بريوش طري.",
            descriptionEn = "100% pure fresh local beef patty, melted cheddar, crispy lettuce, tomato, special signature Bunzo sauce on toasted brioche bun.",
            price = 38000.0,
            oldPrice = 45000.0,
            discountPercent = 15,
            categoryId = "cat_burgers",
            categoryNameAr = "برغر الذواقة",
            imageUrl = "https://images.unsplash.com/photo-1568901346375-23c9450c58cd?w=600&auto=format&fit=crop&q=80",
            isAvailable = true,
            isFeatured = true,
            isOffer = true,
            barcode = "621001001",
            unit = "وجبة",
            sizes = burgerSizes,
            extras = commonBurgerExtras,
            rating = 4.9f,
            reviewCount = 340,
            prepTimeMinutes = 15,
            calories = 620
        ),
        Product(
            id = "prod_smoky_bbq",
            nameAr = "سموكي باربكيو برغر",
            nameEn = "Smoky BBQ Bacon Burger",
            descriptionAr = "شريحة لحم مشوية على الفحم مع صوص باربكيو مدخن، حلقات بصل مقرمشة، بيكون بقري وجبنة بيبر جاك الذائبة.",
            descriptionEn = "Charcoal grilled beef patty with smoky BBQ sauce, crispy onion rings, beef bacon and melted pepper jack cheese.",
            price = 42000.0,
            oldPrice = 48000.0,
            discountPercent = 12,
            categoryId = "cat_burgers",
            categoryNameAr = "برغر الذواقة",
            imageUrl = "https://images.unsplash.com/photo-1586190848861-99aa4a171e90?w=600&auto=format&fit=crop&q=80",
            isAvailable = true,
            isFeatured = true,
            isOffer = true,
            barcode = "621001002",
            unit = "وجبة",
            sizes = burgerSizes,
            extras = commonBurgerExtras,
            rating = 4.8f,
            reviewCount = 210,
            prepTimeMinutes = 18,
            calories = 710
        ),
        Product(
            id = "prod_truffle_mushroom",
            nameAr = "ترافل مشروم سوبريم",
            nameEn = "Truffle Mushroom Supreme",
            descriptionAr = "شريحة لحم أنغوس مع صوص الترافل الأبيض الإيطالي، فطر بري سوتيه، جبنة سويسرية وجرجير طازج.",
            descriptionEn = "Angus beef patty with white truffle sauce, wild sautéed mushrooms, Swiss cheese and fresh baby arugula.",
            price = 46000.0,
            oldPrice = null,
            discountPercent = 0,
            categoryId = "cat_burgers",
            categoryNameAr = "برغر الذواقة",
            imageUrl = "https://images.unsplash.com/photo-1550547660-d9450f859349?w=600&auto=format&fit=crop&q=80",
            isAvailable = true,
            isFeatured = true,
            isOffer = false,
            barcode = "621001003",
            unit = "وجبة",
            sizes = burgerSizes,
            extras = commonBurgerExtras,
            rating = 4.9f,
            reviewCount = 185,
            prepTimeMinutes = 20,
            calories = 680
        ),
        Product(
            id = "prod_crispy_chicken_zesty",
            nameAr = "كريسبي تشيكن حار ولذيذ",
            nameEn = "Zesty Crispy Chicken",
            descriptionAr = "صدر دجاج مقرمش ذهبي متبل بخلطة بهارات سرية، رانش حار ومايونيز، مخلل مقرمش وخس آيسبيرغ.",
            descriptionEn = "Golden crispy chicken breast marinated in secret herbs, spicy ranch and mayo, crunchy pickles and iceberg lettuce.",
            price = 34000.0,
            oldPrice = 40000.0,
            discountPercent = 15,
            categoryId = "cat_burgers",
            categoryNameAr = "برغر الذواقة",
            imageUrl = "https://images.unsplash.com/photo-1625813506062-0aeb1d7a094b?w=600&auto=format&fit=crop&q=80",
            isAvailable = true,
            isFeatured = false,
            isOffer = true,
            barcode = "621001004",
            unit = "وجبة",
            sizes = listOf(
                ProductSize("sz_reg", "وجبة عادية", "Regular", 0.0, true),
                ProductSize("sz_combo", "كومبو مع بطاطا ومشروب", "Combo with Fries & Drink", 9000.0)
            ),
            extras = commonBurgerExtras,
            rating = 4.7f,
            reviewCount = 290,
            prepTimeMinutes = 14,
            calories = 590
        ),
        Product(
            id = "prod_broasted_meal",
            nameAr = "وجبة بروستد بونزوا (4 قطع)",
            nameEn = "Golden Broasted Chicken (4 pcs)",
            descriptionAr = "4 قطع دجاج بروستد طازج مقرمش، بطاطا ودجز متبلة، كريمة ثوم دمشقي أصلي، كول سلو وخبز بونزوا الطازج.",
            descriptionEn = "4 pieces fresh golden crispy broasted chicken, potato wedges, authentic Syrian garlic cream, coleslaw and bun.",
            price = 45000.0,
            oldPrice = 52000.0,
            discountPercent = 13,
            categoryId = "cat_crispy",
            categoryNameAr = "بروستد ومقرمشات",
            imageUrl = "https://images.unsplash.com/photo-1626082927389-6cd097cdc6ec?w=600&auto=format&fit=crop&q=80",
            isAvailable = true,
            isFeatured = true,
            isOffer = true,
            barcode = "621001005",
            unit = "وجبة",
            sizes = listOf(
                ProductSize("size_4p", "وجبة 4 قطع", "4 Pieces", 0.0, true),
                ProductSize("size_8p", "وجبة 8 قطع", "8 Pieces", 40000.0),
                ProductSize("size_12p", "عائلية 12 قطعة", "12 Pieces Family", 78000.0)
            ),
            extras = listOf(
                ProductExtra("ext_garlic", "كريم ثوم إضافي", "Extra Garlic Dip", 2500.0),
                ProductExtra("ext_spicy_garlic", "كريم ثوم حار إضافي", "Extra Spicy Garlic", 2500.0),
                ProductExtra("ext_coleslaw", "سلطة ملفوف كبيرة", "Large Coleslaw", 3500.0)
            ),
            rating = 4.9f,
            reviewCount = 420,
            prepTimeMinutes = 22,
            calories = 890
        ),
        Product(
            id = "prod_chicken_strips",
            nameAr = "تشيكن ستربس مقرمش (5 قطع)",
            nameEn = "Crunchy Chicken Strips (5 pcs)",
            descriptionAr = "5 قطع من أصابع الدجاج المقرمشة بتتبيلتنا الخاصة، تقدم مع صوص الخردل بالعسل، صوص الباربكيو وبطاطا مقلية.",
            descriptionEn = "5 pieces of crispy fried chicken tenders with special seasoning, served with honey mustard, BBQ dip, and french fries.",
            price = 32000.0,
            oldPrice = null,
            discountPercent = 0,
            categoryId = "cat_crispy",
            categoryNameAr = "بروستد ومقرمشات",
            imageUrl = "https://images.unsplash.com/photo-1562967914-608f82629710?w=600&auto=format&fit=crop&q=80",
            isAvailable = true,
            isFeatured = false,
            isOffer = false,
            barcode = "621001006",
            unit = "وجبة",
            sizes = emptyList(),
            extras = listOf(
                ProductExtra("ext_honey_mustard", "صوص هوني ماسترد", "Honey Mustard Sauce", 2000.0),
                ProductExtra("ext_cheddar_dip", "صوص جبنة شيدر", "Melted Cheddar Sauce", 3500.0)
            ),
            rating = 4.8f,
            reviewCount = 175,
            prepTimeMinutes = 12,
            calories = 510
        ),
        Product(
            id = "prod_shawarma_arabia",
            nameAr = "وجبة شاورما دجاج عربي سوبر",
            nameEn = "Super Chicken Shawarma Arabi",
            descriptionAr = "شاورما دجاج سورية متبلة على السيخ مقطعة على خبز الصاج، بطاطا مقلية، مخلل وثومية دمشقية أصيلة.",
            descriptionEn = "Authentic Syrian chicken shawarma sliced into bites, served with crispy fries, original garlic paste and pickles.",
            price = 28000.0,
            oldPrice = 33000.0,
            discountPercent = 15,
            categoryId = "cat_shawarma",
            categoryNameAr = "شاورما وساندوتش",
            imageUrl = "https://images.unsplash.com/photo-1529006557810-274b9b2fc783?w=600&auto=format&fit=crop&q=80",
            isAvailable = true,
            isFeatured = true,
            isOffer = true,
            barcode = "621001007",
            unit = "وجبة",
            sizes = listOf(
                ProductSize("sz_reg_sh", "عادي (1 ساندويش)", "Regular 1 Sandwich", 0.0, true),
                ProductSize("sz_double_sh", "دبل (2 ساندويش)", "Double 2 Sandwiches", 22000.0)
            ),
            extras = listOf(
                ProductExtra("ext_cheese_sh", "جبنة قشقوان ذائبة", "Melted Kashkaval Cheese", 4000.0),
                ProductExtra("ext_garlic_sh", "ثومية زيادة", "Extra Garlic Paste", 2000.0)
            ),
            rating = 4.9f,
            reviewCount = 512,
            prepTimeMinutes = 10,
            calories = 650
        ),
        Product(
            id = "prod_appetizer_combo_deluxe",
            nameAr = "طبق مقبلات بونزوا المشكل",
            nameEn = "Bunzo Deluxe Appetizer Platter",
            descriptionAr = "تشكيلة رائعة من بطاطا كريسبي، أصابع جبنة موزاريلا مقرمشة، حلقات بصل ذهبية، وقطع دجاج بايتس مع 3 صوصات.",
            descriptionEn = "Deluxe platter with crispy french fries, melted mozzarella sticks, golden onion rings, chicken bites with 3 signature dipping sauces.",
            price = 28000.0,
            oldPrice = 34000.0,
            discountPercent = 18,
            categoryId = "cat_appetizers",
            categoryNameAr = "مقبلات وصوصات",
            imageUrl = "https://images.unsplash.com/photo-1541592106381-b31e9677c0e5?w=600&auto=format&fit=crop&q=80",
            isAvailable = true,
            isFeatured = true,
            isOffer = true,
            barcode = "621001008",
            unit = "طبق",
            sizes = listOf(
                ProductSize("sz_platter_med", "وسط (شخصين)", "Medium (2 Persons)", 0.0, true),
                ProductSize("sz_platter_lar", "كبير ديلوكس (4 أشخاص)", "Large Deluxe (4 Persons)", 16000.0)
            ),
            extras = listOf(
                ProductExtra("ext_extra_cheddar", "شيدر إضافي", "Extra Melted Cheddar", 3000.0),
                ProductExtra("ext_extra_jalapeno", "هالبينو مقرمش", "Crispy Jalapenos", 2000.0)
            ),
            rating = 4.9f,
            reviewCount = 220,
            prepTimeMinutes = 10,
            calories = 580
        ),
        Product(
            id = "prod_french_fries_cheese",
            nameAr = "بطاطا مقلية بجبنة الشيدر والبيكون",
            nameEn = "Bunzo Loaded Cheese Fries",
            descriptionAr = "بطاطا ذهبية مقرمشة مغطاة بصوص جبنة الشيدر الساخنة، قطع لحم بيكون مقرمش، شرائح هالبينو وصوص الرانش.",
            descriptionEn = "Crispy golden french fries smothered in hot melted cheddar cheese, chopped beef bacon, jalapeno slices and ranch sauce.",
            price = 18000.0,
            oldPrice = 22000.0,
            discountPercent = 18,
            categoryId = "cat_appetizers",
            categoryNameAr = "مقبلات وصوصات",
            imageUrl = "https://images.unsplash.com/photo-1585109649139-366815a0d713?w=600&auto=format&fit=crop&q=80",
            isAvailable = true,
            isFeatured = true,
            isOffer = true,
            barcode = "621001009",
            unit = "علبة",
            sizes = listOf(
                ProductSize("sz_fries_reg", "حجم عادي", "Regular", 0.0, true),
                ProductSize("sz_fries_lar", "حجم سوبر كبير", "Super Large", 8000.0)
            ),
            extras = listOf(
                ProductExtra("ext_fries_bacon", "بيكون إضافي", "Extra Beef Bacon", 4000.0),
                ProductExtra("ext_fries_cheese", "دبل صوص شيدر", "Double Melted Cheese", 3500.0)
            ),
            rating = 4.8f,
            reviewCount = 145,
            prepTimeMinutes = 8,
            calories = 440
        ),
        Product(
            id = "prod_mojito_berry",
            nameAr = "موهيتو توت بري منعش",
            nameEn = "Fresh Wild Berry Mojito",
            descriptionAr = "مزيج منعش من التوت البري الطازج، أوراق النعناع، الليمون مع مياه غازية فوارة وثلج مجروش.",
            descriptionEn = "Refreshing blend of wild berries, fresh mint leaves, lime and sparkling soda over crushed ice.",
            price = 11000.0,
            oldPrice = null,
            discountPercent = 0,
            categoryId = "cat_drinks",
            categoryNameAr = "مشروبات وموهيتو",
            imageUrl = "https://images.unsplash.com/photo-1551024709-8f23befc6f87?w=600&auto=format&fit=crop&q=80",
            isAvailable = true,
            isFeatured = true,
            isOffer = false,
            barcode = "621001011",
            unit = "كوب",
            sizes = listOf(
                ProductSize("sz_med", "وسط (400 مل)", "Medium", 0.0, true),
                ProductSize("sz_lar", "كبير (650 مل)", "Large", 4000.0)
            ),
            extras = emptyList(),
            rating = 4.9f,
            reviewCount = 160,
            prepTimeMinutes = 5,
            calories = 140
        ),
        Product(
            id = "prod_oreo_shake",
            nameAr = "ميلك شيك أوريو ديلوكس",
            nameEn = "Oreo Cookies Milkshake",
            descriptionAr = "ميلك شيك كثيف وغني ممزوج بقطع بسكويت أوريو، آيس كريم فانيليا، فدج الشوكولاتة وكريمة مخفوقة.",
            descriptionEn = "Rich thick milkshake blended with crushed Oreo cookies, vanilla ice cream, chocolate fudge and whipped cream.",
            price = 15000.0,
            oldPrice = 18000.0,
            discountPercent = 16,
            categoryId = "cat_drinks",
            categoryNameAr = "مشروبات وموهيتو",
            imageUrl = "https://images.unsplash.com/photo-1572490122747-3968b75cc699?w=600&auto=format&fit=crop&q=80",
            isAvailable = true,
            isFeatured = false,
            isOffer = true,
            barcode = "621001012",
            unit = "كوب",
            sizes = emptyList(),
            extras = emptyList(),
            rating = 4.8f,
            reviewCount = 205,
            prepTimeMinutes = 6,
            calories = 480
        ),
        Product(
            id = "prod_lava_cake",
            nameAr = "كيكة الشوكولاتة الذائبة (لافا كيك)",
            nameEn = "Molten Chocolate Lava Cake",
            descriptionAr = "كيكة شوكولاتة بلجيكية ساخنة بقلب من الشوكولاتة الذائبة، تقدم مع كرة من آيس كريم الفانيليا الفاخر.",
            descriptionEn = "Warm Belgian chocolate cake with a molten fudge core, served with a scoop of premium vanilla ice cream.",
            price = 17000.0,
            oldPrice = 20000.0,
            discountPercent = 15,
            categoryId = "cat_desserts",
            categoryNameAr = "حلويات وسويتس",
            imageUrl = "https://images.unsplash.com/photo-1606313564200-e75d5e30476c?w=600&auto=format&fit=crop&q=80",
            isAvailable = true,
            isFeatured = true,
            isOffer = true,
            barcode = "621001013",
            unit = "قطعة",
            sizes = emptyList(),
            extras = emptyList(),
            rating = 4.9f,
            reviewCount = 180,
            prepTimeMinutes = 10,
            calories = 520
        )
    )

    val bannerOffers = listOf(
        BannerOffer(
            id = "banner_1",
            titleAr = "عرض بونزوا الملكي",
            titleEn = "Bunzo Royal Offer",
            subtitleAr = "خصم 15% على البرغر الكلاسيكي مع بطاطا ومشروب مجاناً",
            subtitleEn = "15% off signature burger + free fries & drink",
            badgeAr = "وفر 15%",
            imageUrl = "https://images.unsplash.com/photo-1568901346375-23c9450c58cd?w=800&auto=format&fit=crop&q=80",
            targetProductId = "prod_bunzo_classic"
        ),
        BannerOffer(
            id = "banner_2",
            titleAr = "وليمة البروستد الذهبية",
            titleEn = "Golden Broasted Feast",
            subtitleAr = "وجبة عائلية فاخرة مع كريمة الثوم الأصلية وكول سلو",
            subtitleEn = "Family feast with Syrian garlic dip & coleslaw",
            badgeAr = "الأكثر طلباً",
            imageUrl = "https://images.unsplash.com/photo-1626082927389-6cd097cdc6ec?w=800&auto=format&fit=crop&q=80",
            targetProductId = "prod_broasted_meal"
        ),
        BannerOffer(
            id = "banner_3",
            titleAr = "شاورما الصاج الدمشقية",
            titleEn = "Syrian Saj Shawarma",
            subtitleAr = "شاورما دجاج أصيلة متبلة بتتبيلة بونزوا الخاصة",
            subtitleEn = "Authentic Syrian chicken shawarma with garlic sauce",
            badgeAr = "طازجة يومياً",
            imageUrl = "https://images.unsplash.com/photo-1529006557810-274b9b2fc783?w=800&auto=format&fit=crop&q=80",
            targetProductId = "prod_shawarma_arabia"
        )
    )

    val branches = listOf(
        Branch(
            id = "branch_damascus_mezzah",
            nameAr = "فرع دمشق - أوتوستراد المزة",
            nameEn = "Damascus - Mezzah Branch",
            addressAr = "دمشق، أوتوستراد المزة، بجانب مقهى الشام",
            addressEn = "Damascus, Mazzeh Highway, Near Al-Sham Cafe",
            phone = "0116611223",
            mobile = "+963933112233",
            email = "mezzah@bunzo.restaurant",
            imageUrl = "https://images.unsplash.com/photo-1555396273-367ea4eb4db5?w=600&auto=format&fit=crop&q=80",
            latitude = 33.5012,
            longitude = 36.2589,
            openingHoursAr = "يومياً 11:00 ص - 02:00 ليلاً",
            openingHoursEn = "Daily 11:00 AM - 02:00 AM",
            isOpen = true
        ),
        Branch(
            id = "branch_damascus_shaalan",
            nameAr = "فرع دمشق - الشعلان",
            nameEn = "Damascus - Shaalan Branch",
            addressAr = "دمشق، الشعلان، شارع الحبيبي، مقابل حديقة السبكي",
            addressEn = "Damascus, Al-Shaalan, Al-Habibi Street, Opp. Al-Subki Park",
            phone = "0113322445",
            mobile = "+963944112233",
            email = "shaalan@bunzo.restaurant",
            imageUrl = "https://images.unsplash.com/photo-1517248135467-4c7edcad34c4?w=600&auto=format&fit=crop&q=80",
            latitude = 33.5186,
            longitude = 36.2891,
            openingHoursAr = "يومياً 12:00 ظ - 02:30 ليلاً",
            openingHoursEn = "Daily 12:00 PM - 02:30 AM",
            isOpen = true
        ),
        Branch(
            id = "branch_damascus_malki",
            nameAr = "فرع دمشق - المالكي",
            nameEn = "Damascus - Al-Malki Branch",
            addressAr = "دمشق، المالكي، شارع تشرين",
            addressEn = "Damascus, Al-Malki, Tishreen Street",
            phone = "0113711990",
            mobile = "+963955112233",
            email = "malki@bunzo.restaurant",
            imageUrl = "https://images.unsplash.com/photo-1552566626-52f8b828add9?w=600&auto=format&fit=crop&q=80",
            latitude = 33.5245,
            longitude = 36.2750,
            openingHoursAr = "يومياً 11:30 ص - 01:30 ليلاً",
            openingHoursEn = "Daily 11:30 AM - 01:30 AM",
            isOpen = true
        ),
        Branch(
            id = "branch_aleppo",
            nameAr = "فرع حلب - الشهباء الجديدة",
            nameEn = "Aleppo - New Shahba Branch",
            addressAr = "حلب، الشهباء الجديدة، شارع الشيراتون",
            addressEn = "Aleppo, New Shahba, Sheraton St.",
            phone = "0212211990",
            mobile = "+963966112233",
            email = "aleppo@bunzo.restaurant",
            imageUrl = "https://images.unsplash.com/photo-1537047902294-62a40c20a6ae?w=600&auto=format&fit=crop&q=80",
            latitude = 36.2234,
            longitude = 37.1356,
            openingHoursAr = "يومياً 12:00 ظ - 01:00 ليلاً",
            openingHoursEn = "Daily 12:00 PM - 01:00 AM",
            isOpen = true
        ),
        Branch(
            id = "branch_latakia",
            nameAr = "فرع اللاذقية - الكورنيش الغربي",
            nameEn = "Latakia - Western Corniche",
            addressAr = "اللاذقية، الكورنيش الغربي، الواجهة البحرية",
            addressEn = "Latakia, Western Corniche, Waterfront Boulevard",
            phone = "0414433221",
            mobile = "+963988112233",
            email = "latakia@bunzo.restaurant",
            imageUrl = "https://images.unsplash.com/photo-1559339352-11d035aa65de?w=600&auto=format&fit=crop&q=80",
            latitude = 35.5317,
            longitude = 35.7876,
            openingHoursAr = "يومياً 11:00 ص - 02:00 ليلاً",
            openingHoursEn = "Daily 11:00 AM - 02:00 AM",
            isOpen = true
        )
    )

    val appSettings = AppSetting()
}
