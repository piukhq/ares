package com.bink.wallet.utils


object ColourPalette {

    val grecian = listOf("#2f496e", "#ed8c72", "#2988bc", "#f4eade")
    val antique = listOf("#eab364", "#acbd78", "#a4cabc", "#b2473e")
    val warm = listOf("#ce5a57", "#e1b16a", "#444c5c", "#78a5a3")
    val serene = listOf("#ffdb5c", "#f8a055", "#4897db", "#fa6e59")
    val lemonade = listOf("#e2dfa2", "#ed5752", "#a1be95", "#92aac7")
    val muted = listOf("#002c54", "#cd7213", "#16253d", "#efb509")
    val watery = listOf("#004445", "#6fb98f", "#021c1e", "#2c7873")
    val outdoorsy = listOf("#486b00", "#7d4427", "#2e4600", "#a2c523")
    val primary = listOf("#fb6542", "#37681c", "#375e97", "#ffbb00")
    val sleek = listOf("#d5d6d2", "#3a5199", "#2f2e33", "#ffffff")
    val school = listOf("#fdc3c3", "#138d90", "#061283", "#ffb74c")

    private val coloursList = listOf(
        grecian, antique, warm, serene, lemonade, muted, watery, outdoorsy, primary,
        sleek, school
    )

    fun getRandomColour(): String {
        val colourList = coloursList.random()
        return colourList.random()
    }

}