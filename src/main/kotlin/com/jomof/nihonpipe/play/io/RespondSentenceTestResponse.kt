package com.jomof.nihonpipe.play.io

import com.jomof.nihonpipe.play.LadderCoordinate
import com.jomof.nihonpipe.play.MezzoScore

data class AchievementElement(
        val vocab: String,
        val flavors: Set<String>
)

data class RespondSentenceTestResponse(
        val wasCorrect: Boolean,
        val japanese: String,
        val pronunciation: String,
        val reading: String,
        val mezzoPromotion: MezzoScore?,
        val achievementsUnlocked: Set<String>,
        val achievementElementsUnlocked: List<AchievementElement>,
        val ladderKeyElementsUnlocked: List<LadderCoordinate>)