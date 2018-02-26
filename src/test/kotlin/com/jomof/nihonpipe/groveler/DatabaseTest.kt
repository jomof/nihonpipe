package com.jomof.nihonpipe.groveler

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class DatabaseTest {
    @Test
    fun testDatabaseSecondWrite() {
        val db = Database(createTempDir("nihonpipe"))
        db.withinKey("readme.txt", "raw")
                .write("Hello world", "text-file")
                .write("Goodbye world", "text-file")
        val got = db.get("readme.txt", "raw", "text-file")
        assertThat(got).named("second write should fail").isEqualTo("Hello world")
        db.save()
    }

    @Test
    fun testDatabaseSecondAcrossOpen() {
        val root = createTempDir("nihonpipe")
        val db = Database(root)
        db.withinKey("readme.txt", "raw")
                .write("Hello world", "text-file")
        db.save()
        val db2 = Database(root)
        db2.withinKey("readme.txt", "raw")
                .write("Goodbye world", "text-file")
        val got = db2.get("readme.txt", "raw", "text-file")
        assertThat(got).named("second write should fail").isEqualTo("Hello world")
        db.save()
    }
}