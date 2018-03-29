package com.jomof.nihonpipe.play

import com.jomof.nihonpipe.datafiles.tokenizeJapaneseSentence
import com.jomof.nihonpipe.groveler.schema.particleSkeletonForm
import com.jomof.nihonpipe.play.MezzoScore.APPRENTICE
import com.jomof.nihonpipe.play.MezzoScore.GURU

/**
 * Provides a hint to the user about the sentence. This hint
 * presents a sentence particle skeleton.
 */
fun skeletonHint(japanese: String, score: Score): String {
    return when (score.mezzo()) {
        APPRENTICE, GURU -> {
            val tokenization = tokenizeJapaneseSentence(japanese)
            val skeleton = tokenization.particleSkeletonForm()
            if (!skeleton.contains("x")) {
                return ""
            }
            skeleton
        }
        else -> ""
    }
}