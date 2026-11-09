package com.example.util

enum class AppLanguage {
    ENGLISH,
    HINDI
}

object Localization {

    fun get(key: String, lang: AppLanguage): String {
        val entry = strings[key] ?: return key
        return when (lang) {
            AppLanguage.ENGLISH -> entry.first
            AppLanguage.HINDI -> entry.second
        }
    }

    private val strings = mapOf(
        "app_title" to Pair("Gaurav Furniture Pro", "गौरव फर्नीचर प्रो"),
        "dashboard" to Pair("Dashboard", "डैशबोर्ड"),
        "customers" to Pair("Customers", "ग्राहक सूची"),
        "projects" to Pair("Projects", "प्रोजेक्ट्स"),
        "design_3d" to Pair("3D Design", "3D डिज़ाइन"),
        "cutting_list" to Pair("Cutting List", "कटिंग लिस्ट"),
        "sheet_optimizer" to Pair("Optimizer", "शीट ऑप्टिमाइज़र"),
        "estimation" to Pair("Estimation", "लागत अनुमान"),
        "quotation" to Pair("Quotation / PDF", "कोटेशन / PDF"),
        "backup_restore" to Pair("Backup & Restore", "डेटा बैकअप / रीस्टोर"),
        "take_screenshot" to Pair("Capture 3D View", "3D फोटो लें"),

        // Dashboard Stats
        "total_projects" to Pair("Total Projects", "कुल प्रोजेक्ट्स"),
        "total_customers" to Pair("Total Customers", "कुल ग्राहक"),
        "completed_projects" to Pair("Completed", "पूरे हुए"),
        "in_progress" to Pair("In Progress", "प्रगति पर"),
        "total_sales_value" to Pair("Total Sales Value", "कुल व्यापार मूल्य"),
        "new_project" to Pair("New Project", "नया प्रोजेक्ट"),
        "add_customer" to Pair("Add Customer", "नया ग्राहक जोड़ें"),

        // Design 3D & Specs
        "furniture_category" to Pair("Furniture Category", "फर्नीचर का प्रकार"),
        "height_mm" to Pair("Height (mm)", "ऊँचाई (mm)"),
        "width_mm" to Pair("Width (mm)", "चौड़ाई (mm)"),
        "depth_mm" to Pair("Depth (mm)", "गहराई (mm)"),
        "board_thickness" to Pair("Board Thickness (mm)", "बोर्ड मोटाई (mm)"),
        "doors_count" to Pair("Doors", "दरवाजे (Doors)"),
        "shelves_count" to Pair("Shelves", "शेल्फ (Shelves)"),
        "drawers_count" to Pair("Drawers", "दराज (Drawers)"),
        "wood_finish" to Pair("Wood Finish / Laminate", "वुड फिनिश / लैमिनेट"),
        "loft_unit" to Pair("Include Loft Unit", "ऊपरी लॉफ्ट शामिल करें"),
        "view_mode" to Pair("View Mode", "व्यू मोड"),
        "doors_open" to Pair("Open Doors", "दरवाजे खोलें"),
        "doors_closed" to Pair("Close Doors", "दरवाजे बंद करें"),
        "wireframe" to Pair("Wireframe / Blueprint", "वायरफ्रेम"),
        "solid_shaded" to Pair("Shaded 3D", "3D शेडेड"),
        "reset_view" to Pair("Reset View", "व्यू रीसेट"),

        // Cutting list
        "part_name" to Pair("Part Name", "भाग का नाम"),
        "dimensions" to Pair("Dimensions (L x W x T)", "माप (लंबाई x चौड़ाई x मोटाई)"),
        "qty" to Pair("Qty", "मात्रा"),
        "material" to Pair("Material", "सामग्री"),
        "edge_banding" to Pair("Edge Banding", "किनारे की टेप (Edge Banding)"),
        "add_custom_part" to Pair("Add Part Manually", "मैन्युअल पार्ट जोड़ें"),
        "edit_part" to Pair("Edit Part", "पार्ट बदलें"),
        "delete_part" to Pair("Delete Part", "हटाएं"),

        // Optimizer
        "sheet_size" to Pair("Standard Sheet Size", "शीट का आकार"),
        "blade_kerf" to Pair("Blade Kerf (mm)", "कटर ब्लेड मोटाई (mm)"),
        "optimize_cuts" to Pair("Calculate Optimal Nesting", "कटिंग लेआउट तैयार करें"),
        "sheets_required" to Pair("Sheets Required", "कुल आवश्यक शीट"),
        "wastage_percent" to Pair("Wastage %", "अपव्यय / बर्बादी %"),
        "efficiency_percent" to Pair("Efficiency", "दक्षता"),
        "sheet_view" to Pair("Sheet Diagram", "शीट नक्शा"),

        // Estimation
        "raw_material_cost" to Pair("Raw Board Cost", "प्लाईवुड / बोर्ड लागत"),
        "laminate_cost" to Pair("Laminate Cost", "लैमिनेट लागत"),
        "hardware_cost" to Pair("Hardware & Fittings", "हार्डवेयर और फिटिंग"),
        "labour_cost" to Pair("Carpentry Labour", "कारीगर मजदूरी"),
        "transportation" to Pair("Transportation", "परिवहन खर्च"),
        "installation" to Pair("Installation at Site", "साइट पर फिटिंग"),
        "other_expenses" to Pair("Other Expenses", "अन्य खर्च"),
        "profit_margin" to Pair("Profit Margin", "मुनाफा (Profit)"),
        "gst_tax" to Pair("GST Tax", "जीएसटी (GST)"),
        "grand_total" to Pair("Grand Total", "कुल देय राशि"),
        "advance_paid" to Pair("Advance Received", "अग्रिम भुगतान (Advance)"),
        "remaining_balance" to Pair("Remaining Balance", "बाकी राशि (Balance)"),
        "rates_settings" to Pair("Edit Market Rates", "बाजार दरें बदलें"),

        // Actions
        "generate_pdf" to Pair("Download Quotation PDF", "कोटेशन PDF डाउनलोड करें"),
        "quotation_pdf" to Pair("Customer Quotation PDF", "ग्राहक कोटेशन PDF"),
        "cutting_list_pdf" to Pair("Workshop Cutting List PDF", "वर्कशॉप कटिंग लिस्ट PDF"),
        "export_cutting_pdf" to Pair("Export Cutting PDF", "कटिंग PDF निकालें"),
        "share_whatsapp" to Pair("Share on WhatsApp", "WhatsApp पर भेजें"),
        "save_project" to Pair("Save Project", "प्रोजेक्ट सेव करें"),
        "duplicate_project" to Pair("Duplicate", "डुप्लिकेट कॉपी बनाएं"),
        "delete_project" to Pair("Delete", "प्रोजेक्ट हटाएं"),
        "customer_name" to Pair("Customer Name", "ग्राहक का नाम"),
        "mobile_number" to Pair("Mobile Number", "मोबाइल नंबर"),
        "address" to Pair("Site Address", "साइट का पता"),
        "project_name" to Pair("Project Name", "प्रोजेक्ट का नाम")
    )
}
