package com.vitapulse.app.food

data class FoodCandidate(
    val name: String,
    val kcal100: Double,
    val protein100: Double,
    val carbs100: Double,
    val fat100: Double,
    val servingG: Double,
    val source: String,
)

/**
 * Small offline nutrition table (approximate values per 100 g, typical serving)
 * used to turn on-device image labels into calorie estimates. Packaged products
 * come from Open Food Facts instead.
 */
object FoodCatalog {
    private fun F(name: String, kcal: Double, p: Double, c: Double, f: Double, serving: Double, cat: String, keys: List<String>) =
        LocalFood(name, kcal, p, c, f, serving, cat, keys)

    private val general = listOf(
        F("Apple", 52.0, 0.3, 14.0, 0.2, 182.0, "fruit", listOf("apple")),
        F("Banana", 89.0, 1.1, 23.0, 0.3, 118.0, "fruit", listOf("banana", "kola", "কলা")),
        F("Orange", 47.0, 0.9, 12.0, 0.1, 131.0, "fruit", listOf("orange", "citrus", "mandarin", "komla", "কমলা")),
        F("Grapes", 69.0, 0.7, 18.0, 0.2, 150.0, "fruit", listOf("grape")),
        F("Strawberries", 32.0, 0.7, 7.7, 0.3, 150.0, "fruit", listOf("strawberr", "berry")),
        F("Watermelon", 30.0, 0.6, 7.6, 0.2, 280.0, "fruit", listOf("watermelon", "melon", "tormuj", "তরমুজ")),
        F("Mango", 60.0, 0.8, 15.0, 0.4, 165.0, "fruit", listOf("mango", "aam", "আম")),
        F("Pineapple", 50.0, 0.5, 13.0, 0.1, 165.0, "fruit", listOf("pineapple", "anaros", "আনারস")),
        F("Avocado", 160.0, 2.0, 8.5, 15.0, 150.0, "fruit", listOf("avocado")),
        F("Boiled egg", 155.0, 13.0, 1.1, 11.0, 50.0, "protein", listOf("egg", "dim sedho", "ডিম সিদ্ধ")),
        F("White rice, cooked", 130.0, 2.7, 28.0, 0.3, 158.0, "grain", listOf("rice", "bhaat", "bhat", "ভাত")),
        F("Brown rice, cooked", 112.0, 2.3, 24.0, 0.8, 195.0, "grain", listOf("brown rice")),
        F("Biryani", 170.0, 6.0, 22.0, 6.0, 300.0, "grain", listOf("biryani", "pilaf", "pulao")),
        F("White bread", 265.0, 9.0, 49.0, 3.2, 30.0, "grain", listOf("bread", "toast")),
        F("Whole-wheat bread", 247.0, 13.0, 41.0, 3.4, 32.0, "grain", listOf("whole wheat", "wholemeal")),
        F("Roti / chapati", 297.0, 9.6, 46.0, 9.2, 40.0, "grain", listOf("roti", "ruti", "chapati", "flatbread", "tortilla", "রুটি")),
        F("Paratha", 326.0, 6.4, 45.0, 13.0, 80.0, "grain", listOf("paratha", "porota", "পরোটা")),
        F("Pasta, cooked", 158.0, 5.8, 31.0, 0.9, 140.0, "grain", listOf("pasta", "spaghetti", "macaroni")),
        F("Noodles, cooked", 138.0, 4.5, 25.0, 2.0, 160.0, "grain", listOf("noodle", "ramen", "chow mein")),
        F("Oatmeal, cooked", 71.0, 2.5, 12.0, 1.5, 234.0, "grain", listOf("oat", "porridge")),
        F("Cornflakes", 357.0, 7.5, 84.0, 0.4, 30.0, "grain", listOf("cereal", "cornflakes")),
        F("Pizza", 266.0, 11.0, 33.0, 10.0, 107.0, "fast", listOf("pizza")),
        F("Hamburger", 254.0, 13.0, 30.0, 9.0, 150.0, "fast", listOf("burger", "hamburger", "cheeseburger")),
        F("French fries", 312.0, 3.4, 41.0, 15.0, 117.0, "fast", listOf("fries", "french fries", "chips")),
        F("Hot dog", 290.0, 10.0, 24.0, 17.0, 100.0, "fast", listOf("hot dog", "sausage")),
        F("Sandwich", 250.0, 11.0, 28.0, 10.0, 150.0, "fast", listOf("sandwich", "sub", "wrap")),
        F("Samosa / singara", 262.0, 4.5, 32.0, 13.0, 60.0, "fast", listOf("samosa", "singara", "সিঙ্গারা", "শিঙাড়া")),
        F("Fried chicken", 260.0, 19.0, 9.0, 16.0, 140.0, "protein", listOf("fried chicken", "nugget")),
        F("Chicken breast, cooked", 165.0, 31.0, 0.0, 3.6, 120.0, "protein", listOf("chicken", "poultry")),
        F("Beef steak", 271.0, 25.0, 0.0, 19.0, 150.0, "protein", listOf("beef", "steak", "meat")),
        F("Salmon", 208.0, 20.0, 0.0, 13.0, 120.0, "protein", listOf("salmon")),
        F("Fish, cooked", 140.0, 22.0, 0.0, 5.0, 120.0, "protein", listOf("fish", "seafood", "maach", "mach", "মাছ")),
        F("Sushi", 150.0, 6.0, 28.0, 1.5, 200.0, "protein", listOf("sushi")),
        F("Dal (lentils), cooked", 116.0, 9.0, 20.0, 0.4, 200.0, "protein", listOf("dal", "daal", "lentil", "ডাল")),
        F("Chickpeas, cooked", 164.0, 8.9, 27.0, 2.6, 160.0, "protein", listOf("chickpea", "hummus", "legume", "bean")),
        F("Green salad", 15.0, 1.2, 2.9, 0.2, 100.0, "veg", listOf("salad", "lettuce", "leaf vegetable")),
        F("Broccoli", 35.0, 2.4, 7.2, 0.4, 90.0, "veg", listOf("broccoli")),
        F("Carrot", 41.0, 0.9, 10.0, 0.2, 60.0, "veg", listOf("carrot")),
        F("Potato, boiled", 87.0, 1.9, 20.0, 0.1, 150.0, "veg", listOf("potato", "aloo", "alu", "আলু")),
        F("Tomato", 18.0, 0.9, 3.9, 0.2, 120.0, "veg", listOf("tomato")),
        F("Cucumber", 15.0, 0.7, 3.6, 0.1, 100.0, "veg", listOf("cucumber")),
        F("Vegetable soup", 30.0, 1.5, 5.0, 0.6, 250.0, "veg", listOf("vegetable soup")),
        F("Whole milk", 61.0, 3.2, 4.8, 3.3, 244.0, "dairy", listOf("milk", "dudh", "দুধ")),
        F("Plain yogurt", 61.0, 3.5, 4.7, 3.3, 170.0, "dairy", listOf("yogurt", "yoghurt", "curd", "doi")),
        F("Cheese", 402.0, 25.0, 1.3, 33.0, 28.0, "dairy", listOf("cheese")),
        F("Butter", 717.0, 0.9, 0.1, 81.0, 10.0, "dairy", listOf("butter")),
        F("Chocolate cake", 371.0, 5.3, 53.0, 15.0, 80.0, "dessert", listOf("cake", "pastry", "cupcake")),
        F("Cookie", 488.0, 5.0, 64.0, 24.0, 30.0, "dessert", listOf("cookie", "biscuit")),
        F("Donut", 452.0, 4.9, 51.0, 25.0, 60.0, "dessert", listOf("donut", "doughnut")),
        F("Ice cream", 207.0, 3.5, 24.0, 11.0, 66.0, "dessert", listOf("ice cream", "gelato")),
        F("Chocolate", 546.0, 4.9, 61.0, 31.0, 40.0, "dessert", listOf("chocolate", "candy")),
        F("Almonds", 579.0, 21.0, 22.0, 50.0, 28.0, "snack", listOf("almond", "nut")),
        F("Peanuts", 567.0, 26.0, 16.0, 49.0, 28.0, "snack", listOf("peanut")),
        F("Black coffee", 2.0, 0.3, 0.0, 0.0, 240.0, "drink", listOf("coffee", "espresso")),
        F("Tea with milk & sugar", 37.0, 1.0, 7.0, 0.8, 150.0, "drink", listOf("tea", "chai", "dudh cha", "চা")),
        F("Soft drink", 41.0, 0.0, 10.6, 0.0, 330.0, "drink", listOf("soda", "cola", "soft drink")),
        F("Orange juice", 45.0, 0.7, 10.0, 0.2, 250.0, "drink", listOf("juice")),
    )

    /** ML Kit labels that describe a category rather than a dish. */
    private val categoryLabels = mapOf(
        "fruit" to "fruit", "vegetable" to "veg", "produce" to "veg", "leaf vegetable" to "veg",
        "bread" to "grain", "baked goods" to "grain", "staple food" to "grain", "grain" to "grain",
        "meat" to "protein", "poultry" to "protein", "seafood" to "protein",
        "dairy" to "dairy", "dessert" to "dessert", "sweetness" to "dessert", "confectionery" to "dessert",
        "baking" to "dessert", "drink" to "drink", "beverage" to "drink",
        "fast food" to "fast", "junk food" to "fast", "snack" to "snack",
    )

    /** Labels too generic to use as an online search query. */
    val GENERIC = setOf(
        "food", "dish", "cuisine", "tableware", "plate", "ingredient", "recipe", "meal", "produce",
        "fruit", "vegetable", "natural foods", "fast food", "baked goods", "staple food", "bowl", "cup",
        "drink", "dessert", "snack", "lunch", "breakfast", "dinner", "junk food", "superfood",
        "comfort food", "sweetness", "dishware", "serveware", "table", "hand", "finger",
    )

    private val foods: List<LocalFood> = general + BdFoods.all

    private fun LocalFood.toCandidate() = FoodCandidate(name, kcal, p, c, f, serving, source)

    fun match(labels: List<String>): List<FoodCandidate> {
        val ls = labels.map { it.lowercase().trim() }
        val direct = foods.filter { food -> food.keys.any { k -> ls.any { l -> l.contains(k) } } }
        val cats = ls.mapNotNull { categoryLabels[it] }.toSet()
        val byCat = foods.filter { it.cat in cats }
        return (direct + byCat).distinct().take(8).map { it.toCandidate() }
    }

    fun byName(name: String): FoodCandidate? = foods.firstOrNull { it.name == name }?.toCandidate()

    fun search(query: String): List<FoodCandidate> {
        val q = query.lowercase().trim()
        if (q.length < 2) return emptyList()
        return foods.mapNotNull { f ->
            val n = f.name.lowercase()
            val rank = when {
                n.startsWith(q) -> 0
                n.contains(q) -> 1
                f.keys.any { it.startsWith(q) } -> 2
                f.keys.any { it.contains(q) || (it.length >= 4 && q.contains(it)) } -> 3
                else -> null
            }
            rank?.let { it to f }
        }.sortedBy { it.first }.take(25).map { it.second.toCandidate() }
    }
}
