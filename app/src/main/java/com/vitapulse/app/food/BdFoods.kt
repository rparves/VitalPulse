package com.vitapulse.app.food

/**
 * Common Bangladeshi dishes. Values are approximate per-100 g estimates for typical
 * home/restaurant preparation; oil and portion size vary a lot, so users adjust grams.
 * Keys include romanised and Bangla names so search works in both scripts.
 */
internal object BdFoods {
    private const val SRC = "Bangladeshi dish (estimate)"
    private fun bd(name: String, kcal: Double, p: Double, c: Double, f: Double, serving: Double, cat: String, vararg keys: String) =
        LocalFood(name, kcal, p, c, f, serving, cat, keys.toList(), SRC)

    val all: List<LocalFood> = listOf(
        // Rice & grains
        bd("Kacchi biryani (mutton)", 210.0, 9.0, 22.0, 9.5, 350.0, "grain", "kacchi", "kachchi", "কাচ্চি"),
        bd("Chicken biryani", 180.0, 8.0, 22.0, 6.5, 350.0, "grain", "chicken biryani", "চিকেন বিরিয়ানি"),
        bd("Beef tehari", 195.0, 8.0, 24.0, 7.5, 300.0, "grain", "tehari", "তেহারি"),
        bd("Morog polao", 190.0, 9.0, 22.0, 7.0, 350.0, "grain", "morog polao", "murg polao", "মোরগ পোলাও"),
        bd("Plain polao", 175.0, 3.5, 28.0, 5.5, 200.0, "grain", "polao", "pulao", "পোলাও"),
        bd("Bhuna khichuri", 150.0, 5.0, 22.0, 4.5, 300.0, "grain", "khichuri", "khichdi", "খিচুড়ি"),
        bd("Panta bhaat", 70.0, 1.5, 15.0, 0.2, 250.0, "grain", "panta", "পান্তা"),
        bd("Fried rice (deshi Chinese)", 175.0, 4.5, 26.0, 6.0, 300.0, "grain", "fried rice", "ফ্রাইড রাইস"),
        bd("Chowmein", 165.0, 5.0, 24.0, 5.5, 250.0, "grain", "chowmein", "chow mein", "চাউমিন"),
        bd("Chira (flattened rice, dry)", 346.0, 6.6, 77.0, 1.2, 50.0, "grain", "chira", "poha", "চিড়া"),
        bd("Muri (puffed rice)", 400.0, 7.0, 90.0, 0.5, 30.0, "grain", "muri", "puffed rice", "মুড়ি"),
        bd("Luchi", 360.0, 7.0, 45.0, 17.0, 25.0, "grain", "luchi", "লুচি"),
        bd("Puri", 340.0, 7.0, 43.0, 16.0, 30.0, "grain", "puri", "পুরি"),
        bd("Naan", 290.0, 9.0, 50.0, 6.0, 90.0, "grain", "naan", "নান"),
        bd("Bakarkhani", 450.0, 8.0, 55.0, 22.0, 30.0, "grain", "bakarkhani", "বাকরখানি"),

        // Meat & egg
        bd("Beef kala bhuna", 250.0, 22.0, 4.0, 16.0, 150.0, "protein", "kala bhuna", "beef bhuna", "কালা ভুনা"),
        bd("Mezban beef", 210.0, 19.0, 4.0, 13.0, 150.0, "protein", "mezban", "mezbani", "মেজবান"),
        bd("Beef curry", 190.0, 18.0, 5.0, 11.0, 150.0, "protein", "beef curry", "gorur mangsho", "গরুর মাংস"),
        bd("Mutton curry (khashi)", 230.0, 18.0, 4.0, 16.0, 150.0, "protein", "mutton", "khashi", "kosha mangsho", "খাসির মাংস"),
        bd("Chicken curry", 150.0, 14.0, 5.0, 8.0, 150.0, "protein", "chicken curry", "murgi", "মুরগির তরকারি"),
        bd("Chicken roast (biye-bari)", 220.0, 18.0, 7.0, 13.0, 150.0, "protein", "chicken roast", "রোস্ট"),
        bd("Chicken rezala", 190.0, 15.0, 5.0, 12.0, 150.0, "protein", "rezala", "রেজালা"),
        bd("Chicken korma", 200.0, 15.0, 6.0, 13.0, 150.0, "protein", "korma", "কোরমা"),
        bd("Haleem", 140.0, 9.0, 15.0, 5.0, 250.0, "protein", "haleem", "halim", "হালিম"),
        bd("Nihari", 180.0, 15.0, 4.0, 12.0, 250.0, "protein", "nihari", "নিহারি"),
        bd("Egg curry (dim bhuna)", 160.0, 10.0, 5.0, 11.0, 120.0, "protein", "egg curry", "dim bhuna", "ডিম ভুনা"),
        bd("Dim bhaji (omelette)", 190.0, 12.0, 2.0, 15.0, 60.0, "protein", "omelette", "dim bhaji", "mamlet", "ডিম ভাজি"),
        bd("Shami kebab", 230.0, 14.0, 10.0, 15.0, 50.0, "protein", "shami", "শামি কাবাব"),
        bd("Sheek kebab", 220.0, 18.0, 4.0, 15.0, 80.0, "protein", "sheek", "seekh", "শিক কাবাব"),
        bd("Chicken tikka", 165.0, 25.0, 3.0, 6.0, 120.0, "protein", "tikka", "টিক্কা"),
        bd("Grilled chicken (deshi)", 215.0, 22.0, 3.0, 13.0, 200.0, "protein", "grill", "গ্রিল"),
        bd("Chicken shawarma", 230.0, 12.0, 22.0, 11.0, 250.0, "fast", "shawarma", "শর্মা"),

        // Fish
        bd("Ilish (hilsa), fried", 320.0, 22.0, 2.0, 25.0, 100.0, "protein", "ilish", "hilsa", "hilsha", "ইলিশ"),
        bd("Shorshe ilish", 270.0, 19.0, 3.0, 20.0, 120.0, "protein", "shorshe ilish", "sorshe", "bhapa ilish", "সর্ষে ইলিশ"),
        bd("Rui fish curry", 125.0, 15.0, 3.0, 6.0, 150.0, "protein", "rui", "rohu", "রুই"),
        bd("Pangas fish curry", 160.0, 13.0, 3.0, 11.0, 150.0, "protein", "pangas", "pangasius", "পাঙ্গাস"),
        bd("Tilapia fry", 190.0, 22.0, 4.0, 10.0, 120.0, "protein", "tilapia", "তেলাপিয়া"),
        bd("Koi fish curry", 150.0, 16.0, 3.0, 8.0, 120.0, "protein", "koi", "কই"),
        bd("Pabda fish curry", 140.0, 15.0, 3.0, 8.0, 120.0, "protein", "pabda", "পাবদা"),
        bd("Chingri malai curry", 180.0, 13.0, 5.0, 12.0, 150.0, "protein", "chingri", "prawn", "shrimp", "malai curry", "চিংড়ি"),
        bd("Shutki bhuna", 240.0, 35.0, 6.0, 9.0, 60.0, "protein", "shutki", "dried fish", "শুঁটকি"),

        // Bhorta
        bd("Aloo bhorta", 120.0, 2.0, 17.0, 5.5, 100.0, "veg", "aloo bhorta", "alu bhorta", "আলু ভর্তা"),
        bd("Begun bhorta", 90.0, 1.5, 8.0, 6.0, 100.0, "veg", "begun bhorta", "baingan", "বেগুন ভর্তা"),
        bd("Shutki bhorta", 200.0, 25.0, 8.0, 8.0, 50.0, "protein", "shutki bhorta", "শুঁটকি ভর্তা"),
        bd("Dal bhorta", 160.0, 9.0, 20.0, 5.0, 80.0, "protein", "dal bhorta", "daal bhorta", "ডাল ভর্তা"),
        bd("Tomato bhorta", 70.0, 1.0, 6.0, 5.0, 80.0, "veg", "tomato bhorta", "টমেটো ভর্তা"),
        bd("Chingri bhorta", 180.0, 14.0, 5.0, 12.0, 60.0, "protein", "chingri bhorta", "চিংড়ি ভর্তা"),

        // Dal & vegetables
        bd("Masoor dal (thin)", 75.0, 4.5, 11.0, 1.5, 200.0, "protein", "masoor", "musur", "patla dal", "মসুর ডাল"),
        bd("Mug dal", 105.0, 6.0, 14.0, 2.5, 200.0, "protein", "mug dal", "moong", "মুগ ডাল"),
        bd("Chola (chickpea curry)", 160.0, 7.0, 22.0, 5.0, 150.0, "protein", "chola", "chana", "ছোলা"),
        bd("Mixed vegetable (shobji)", 85.0, 2.0, 9.0, 5.0, 150.0, "veg", "sabji", "shobji", "mixed vegetable", "সবজি"),
        bd("Shak bhaji (leafy greens)", 70.0, 3.0, 6.0, 4.0, 100.0, "veg", "shak", "lal shak", "palong", "spinach", "শাক"),
        bd("Aloo bhaji", 150.0, 2.0, 19.0, 7.5, 100.0, "veg", "aloo bhaji", "alu bhaji", "আলু ভাজি"),
        bd("Labra", 90.0, 2.0, 10.0, 4.5, 150.0, "veg", "labra", "লাবড়া"),
        bd("Niramish", 80.0, 2.0, 9.0, 4.0, 150.0, "veg", "niramish", "নিরামিষ"),
        bd("Begun bhaja", 180.0, 1.5, 9.0, 16.0, 60.0, "veg", "begun bhaja", "বেগুন ভাজা"),
        bd("Korola bhaji", 110.0, 2.5, 8.0, 8.0, 80.0, "veg", "korola", "bitter gourd", "করলা"),

        // Street food & snacks
        bd("Fuchka", 230.0, 5.0, 32.0, 9.0, 120.0, "fast", "fuchka", "phuchka", "panipuri", "ফুচকা"),
        bd("Chotpoti", 150.0, 6.0, 22.0, 4.5, 200.0, "fast", "chotpoti", "chatpati", "চটপটি"),
        bd("Jhalmuri", 400.0, 9.0, 70.0, 10.0, 50.0, "snack", "jhalmuri", "jhal muri", "ঝালমুড়ি"),
        bd("Piyaju", 330.0, 9.0, 30.0, 19.0, 30.0, "fast", "piyaju", "peyaju", "পেঁয়াজু"),
        bd("Beguni", 280.0, 4.0, 25.0, 18.0, 40.0, "fast", "beguni", "বেগুনি"),
        bd("Aloo chop", 250.0, 4.0, 28.0, 13.0, 60.0, "fast", "aloo chop", "alu chop", "আলুর চপ"),
        bd("Dal puri", 330.0, 8.0, 40.0, 15.0, 50.0, "fast", "dal puri", "ডালপুরি"),
        bd("Velpuri", 220.0, 6.0, 28.0, 9.0, 150.0, "fast", "velpuri", "ভেলপুরি"),
        bd("Chicken patties", 320.0, 9.0, 30.0, 18.0, 80.0, "fast", "patties", "patty", "প্যাটিস"),
        bd("Vegetable roll", 260.0, 6.0, 30.0, 13.0, 70.0, "fast", "roll", "spring roll", "রোল"),
        bd("Momo", 200.0, 9.0, 25.0, 7.0, 150.0, "fast", "momo", "মোমো"),
        bd("Nimki", 480.0, 7.0, 55.0, 26.0, 30.0, "snack", "nimki", "নিমকি"),
        bd("Chanachur", 520.0, 14.0, 45.0, 32.0, 30.0, "snack", "chanachur", "চানাচুর"),

        // Sweets & pitha
        bd("Rosogolla", 186.0, 4.0, 38.0, 2.0, 50.0, "dessert", "rosogolla", "rasgulla", "রসগোল্লা"),
        bd("Roshmalai", 220.0, 7.0, 26.0, 10.0, 80.0, "dessert", "roshmalai", "rasmalai", "রসমালাই"),
        bd("Mishti doi", 150.0, 4.0, 24.0, 4.0, 100.0, "dessert", "mishti doi", "sweet yogurt", "মিষ্টি দই"),
        bd("Chomchom", 330.0, 6.0, 55.0, 9.0, 50.0, "dessert", "chomchom", "cham cham", "চমচম"),
        bd("Kalojam", 380.0, 5.0, 58.0, 14.0, 45.0, "dessert", "kalojam", "gulab jamun", "কালোজাম"),
        bd("Sandesh", 350.0, 10.0, 48.0, 13.0, 40.0, "dessert", "sandesh", "shondesh", "সন্দেশ"),
        bd("Jilapi", 400.0, 3.0, 62.0, 16.0, 40.0, "dessert", "jilapi", "jalebi", "জিলাপি"),
        bd("Payesh", 150.0, 4.0, 22.0, 5.0, 150.0, "dessert", "payesh", "kheer", "পায়েস"),
        bd("Firni", 160.0, 4.0, 24.0, 5.0, 120.0, "dessert", "firni", "ফিরনি"),
        bd("Shemai", 210.0, 4.0, 34.0, 7.0, 150.0, "dessert", "shemai", "semai", "সেমাই"),
        bd("Suji halwa", 330.0, 4.0, 45.0, 15.0, 80.0, "dessert", "halwa", "suji", "হালুয়া"),
        bd("Bhapa pitha", 230.0, 3.0, 48.0, 3.0, 60.0, "dessert", "bhapa pitha", "ভাপা পিঠা"),
        bd("Chitoi pitha", 160.0, 3.0, 35.0, 0.5, 60.0, "grain", "chitoi", "চিতই"),
        bd("Patishapta", 260.0, 5.0, 40.0, 9.0, 60.0, "dessert", "patishapta", "পাটিসাপটা"),
        bd("Pua / malpua", 380.0, 5.0, 50.0, 18.0, 50.0, "dessert", "pua", "malpua", "পুয়া"),
        bd("Laddu", 420.0, 6.0, 55.0, 20.0, 40.0, "dessert", "laddu", "ladoo", "লাড্ডু"),

        // Drinks
        bd("Lassi (sweet)", 90.0, 3.0, 15.0, 2.0, 250.0, "drink", "lassi", "লাচ্ছি"),
        bd("Borhani", 50.0, 2.0, 6.0, 1.5, 250.0, "drink", "borhani", "বোরহানি"),
        bd("Sugarcane juice", 70.0, 0.2, 18.0, 0.0, 250.0, "drink", "akher rosh", "sugarcane", "আখের রস"),
        bd("Lemon shorbot", 40.0, 0.1, 10.0, 0.0, 250.0, "drink", "shorbot", "lemonade", "শরবত"),
        bd("Daab (green coconut water)", 19.0, 0.7, 3.7, 0.2, 300.0, "drink", "daab", "coconut water", "ডাব"),
        bd("Falooda", 160.0, 4.0, 25.0, 5.0, 300.0, "drink", "falooda", "faluda", "ফালুদা"),

        // Local fruits
        bd("Jackfruit", 95.0, 1.7, 23.0, 0.6, 150.0, "fruit", "jackfruit", "kathal", "কাঁঠাল"),
        bd("Lychee", 66.0, 0.8, 17.0, 0.4, 100.0, "fruit", "lychee", "litchi", "লিচু"),
        bd("Guava", 68.0, 2.6, 14.0, 1.0, 100.0, "fruit", "guava", "peyara", "পেয়ারা"),
        bd("Papaya", 43.0, 0.5, 11.0, 0.3, 150.0, "fruit", "papaya", "pepe", "পেঁপে"),
        bd("Jamun", 60.0, 0.7, 16.0, 0.2, 100.0, "fruit", "jamun", "kalo jam", "জাম"),
        bd("Amra (hog plum)", 46.0, 0.7, 11.0, 0.1, 100.0, "fruit", "amra", "আমড়া"),
        bd("Boroi (jujube)", 79.0, 1.2, 20.0, 0.2, 100.0, "fruit", "boroi", "jujube", "বরই"),

        // Cooking basics
        bd("Cooking oil / ghee (1 tbsp)", 884.0, 0.0, 0.0, 100.0, 14.0, "snack", "oil", "tel", "ghee", "তেল", "ঘি"),
        bd("Sugar (1 tsp)", 387.0, 0.0, 100.0, 0.0, 5.0, "snack", "sugar", "chini", "চিনি"),
        bd("Gur (jaggery)", 383.0, 0.4, 98.0, 0.1, 20.0, "snack", "gur", "jaggery", "গুড়"),
    )
}
