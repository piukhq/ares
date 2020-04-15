package com.bink.wallet.scenes.browse_brands

data class BrandsFilter(
    val category: String,
    var isChecked: Boolean = true
)