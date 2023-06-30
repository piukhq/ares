package com.bink.wallet.model

data class Releases(val releases: List<ReleaseData> = listOf())

data class ReleaseData(val release_title: String = "", val release_notes: List<ReleaseNotes> = listOf())

data class ReleaseNotes(val heading: String = "", val bullet_points: List<String> = listOf())

