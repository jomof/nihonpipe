package com.jomof.nihonpipe

import java.io.File

private val projectRootDir = File(".").absoluteFile.canonicalFile!!
internal val dataDir = File(projectRootDir, "data")
/*--*/internal val dataKuromojiBin = File(dataDir, "kuromoji-cache.bin")
/*--*/internal val tokenToSentenceBin = File(dataDir, "token-to-sentence.bin")
/*--*/internal val jishoJlptVocabsBin = File(dataDir, "jisho-jlpt-vocabs.bin")
/*--*/internal val optimizedKoreVocabsBin = File(dataDir, "optimized-kore-vocabs.bin")
/*--*/internal val wanikaniVocabsBin = File(dataDir, "wanikani-vocabs.bin")
/*--*/internal val translatedSentencesBin = File(dataDir, "translated-sentences.bin")
/*--*/internal val wanikaniVsJlptVocabsBin = File(dataDir, "wanikani-vs-jlpt-vocabs.bin")
/*--*/internal val sentenceStatisticsCacheBin = File(dataDir, "sentence-statistics-cache.bin")
/*--*/internal val sentenceSkeletonFilter = File(dataDir, "sentence-skeleton.filter")
/*--*/internal val grammarSummaryFilter = File(dataDir, "grammar-summary.filter")
/*--*/internal val vocabToSentenceFilter = File(dataDir, "vocab-to-sentence.filter")
/*--*/internal val sentenceSkeletonLadderBin = File(dataDir, "sentence-skeleton-ladder.bin")
/*--*/internal val grammarSummaryLadderBin = File(dataDir, "grammar-summary-ladder.bin")
/*--*/internal val tokenBaseformFrequencyLadderBin = File(dataDir, "token-baseform-frequency-ladder.bin")
/*--*/internal val tokenSurfaceFrequencyLadderBin = File(dataDir, "token-surface-frequency-ladder.bin")
/*--*/internal val sampleSentencesTsv = File(dataDir, "sample-sentences.tsv")
/*--*/internal val sentenceTransitionsBin = File(dataDir, "sentence-transitions.bin")
private val externalDir = File(projectRootDir, "external")
/*--*/internal val tanakaWWWJDICExamplesResidueFile = File(externalDir, "tanaka-WWWJDIC/examples-residue.txt")
/*--*/internal val core2k6k10kFile = File(externalDir, "core2k6k10k.tsv")
/*--*/internal val optimizedKoreFile = File(externalDir, "optimized-kore.tsv")
/*--*/internal val wanikaniVocabFile = File(externalDir, "wani-kani-vocab.tsv")
/*--*/internal val personalSentencesFile = File(externalDir, "personal-sentences.txt")
/*--*/internal val gapFillingSentencesFile = File(externalDir, "gap-filling-sentences.txt")
/*--*/internal val achievementsDir = File(externalDir, "achievements")
/*--*/private val wanikaniVsJlptDir = File(externalDir, "wanikani-vs-jlpt")
/*----*/internal val wanikanivsjlptJLPT1 = File(wanikaniVsJlptDir, "jlpt1.tsv")
/*----*/internal val wanikanivsjlptJLPT2 = File(wanikaniVsJlptDir, "jlpt2.tsv")
/*----*/internal val wanikanivsjlptJLPT3 = File(wanikaniVsJlptDir, "jlpt3.tsv")
/*----*/internal val wanikanivsjlptJLPT4 = File(wanikaniVsJlptDir, "jlpt4.tsv")
/*----*/internal val wanikanivsjlptJLPT5 = File(wanikaniVsJlptDir, "jlpt5.tsv")
/*--*/private val jishoDir = File(externalDir, "jisho")
/*----*/internal val jishoJLPT1 = File(jishoDir, "JLPT1.tsv")
/*----*/internal val jishoJLPT2 = File(jishoDir, "JLPT2.tsv")
/*----*/internal val jishoJLPT3 = File(jishoDir, "JLPT3.tsv")
/*----*/internal val jishoJLPT4 = File(jishoDir, "JLPT4.tsv")
/*----*/internal val jishoJLPT5 = File(jishoDir, "JLPT5.tsv")
/*--*/private val jacyDir = File(externalDir, "jacy")
/*----*/private val jacyDataDir = File(jacyDir, "data")
/*------*/internal val jacyDataTanakaDir = File(jacyDataDir, "tanaka")
