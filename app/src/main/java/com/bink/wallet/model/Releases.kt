package com.bink.wallet.model

data class Releases(val releases: List<ReleaseData>)

data class ReleaseData(val release_title: String, val release_notes: List<ReleaseNotes>)

data class ReleaseNotes(val heading: String, val bullet_points: List<String>)

