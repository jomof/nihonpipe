package com.jomof.nihonpipe.play.io

data class UserStatisticsResponse(
        val apprenticeSentences: Int,
        val guruSentences: Int,
        val masterSentences: Int,
        val enlightenedSentences: Int,
        val burnedSentences: Int
)