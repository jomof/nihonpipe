package com.jomof.nihonpipe.groveler

import java.io.File

private val projectRootDir = File(".").absoluteFile.canonicalFile!!
internal val linuxScriptFile = File(projectRootDir, "make.sh")
/**/private val externalDir = File(projectRootDir, "external")
/*--*/internal val optimizedKoreFile = File(externalDir, "optimized-kore.tsv")
/*--*/internal val wanikaniVocabFile = File(externalDir, "wani-kani-vocab.tsv")
/*--*/private val jishoDir = File(externalDir, "jisho")
/*----*/internal val jishoJLPT1 = File(jishoDir, "JLPT1.tsv")
/*----*/internal val jishoJLPT2 = File(jishoDir, "JLPT2.tsv")
/*----*/internal val jishoJLPT3 = File(jishoDir, "JLPT3.tsv")
/*----*/internal val jishoJLPT4 = File(jishoDir, "JLPT4.tsv")
/*----*/internal val jishoJLPT5 = File(jishoDir, "JLPT5.tsv")
/*--*/private val jacyDir = File(externalDir, "jacy")
/*----*/private val jacyAceDir = File(jacyDir, "ace")
/*----*/internal val jacyAceConfigTdlFile = File(jacyAceDir, "config.tdl")
/*----*/private val jacyDataDir = File(jacyDir, "data")
/*------*/internal val jacyDataTanakaDir = File(jacyDataDir, "tanaka")
/**/private val processedDir = File(projectRootDir, "processed")
/*--*/ val indexedDir = File(processedDir, "indexed")
/*--*/private val binDir = File(projectRootDir, "bin")
/*----*/private val aceBinDir = File(binDir, "ace-0.9.26")
/*----*/internal val aceExecutableFile = File(aceBinDir, "ace")
/**/private val grammarsDir = File(projectRootDir, "grammars")
/*--*/private val grammarsJacyDir = File(grammarsDir, "jacy")
/*--*/internal val grammarsJacyDatFile = File(grammarsJacyDir, "jacy.dat")
