package com.jomof.nihonpipe.play.io

import com.jomof.nihonpipe.play.MezzoScore


data class RespondSentenceTestResponse(
        val wasCorrect: Boolean,
        val mezzoScore: MezzoScore
)