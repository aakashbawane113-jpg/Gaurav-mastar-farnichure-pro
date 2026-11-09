package com.example.model

enum class FurnitureCategory(
    val englishName: String,
    val hindiName: String,
    val defaultHeightMm: Int,
    val defaultWidthMm: Int,
    val defaultDepthMm: Int,
    val defaultDoors: Int,
    val defaultShelves: Int,
    val defaultDrawers: Int,
    val defaultLoft: Boolean = false
) {
    WARDROBE(
        englishName = "Wardrobe / Almirah",
        hindiName = "अलमारी / वार्डरोब",
        defaultHeightMm = 2100,
        defaultWidthMm = 1800,
        defaultDepthMm = 600,
        defaultDoors = 3,
        defaultShelves = 5,
        defaultDrawers = 3,
        defaultLoft = true
    ),
    MODULAR_KITCHEN(
        englishName = "Modular Kitchen",
        hindiName = "मॉड्यूलर किचन",
        defaultHeightMm = 850,
        defaultWidthMm = 2400,
        defaultDepthMm = 600,
        defaultDoors = 4,
        defaultShelves = 4,
        defaultDrawers = 4,
        defaultLoft = false
    ),
    TV_UNIT(
        englishName = "TV Unit / Console",
        hindiName = "टीवी यूनिट / कंसोल",
        defaultHeightMm = 1800,
        defaultWidthMm = 1800,
        defaultDepthMm = 400,
        defaultDoors = 2,
        defaultShelves = 3,
        defaultDrawers = 2,
        defaultLoft = false
    ),
    BED(
        englishName = "Bed with Storage",
        hindiName = "पलंग / स्टोरेज बेड",
        defaultHeightMm = 900,
        defaultWidthMm = 1800,
        defaultDepthMm = 2000,
        defaultDoors = 0,
        defaultShelves = 2,
        defaultDrawers = 2,
        defaultLoft = false
    ),
    SOFA(
        englishName = "Sofa / Couch",
        hindiName = "सोफा / काउच",
        defaultHeightMm = 850,
        defaultWidthMm = 2100,
        defaultDepthMm = 850,
        defaultDoors = 0,
        defaultShelves = 0,
        defaultDrawers = 2,
        defaultLoft = false
    ),
    DINING_TABLE(
        englishName = "Dining Table Set",
        hindiName = "डाइनिंग टेबल",
        defaultHeightMm = 760,
        defaultWidthMm = 1600,
        defaultDepthMm = 900,
        defaultDoors = 0,
        defaultShelves = 1,
        defaultDrawers = 0,
        defaultLoft = false
    ),
    OFFICE_DESK(
        englishName = "Office Furniture",
        hindiName = "ऑफिस फर्नीचर / टेबल",
        defaultHeightMm = 750,
        defaultWidthMm = 1500,
        defaultDepthMm = 750,
        defaultDoors = 1,
        defaultShelves = 2,
        defaultDrawers = 3,
        defaultLoft = false
    ),
    SHOE_RACK(
        englishName = "Shoe Rack",
        hindiName = "शू रैक / जूता स्टैंड",
        defaultHeightMm = 1000,
        defaultWidthMm = 900,
        defaultDepthMm = 350,
        defaultDoors = 2,
        defaultShelves = 4,
        defaultDrawers = 1,
        defaultLoft = false
    ),
    STUDY_TABLE(
        englishName = "Study Table with Bookshelf",
        hindiName = "स्टडी टेबल / बुकशेल्फ़",
        defaultHeightMm = 1600,
        defaultWidthMm = 1200,
        defaultDepthMm = 600,
        defaultDoors = 1,
        defaultShelves = 4,
        defaultDrawers = 2,
        defaultLoft = false
    ),
    CUSTOM_FURNITURE(
        englishName = "Custom Furniture",
        hindiName = "कस्टम फर्नीचर",
        defaultHeightMm = 1500,
        defaultWidthMm = 1200,
        defaultDepthMm = 500,
        defaultDoors = 2,
        defaultShelves = 3,
        defaultDrawers = 1,
        defaultLoft = false
    )
}

enum class WoodFinish(val displayName: String, val hexColor: Long, val insideHexColor: Long) {
    TEAK_WOOD("Classic Teak", 0xFFB45309, 0xFFE0C59E),
    WALNUT_DARK("Rich Walnut", 0xFF451A03, 0xFFC9AF92),
    WHITE_GLOSS("Modern White Gloss", 0xFFF1F5F9, 0xFFE2E8F0),
    SLATE_GREY("Matte Charcoal Grey", 0xFF334155, 0xFF94A3B8),
    NATURAL_OAK("Natural Honey Oak", 0xFFD97706, 0xFFFDE68A),
    ROYAL_MAHOGANY("Royal Mahogany", 0xFF7F1D1D, 0xFFB97A7A),
    WARM_CHERRY("Warm Cherry Wood", 0xFF9A3412, 0xFFFDBA74)
}
