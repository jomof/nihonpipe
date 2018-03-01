package com.jomof.nihonpipe.groveler

import com.jomof.nihonpipe.groveler.schema.Store
import org.junit.Test

class H2Populator {
    @Test
    fun populate() {
        dataDir.mkdirs()
        dataDatabaseBin.delete()
        val store = Store()
        translatJishoJLPT(store)
        translateOptimizedKore(store)
        store.close()
    }
}