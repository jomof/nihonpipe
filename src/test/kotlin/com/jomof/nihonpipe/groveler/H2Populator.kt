package com.jomof.nihonpipe.groveler

import com.jomof.nihonpipe.groveler.schema.JishoVocab
import org.h2.mvstore.MVStore
import org.junit.Test

class H2Populator {
    @Test
    fun populateJishoByJlptBin() {
        dataDir.mkdirs()
        var store = MVStore.Builder().fileName(dataJishoByJlptBin.absolutePath).compress().open()
        var map = store.openMap<String, List<JishoVocab>>("jisho-vocab")
        translatJishoJLPT(map)
        store.close()
    }
}