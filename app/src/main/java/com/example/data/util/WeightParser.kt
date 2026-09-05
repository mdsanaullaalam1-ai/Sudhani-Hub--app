package com.example.data.util

object WeightParser {
    /**
     * Parses weight in grams from unit string or product name
     * Examples:
     * "500 g", "500g", "500 gm" -> 500
     * "1 kg", "1.5 kg", "2 kg", "5 kg" -> 1000, 1500, 2000, 5000
     * "1 L", "1 litre", "500 ml" -> 1000, 1000, 500
     * "250 g" -> 250
     * "6 pcs", "12 pcs" -> 600, 1200
     * Fallback -> 500
     */
    fun parseWeightInGrams(unit: String, name: String = ""): Int {
        val combined = "$unit $name".lowercase()

        // Match kg pattern: e.g. "1.5 kg", "1kg", "5 kg"
        val kgMatch = Regex("(\\d+(?:\\.\\d+)?)\\s*(?:kg|kilo|kilogram)").find(combined)
        if (kgMatch != null) {
            val value = kgMatch.groupValues[1].toDoubleOrNull() ?: 1.0
            return (value * 1000.0).toInt().coerceAtLeast(50)
        }

        // Match litre pattern: e.g. "1 L", "1.5 litre", "2 ltr"
        val litreMatch = Regex("(\\d+(?:\\.\\d+)?)\\s*(?:l|ltr|litre|liter)").find(combined)
        if (litreMatch != null) {
            val value = litreMatch.groupValues[1].toDoubleOrNull() ?: 1.0
            return (value * 1000.0).toInt().coerceAtLeast(50)
        }

        // Match ml pattern: e.g. "500 ml", "200ml"
        val mlMatch = Regex("(\\d+(?:\\.\\d+)?)\\s*(?:ml|milli)").find(combined)
        if (mlMatch != null) {
            val value = mlMatch.groupValues[1].toDoubleOrNull() ?: 250.0
            return value.toInt().coerceAtLeast(50)
        }

        // Match gram pattern: e.g. "500 g", "250 gm", "100 gms"
        val gramMatch = Regex("(\\d+(?:\\.\\d+)?)\\s*(?:g|gm|gram|gms)").find(combined)
        if (gramMatch != null) {
            val value = gramMatch.groupValues[1].toDoubleOrNull() ?: 500.0
            return value.toInt().coerceAtLeast(10)
        }

        // Match pieces pattern: e.g. "6 pcs", "12 pcs"
        val pcsMatch = Regex("(\\d+)\\s*(?:pcs|pc|pieces|pack)").find(combined)
        if (pcsMatch != null) {
            val count = pcsMatch.groupValues[1].toIntOrNull() ?: 1
            return (count * 100).coerceIn(100, 3000)
        }

        return 500
    }
}
